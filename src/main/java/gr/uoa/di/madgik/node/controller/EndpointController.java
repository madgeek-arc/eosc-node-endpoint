package gr.uoa.di.madgik.node.controller;

import gr.uoa.di.madgik.node.model.EndpointCapabilities;
import gr.uoa.di.madgik.node.service.EndpointService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "endpoint", produces = MediaType.APPLICATION_JSON_VALUE)
public class EndpointController {

    private final EndpointService service;

    public EndpointController(EndpointService service) {
        this.service = service;
    }

    @GetMapping
    public EndpointCapabilities getEndpointCapabilities() throws IOException {
        return service.get();
    }

    @PutMapping
    public EndpointCapabilities updateEndpointCapabilities(@RequestBody EndpointCapabilities capabilities) throws IOException {
        return service.update(capabilities);
    }
}
