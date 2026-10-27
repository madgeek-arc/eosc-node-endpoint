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

import tools.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.node.exception.InvalidCapabilitiesException;
import gr.uoa.di.madgik.node.exception.ReadCapabilitiesException;
import gr.uoa.di.madgik.node.exception.WriteCapabilitiesException;
import gr.uoa.di.madgik.node.capabilities.model.Capability;
import gr.uoa.di.madgik.node.capabilities.model.NodeCapabilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class FileBackedEndpointServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void getReturnsEmptyCapabilitiesWhenFileDoesNotExist() {
        EndpointService service = new FileBackedEndpointService(tempDir.resolve("missing.json").toString(), objectMapper);

        NodeCapabilities result = service.get();

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
    void getPreservesNullProtocolAndStatusFromStoredJson() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        Files.writeString(file, """
                {
                  "capabilities": [
                    {
                      "capability_type": "test",
                      "endpoint": "https://node.eosc-beyond.eu/api/test",
                      "protocol": null,
                      "status": null
                    }
                  ]
                }
                """);
        EndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        Capability capability = service.get().getCapabilities().getFirst();

        assertNull(capability.getProtocol());
        assertNull(capability.getStatus());
    }

    @Test
    void getReturnsCachedCapabilitiesBeforeCacheTtlExpires() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        writeCapabilitiesFile(file, "https://node.eosc-beyond.eu/first");
        MutableClock clock = new MutableClock(Instant.parse("2026-04-28T12:00:00Z"));
        EndpointService service = new FileBackedEndpointService(file.toString(), Duration.ofMinutes(1), objectMapper, clock);

        NodeCapabilities first = service.get();
        writeCapabilitiesFile(file, "https://node.eosc-beyond.eu/second");
        clock.advance(Duration.ofSeconds(59));

        NodeCapabilities second = service.get();

        assertEquals("https://node.eosc-beyond.eu/first", first.getNodeEndpoint().toString());
        assertEquals("https://node.eosc-beyond.eu/first", second.getNodeEndpoint().toString());
    }

    @Test
    void getReloadsCapabilitiesWhenCacheTtlExpires() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        writeCapabilitiesFile(file, "https://node.eosc-beyond.eu/first");
        MutableClock clock = new MutableClock(Instant.parse("2026-04-28T12:00:00Z"));
        EndpointService service = new FileBackedEndpointService(file.toString(), Duration.ofMinutes(1), objectMapper, clock);

        NodeCapabilities first = service.get();
        writeCapabilitiesFile(file, "https://node.eosc-beyond.eu/second");
        clock.advance(Duration.ofMinutes(1));

        NodeCapabilities second = service.get();

        assertEquals("https://node.eosc-beyond.eu/first", first.getNodeEndpoint().toString());
        assertEquals("https://node.eosc-beyond.eu/second", second.getNodeEndpoint().toString());
    }

    @Test
    void updatePersistsCapabilitiesToDisk() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        EndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        NodeCapabilities payload = new NodeCapabilities();
        payload.setNodeEndpoint(URI.create("https://node.eosc-beyond.eu"));

        Capability capability = new Capability();
        capability.setCapabilityType("test");
        capability.setEndpoint("https://node.eosc-beyond.eu/api/test");
        payload.setCapabilities(List.of(capability));

        NodeCapabilities result = service.update(payload);
        NodeCapabilities persisted = objectMapper.readValue(file.toFile(), NodeCapabilities.class);

        assertEquals("https://node.eosc-beyond.eu", result.getNodeEndpoint().toString());
        assertEquals("https://node.eosc-beyond.eu", persisted.getNodeEndpoint().toString());
        assertEquals(1, persisted.getCapabilities().size());
    }

    @Test
    void updateThrowsWhenTargetPathIsNotWritableFile() throws IOException {
        Path directory = tempDir.resolve("capabilities.json");
        Files.createDirectory(directory);
        EndpointService service = new FileBackedEndpointService(directory.toString(), objectMapper);

        assertThrows(WriteCapabilitiesException.class, () -> service.update(new NodeCapabilities()));
    }

    @Test
    void getDoesNotAssignOrWriteIds() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        writeLegacyCapabilitiesFile(file);
        String before = Files.readString(file);
        EndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        Capability capability = service.get().getCapabilities().getFirst();

        assertNull(capability.getId());
        assertEquals(before, Files.readString(file));
    }

    @Test
    void fillMissingIdsPersistsDerivedIds() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        writeLegacyCapabilitiesFile(file);
        FileBackedEndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        service.fillMissingIds();

        Capability persisted = objectMapper.readValue(file.toFile(), NodeCapabilities.class).getCapabilities().getFirst();
        UUID expected = Capability.deriveId("test", URI.create("https://node.eosc-beyond.eu/api/test"));
        assertEquals(expected, persisted.getId());
        assertEquals(expected, service.get().getCapabilities().getFirst().getId());
    }

    @Test
    void fillMissingIdsKeepsExistingIdsAndSkipsWriteWhenComplete() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        UUID existing = UUID.randomUUID();
        Files.writeString(file, """
                {"capabilities":[{"id":"%s","capability_type":"test","endpoint":"https://node.eosc-beyond.eu/api/test"}]}
                """.formatted(existing));
        String before = Files.readString(file);
        FileBackedEndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        service.fillMissingIds();

        assertEquals(before, Files.readString(file));
        assertEquals(existing, service.get().getCapabilities().getFirst().getId());
    }

    @Test
    void fillMissingIdsDoesNothingWhenFileMissing() {
        Path file = tempDir.resolve("missing.json");
        FileBackedEndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        service.fillMissingIds();

        assertFalse(Files.exists(file));
    }

    @Test
    void fillMissingIdsDoesNotThrowWhenStoredJsonIsInvalid() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        Files.writeString(file, "{ invalid json");
        FileBackedEndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        assertDoesNotThrow(service::fillMissingIds);
    }

    @Test
    void fillMissingIdsDoesNotThrowWhenWriteFails() throws IOException {
        assumeFalse("root".equals(System.getProperty("user.name")));
        Path directory = Files.createDirectory(tempDir.resolve("readonly"));
        Path file = directory.resolve("capabilities.json");
        writeLegacyCapabilitiesFile(file);
        Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("r-xr-xr-x"));
        FileBackedEndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        try {
            assertDoesNotThrow(service::fillMissingIds);
            assertNotNull(service.get().getCapabilities());
        } finally {
            Files.setPosixFilePermissions(directory, PosixFilePermissions.fromString("rwxr-xr-x"));
        }
    }

    @Test
    void updateAssignsDerivedIdsWhenMissing() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        EndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);

        service.update(payloadWith(capability("test", "https://node.eosc-beyond.eu/api/test")));

        Capability persisted = objectMapper.readValue(file.toFile(), NodeCapabilities.class).getCapabilities().getFirst();
        assertEquals(Capability.deriveId("test", URI.create("https://node.eosc-beyond.eu/api/test")), persisted.getId());
    }

    @Test
    void updatePreservesClientProvidedIds() throws IOException {
        Path file = tempDir.resolve("capabilities.json");
        EndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);
        Capability capability = capability("test", "https://node.eosc-beyond.eu/api/test");
        UUID clientId = UUID.randomUUID();
        capability.setId(clientId);

        service.update(payloadWith(capability));

        Capability persisted = objectMapper.readValue(file.toFile(), NodeCapabilities.class).getCapabilities().getFirst();
        assertEquals(clientId, persisted.getId());
    }

    @Test
    void updateKeepsIdsStableAcrossUpdatesWithoutIds() {
        EndpointService service = new FileBackedEndpointService(tempDir.resolve("capabilities.json").toString(), objectMapper);

        UUID first = service.update(payloadWith(capability("test", "https://node.eosc-beyond.eu/api/test")))
                .getCapabilities().getFirst().getId();
        UUID second = service.update(payloadWith(capability("test", "https://node.eosc-beyond.eu/api/test")))
                .getCapabilities().getFirst().getId();

        assertEquals(first, second);
    }

    @Test
    void updateRejectsDuplicateIds() {
        Path file = tempDir.resolve("capabilities.json");
        EndpointService service = new FileBackedEndpointService(file.toString(), objectMapper);
        NodeCapabilities payload = payloadWith(
                capability("test", "https://node.eosc-beyond.eu/api/test"),
                capability("Test", "https://node.eosc-beyond.eu/api/test"));

        assertThrows(InvalidCapabilitiesException.class, () -> service.update(payload));
        assertFalse(Files.exists(file));
    }

    @Test
    void updateAcceptsNullCapabilityEntries() {
        EndpointService service = new FileBackedEndpointService(tempDir.resolve("capabilities.json").toString(), objectMapper);

        NodeCapabilities result = service.update(payloadWith(null, capability("test", "https://node.eosc-beyond.eu/api/test")));

        assertNotNull(result.getCapabilities().get(1).getId());
    }

    private static Capability capability(String type, String endpoint) {
        Capability capability = new Capability();
        capability.setCapabilityType(type);
        capability.setEndpoint(endpoint);
        return capability;
    }

    private static NodeCapabilities payloadWith(Capability... capabilities) {
        NodeCapabilities payload = new NodeCapabilities();
        payload.setCapabilities(Arrays.asList(capabilities));
        return payload;
    }

    private void writeLegacyCapabilitiesFile(Path file) throws IOException {
        Files.writeString(file, """
                {
                  "capabilities": [
                    {
                      "capability_type": "test",
                      "endpoint": "https://node.eosc-beyond.eu/api/test"
                    }
                  ]
                }
                """);
    }

    private void writeCapabilitiesFile(Path file, String nodeEndpoint) throws IOException {
        Files.writeString(file, """
                {
                  "node_endpoint": "%s"
                }
                """.formatted(nodeEndpoint));
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
