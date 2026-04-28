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

package gr.uoa.di.madgik.node.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.node.exception.ReadCapabilitiesException;
import gr.uoa.di.madgik.node.exception.WriteCapabilitiesException;
import gr.uoa.di.madgik.node.model.Capability;
import gr.uoa.di.madgik.node.model.EndpointCapabilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

class FileBackedEndpointServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void getReturnsEmptyCapabilitiesWhenFileDoesNotExist() {
        EndpointService service = new FileBackedEndpointService(tempDir.resolve("missing.json").toString(), objectMapper);

        EndpointCapabilities result = service.get();

        assertNotNull(result);
        assertNull(result.getNodeEndpoint());
        assertNull(result.getCapabilities());
    }

    @Test
    void getThrowsWhenStoredJsonIsInvalid() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        Files.writeString(file, "{ invalid json");
        EndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        assertThrows(ReadCapabilitiesException.class, service::get);
    }

    @Test
    void updatePersistsCapabilitiesToDisk() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        EndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        EndpointCapabilities payload = new EndpointCapabilities();
        payload.setNodeEndpoint(URI.create("https://node.eosc-beyond.eu"));

        Capability capability = new Capability();
        capability.setCapabilityType("test");
        capability.setEndpoint("https://node.eosc-beyond.eu/api/test");
        payload.setCapabilities(List.of(capability));

        EndpointCapabilities result = service.update(payload);
        EndpointCapabilities persisted = objectMapper.readValue(file.toFile(), EndpointCapabilities.class);

        assertEquals("https://node.eosc-beyond.eu", result.getNodeEndpoint().toString());
        assertEquals("https://node.eosc-beyond.eu", persisted.getNodeEndpoint().toString());
        assertEquals(1, persisted.getCapabilities().size());
    }

    @Test
    void updateThrowsWhenTargetPathIsNotWritableFile() throws IOException {
        Path directory = tempDir.resolve("capabilities.json");
        Files.createDirectory(directory);
        EndpointService service = new FileBackedEndpointService(directory.toString(), objectMapper);

        assertThrows(WriteCapabilitiesException.class, () -> service.update(new EndpointCapabilities()));
    }
}
