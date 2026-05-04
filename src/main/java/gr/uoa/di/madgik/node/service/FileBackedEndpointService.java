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

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.node.exception.ReadCapabilitiesException;
import gr.uoa.di.madgik.node.exception.WriteCapabilitiesException;
import gr.uoa.di.madgik.node.model.EndpointCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class FileBackedEndpointService implements EndpointService {

    private static final Logger logger = LoggerFactory.getLogger(FileBackedEndpointService.class);
    private final Path capabilitiesPath;
    private final Duration cacheTtl;
    private final Clock clock;
    private final ObjectMapper objectMapper;

    private EndpointCapabilities cachedCapabilities;
    private Instant cachedAt;

    @Autowired
    public FileBackedEndpointService(
            @Value("${capabilities.filepath}") String capabilitiesPath,
            @Value("${capabilities.cache.ttl:PT60S}") Duration cacheTtl,
            ObjectMapper objectMapper) {
        this(capabilitiesPath, cacheTtl, objectMapper, Clock.systemUTC());
    }

    public FileBackedEndpointService(String capabilitiesPath, ObjectMapper objectMapper) {
        this(capabilitiesPath, Duration.ofSeconds(60), objectMapper, Clock.systemUTC());
    }

    FileBackedEndpointService(String capabilitiesPath, Duration cacheTtl, ObjectMapper objectMapper, Clock clock) {
        this.capabilitiesPath = Path.of(capabilitiesPath);
        this.cacheTtl = cacheTtl;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public synchronized EndpointCapabilities get() {
        if (cachedCapabilities == null || cacheExpired()) {
            cachedCapabilities = readFile();
            cachedAt = clock.instant();
        }
        return cachedCapabilities;
    }

    public synchronized EndpointCapabilities update(EndpointCapabilities capabilities) {
        EndpointCapabilities writtenCapabilities = writeFile(capabilities);
        cachedCapabilities = writtenCapabilities;
        cachedAt = clock.instant();
        return writtenCapabilities;
    }

    private boolean cacheExpired() {
        return cachedAt == null || !clock.instant().isBefore(cachedAt.plus(cacheTtl));
    }

    private EndpointCapabilities readFile() {
        // A missing file is expected on first startup before any capabilities have been registered.
        if (Files.notExists(capabilitiesPath)) {
            logger.info("Capabilities file does not exist yet at {}", capabilitiesPath);
            return new EndpointCapabilities();
        }

        try {
            return objectMapper.readValue(capabilitiesPath.toFile(), EndpointCapabilities.class);
        } catch (JacksonException e) {
            throw new ReadCapabilitiesException("Could not read capabilities file: " + capabilitiesPath, e);
        }
    }

    private EndpointCapabilities writeFile(EndpointCapabilities capabilities) {
        Path tempFile = null;

        try {
            tempFile = createTempFileInTargetDirectory();

            try (OutputStream stream = Files.newOutputStream(tempFile)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(stream, capabilities);
            }

            moveIntoPlace(tempFile, capabilitiesPath);
            logger.info("Successfully wrote capabilities file to {}", capabilitiesPath);
            return capabilities;
        } catch (IOException e) {
            throw new WriteCapabilitiesException("Could not write capabilities file: " + capabilitiesPath, e);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    private Path createTempFileInTargetDirectory() throws IOException {
        Path targetDirectory = capabilitiesPath.toAbsolutePath().getParent();

        if (targetDirectory == null) {
            return Files.createTempFile("capabilities-", ".json");
        }

        Files.createDirectories(targetDirectory);
        return Files.createTempFile(targetDirectory, "capabilities-", ".json");
    }

    private void moveIntoPlace(Path source, Path target) throws IOException {
        try {
            // Replace the target only after the full JSON payload has been written successfully.
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            // Fall back to a regular replace on filesystems that do not support atomic moves.
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void deleteTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }

        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            logger.warn("Could not delete temporary capabilities file {}", tempFile, e);
        }
    }
}
