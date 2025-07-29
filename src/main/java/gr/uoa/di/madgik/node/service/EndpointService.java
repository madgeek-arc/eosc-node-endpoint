package gr.uoa.di.madgik.node.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.uoa.di.madgik.node.model.EndpointCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;

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
        // TODO: open file (or create the file if not exists) and update its contents.
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
