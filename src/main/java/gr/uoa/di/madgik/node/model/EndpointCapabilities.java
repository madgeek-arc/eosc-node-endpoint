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

package gr.uoa.di.madgik.node.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.net.URI;
import java.util.List;

/**
 * Represents the capability document exposed by a node endpoint.
 *
 * <p>This model groups the node endpoint identifier with the list of advertised
 * {@link Capability capabilities} available from that endpoint.</p>
 */
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
