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
