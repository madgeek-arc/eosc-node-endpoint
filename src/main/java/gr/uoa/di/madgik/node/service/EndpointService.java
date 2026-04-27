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
import gr.uoa.di.madgik.node.model.EndpointCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class EndpointService {

    private static final Logger logger = LoggerFactory.getLogger(EndpointService.class);
    private final Path capabilitiesPath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private EndpointCapabilities capabilities;

    public EndpointService(@Value("${capabilities.filepath}") String capabilitiesPath) {
        this.capabilitiesPath = Path.of(capabilitiesPath);
    }

    public EndpointCapabilities get() throws IOException {
        if (capabilities == null) {
            capabilities = readFile();
        }
        return capabilities;
    }

    public EndpointCapabilities update(EndpointCapabilities capabilities) throws IOException {
        this.capabilities = capabilities;
        return writeFile(this.capabilities);
    }

    private EndpointCapabilities readFile() throws IOException {
        return objectMapper.readValue(capabilitiesPath.toFile(), EndpointCapabilities.class);
    }

    private EndpointCapabilities writeFile(EndpointCapabilities capabilities) throws IOException {
        Path parentDir = capabilitiesPath.getParent();
        if (parentDir != null) {
            Files.createDirectories(parentDir);
        }
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(capabilitiesPath.toFile(), capabilities);
        logger.info("Successfully wrote capabilities file to {}", capabilitiesPath);
        return capabilities;
    }
}
