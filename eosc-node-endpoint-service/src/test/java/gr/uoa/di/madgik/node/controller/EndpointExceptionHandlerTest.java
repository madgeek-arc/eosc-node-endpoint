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

import gr.uoa.di.madgik.node.exception.ReadCapabilitiesException;
import gr.uoa.di.madgik.node.exception.WriteCapabilitiesException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EndpointExceptionHandlerTest {

    @Test
    void getReturnsProblemDetailWhenReadFails() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new EndpointExceptionHandler())
                .build();

        mockMvc.perform(get("/test/read"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Capabilities read failed"))
                .andExpect(jsonPath("$.detail").value("read failed"));
    }

    @Test
    void putReturnsProblemDetailWhenWriteFails() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new EndpointExceptionHandler())
                .build();

        mockMvc.perform(put("/test/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Capabilities write failed"))
                .andExpect(jsonPath("$.detail").value("write failed"));
    }

    @RestController
    private static class ThrowingController {

        @GetMapping("/test/read")
        public void read() {
            throw new ReadCapabilitiesException("read failed", new IOException());
        }

        @PutMapping("/test/write")
        public void write(@RequestBody String ignoredBody) {
            throw new WriteCapabilitiesException("write failed", new IOException());
        }
    }
}
