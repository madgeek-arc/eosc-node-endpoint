package gr.uoa.di.madgik.node.controller;

import gr.uoa.di.madgik.node.model.EndpointCapabilities;
import gr.uoa.di.madgik.node.service.EndpointService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "endpoint", produces = MediaType.APPLICATION_JSON_VALUE)
public class EndpointController {

    private EndpointCapabilities capabilities;
    private EndpointService service;

    public EndpointController(EndpointService service) {
        this.service = service;
    }

    @GetMapping
    public EndpointCapabilities getCapabilities() {
        return service.get();
    }
}
