/*
 * Copyright 2026 OpenAIRE AMKE & Athena Research and Innovation Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
