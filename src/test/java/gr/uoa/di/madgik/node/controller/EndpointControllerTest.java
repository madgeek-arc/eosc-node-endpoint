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

import tools.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.node.model.Capability;
import gr.uoa.di.madgik.node.model.EndpointCapabilities;
import gr.uoa.di.madgik.node.service.EndpointService;
import gr.uoa.di.madgik.node.service.FileBackedEndpointService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EndpointControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void putThenGetReturnsPersistedCapabilities() throws Exception {
        EndpointService service = new FileBackedEndpointService(tempDir.resolve("capabilities.json").toString(), objectMapper);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new EndpointController(service))
                .setControllerAdvice(new EndpointExceptionHandler())
                .build();

        EndpointCapabilities payload = new EndpointCapabilities();
        payload.setNodeEndpoint(URI.create("https://node.eosc-beyond.eu"));

        Capability capability = new Capability();
        capability.setCapabilityType("test");
        capability.setEndpoint("https://node.eosc-beyond.eu/api/test");
        capability.setProtocol("REST");
        capability.setStatus("maintenance");
        payload.setCapabilities(List.of(capability));

        mockMvc.perform(put("/endpoint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(payload)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.node_endpoint").value("https://node.eosc-beyond.eu"))
                .andExpect(jsonPath("$.capabilities[0].protocol").value("REST"))
                .andExpect(jsonPath("$.capabilities[0].status").value("MAINTENANCE"));

        mockMvc.perform(get("/endpoint"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.node_endpoint").value("https://node.eosc-beyond.eu"))
                .andExpect(jsonPath("$.capabilities[0].capability_type").value("test"))
                .andExpect(jsonPath("$.capabilities[0].endpoint").value("https://node.eosc-beyond.eu/api/test"))
                .andExpect(jsonPath("$.capabilities[0].protocol").value("REST"))
                .andExpect(jsonPath("$.capabilities[0].status").value("MAINTENANCE"));
    }
}
