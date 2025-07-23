package gr.uoa.di.madgik.node.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.node.model.EndpointCapabilities;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping(value = "endpoint", produces = MediaType.APPLICATION_JSON_VALUE)
public class EndpointController {

    private EndpointCapabilities capabilities;

    @GetMapping
    public EndpointCapabilities getCapabilities() {
        return load();
    }

    private EndpointCapabilities load() {
        if (capabilities == null) {
            ObjectMapper mapper = new ObjectMapper();
            try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("capabilities.json")) {

                if (is != null) {
                    String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                    this.capabilities = mapper.convertValue(mapper.readTree(content), EndpointCapabilities.class);
                } else {
                    throw new IOException("Could not read capabilities.json");
                }
            } catch (IOException e) {
                return null;
            }
        }
        return capabilities;
    }
}
