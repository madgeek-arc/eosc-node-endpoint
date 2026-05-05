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

package gr.uoa.di.madgik.node.capabilities.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/**
 * Suggested protocol values for a capability.
 *
 * <p>This enum documents the known and recommended protocol values used by the application.
 * It is not a closed vocabulary for persisted or incoming data. {@link Capability} stores
 * {@code protocol} as a {@link String}, so values outside this enum are still accepted in order
 * to preserve extensibility and compatibility with external producers.</p>
 */
public enum Protocol {
    REST("REST"),
    SOAP("SOAP"),
    GRPC("gRPC"),
    SSE("SSE"),
    WEB_SOCKET("WebSocket"),
    RSOCKET("RSocket");

    private final String value;

    Protocol(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static Optional<Protocol> fromValue(String value) {
        if (value == null) {
            return Optional.empty();
        }

        String normalized = normalize(value);
        return Arrays.stream(values())
                .filter(protocol -> normalize(protocol.value).equals(normalized))
                .findFirst();
    }

    private static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replaceAll("[_\\s-]", "");
    }
}
