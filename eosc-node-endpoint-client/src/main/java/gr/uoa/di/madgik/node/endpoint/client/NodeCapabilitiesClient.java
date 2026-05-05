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

package gr.uoa.di.madgik.node.endpoint.client;

import gr.uoa.di.madgik.node.capabilities.model.NodeCapabilities;

/**
 * Client-side contract for the endpoint capabilities API.
 */
public interface NodeCapabilitiesClient {

    /**
     * Fetches the currently registered node capabilities from a remote endpoint service.
     *
     * @return the current capability document
     */
    NodeCapabilities get();

    /**
     * Replaces the remote endpoint capabilities using an access token for this request.
     *
     * @param capabilities capability document to store
     * @param accessToken access token value without the {@code Bearer } prefix
     * @return the stored capability document returned by the remote endpoint service
     */
    NodeCapabilities update(NodeCapabilities capabilities, String accessToken);
}
