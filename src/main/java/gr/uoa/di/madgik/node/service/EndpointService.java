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

import java.io.*;

@Service
public class EndpointService {

    private static final Logger logger = LoggerFactory.getLogger(EndpointService.class);
    private final String capabilitiesPath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EndpointService(@Value("${capabilities.filepath}") String capabilitiesPath) {
        this.capabilitiesPath = capabilitiesPath;
    }

    public EndpointCapabilities get() {
        return readFile(capabilitiesPath);
    }

    public EndpointCapabilities update(EndpointCapabilities capabilities) {
        return writeFile(capabilitiesPath, capabilities);
    }

    private EndpointCapabilities readFile(String filename) {
        try(InputStream stream = new FileInputStream(filename)) {
            return objectMapper.readValue(stream, EndpointCapabilities.class);
        } catch (Exception e) {
            logger.warn("Could not read capabilities file", e);
            return new EndpointCapabilities();
        }
    }

    private EndpointCapabilities writeFile(String filename, EndpointCapabilities capabilities) {
        File file = new File(filename);
        try {
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            try (OutputStream stream = new FileOutputStream(file)) {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(stream, capabilities);
            }
            logger.info("Successfully wrote capabilities file to {}", filename);
            return capabilities;
        } catch (Exception e) {
            logger.warn("Could not write capabilities file", e);
            return new EndpointCapabilities();
        }
    }
}
