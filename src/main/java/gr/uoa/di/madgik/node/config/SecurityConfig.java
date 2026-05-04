package gr.uoa.di.madgik.node.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.client.RestClient;

import java.util.*;

@Configuration
public class SecurityConfig {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final Set<String> admins;

    public SecurityConfig(ClientRegistrationRepository clientRegistrationRepository,
                          @Value("${security.admin-emails:}") Set<String> admins) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.admins = admins;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.GET, "/**").permitAll()
                                .requestMatchers("/**").hasAuthority("ADMIN")
                                .anyRequest().permitAll()
                )

                .exceptionHandling(exceptionHandling ->
                        exceptionHandling
                                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                .oauth2Login(oauth2login ->
                        oauth2login.defaultSuccessUrl("/"))

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(authenticationConverter())
                        )
                )

                .logout(logout ->
                        logout
                                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                                .deleteCookies()
                                .clearAuthentication(true)
                                .invalidateHttpSession(true))
                .cors(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(
                        this.clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("/");

        return oidcLogoutSuccessHandler;
    }

    @Bean
    public GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return (authorities) -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

            authorities.forEach(authority -> {
                String email = "";
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    // Map the claims found in idToken and/or userInfo
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                    OidcUserInfo userInfo = oidcUserAuthority.getUserInfo();
                    if (userInfo != null) {
                        email = userInfo.getEmail();
                    } else {
                        if (oidcUserAuthority.getAttributes() != null
                                && oidcUserAuthority.getAttributes().containsKey("email")) {
                            email = oidcUserAuthority.getAttributes().get("email").toString();
                        }
                    }

                } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                    // Map the attributes found in userAttributes
                    // to one or more GrantedAuthority's and add it to mappedAuthorities

                    Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();

                    if (userAttributes != null) {
                        email = userAttributes.get("email").toString();
                    }
                }

                if (admins.contains(email)) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ADMIN"));
                }
            });

            return mappedAuthorities;
        };
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> authenticationConverter() {
        return new CustomJwtAuthenticationConverter();
    }

    class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

        private final RestClient restClient = RestClient.builder().build();

        public AbstractAuthenticationToken convert(Jwt jwt) {
            String email = fetchEmailFromUserInfo(jwt.getTokenValue());

            Collection<GrantedAuthority> authorities = new HashSet<>();
            if (admins.contains(email)) {
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
            }

            return new JwtAuthenticationToken(jwt, authorities);
        }

        private String fetchEmailFromUserInfo(String accessToken) {
            String userInfoUri = clientRegistrationRepository.findByRegistrationId("eosc")
                    .getProviderDetails().getUserInfoEndpoint().getUri();

            Map<String, Object> body = restClient.get()
                    .uri(userInfoUri)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (body != null && body.containsKey("email")) {
                return body.get("email").toString();
            }
            return "";
        }
    }
}
