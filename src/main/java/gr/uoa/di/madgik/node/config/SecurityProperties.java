package gr.uoa.di.madgik.node.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@Validated
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    @NotEmpty
    private Set<String> adminEmails = new LinkedHashSet<>();
    @NotNull
    @NotEmpty
    private String loginRedirect = "/";
    @NotNull
    @NotEmpty
    private String logoutRedirect = "/";

    public Set<String> getAdminEmails() {
        return adminEmails;
    }

    public void setAdminEmails(Set<String> adminEmails) {
        this.adminEmails = adminEmails;
    }

    public String getLoginRedirect() {
        return loginRedirect;
    }

    public void setLoginRedirect(String loginRedirect) {
        this.loginRedirect = loginRedirect;
    }

    public String getLogoutRedirect() {
        return logoutRedirect;
    }

    public void setLogoutRedirect(String logoutRedirect) {
        this.logoutRedirect = logoutRedirect;
    }
}
