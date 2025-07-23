package gr.uoa.di.madgik.node.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.net.URI;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class EndpointCapabilities {

    private URI nodeEndpoint;
    private List<Capability> capabilities;

    public EndpointCapabilities() {
    }

    public URI getNodeEndpoint() {
        return nodeEndpoint;
    }

    public void setNodeEndpoint(URI nodeEndpoint) {
        this.nodeEndpoint = nodeEndpoint;
    }

    public List<Capability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Capability> capabilities) {
        this.capabilities = capabilities;
    }
}
