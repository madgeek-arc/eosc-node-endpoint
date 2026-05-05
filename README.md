[![EOSC Beyond Logo][eosc-logo]]()

# Node Endpoint Service (Back-End)

## Overview

The **Node Endpoint Service** helps you register and update your node's capabilities.
Capabilities are stored in a `capabilities.json` file at your chosen location and the file is created automatically on first update.

## Prerequisites

* Java 21
* Maven 3.9+

## Build

Clone the repository and build the project:

```bash
git clone https://github.com/madgeek-arc/eosc-node-endpoint.git
cd eosc-node-endpoint
./mvnw clean install
```

## Run

```bash
java -jar eosc-node-endpoint-<version>.jar \
  --spring.config.additional-location=file:/path/to/application.yaml
```

See [Configuration](#configuration) for the required properties and an example config file.

## Docker

```bash
make docker-build
make docker-compose
```

The image name is derived from the Maven project version.
`make docker-build` builds a native image with the Paketo health-checker buildpack.
The Compose setup in [compose/docker-compose.yml](compose/docker-compose.yml) exposes the service on `127.0.0.1:8888`, loads [compose/config/application.properties](compose/config/application.properties), and runs the container as the current host UID/GID.

To stop it:

```bash
make docker-compose-down
```

## Configuration

Common runtime properties:

| Property | Description | Default |
|----------|-------------|---------|
| `capabilities.filepath` | Path to the JSON storage file | — |
| `capabilities.cache.ttl` | Cache TTL for loaded file contents (Spring duration syntax) | `PT60S` |
| `server.port` | HTTP port | `8080` |
| `server.servlet.session.cookie.name` | Name of the HTTP session cookie used by the OAuth2 login flow | `NE_SESSION` |
| `server.servlet.session.cookie.path` | Cookie path | `/` |
| `security.admin-emails` | Comma-separated list of email addresses granted admin access | — |

Because the OAuth2 properties contain secrets, supply them via an external config file rather than inline flags:

```bash
java -jar eosc-node-endpoint-<version>.jar \
  --spring.config.additional-location=file:/path/to/application.yaml
```

Use [`src/main/resources/application.yaml`](src/main/resources/application.yaml) as a starting point. A minimal deployment file looks like:

```yaml
capabilities:
  filepath: /path/to/capabilities.json

security:
  admin-emails: user@example.com,other@example.com

spring:
  security:
    oauth2:
      client:
        provider:
          eosc:
            issuer-uri: https://core-proxy.node.eosc-beyond.eu/auth/realms/core
        registration:
          eosc:
            client-id: my-client-id
            client-secret: my-client-secret
```

`--spring.config.additional-location` merges the external file on top of the bundled defaults, so only the properties that differ need to be set.

Manual edits to `capabilities.json` are picked up after the cache TTL expires.
The TTL uses Spring Boot duration syntax, for example `PT60S`, `PT5M`, or `1m`.

### OAuth2 / EOSC AAI properties

Required when using the OAuth2 browser login flow:

| Property | Description                                                                     |
|----------|---------------------------------------------------------------------------------|
| `spring.security.oauth2.client.provider.eosc.issuer-uri` | EOSC AAI issuer URI (used to discover OIDC endpoints)                           |
| `spring.security.oauth2.client.registration.eosc.client-id` | OAuth2 client ID                                                                |
| `spring.security.oauth2.client.registration.eosc.client-secret` | OAuth2 client secret                                                            |
| `spring.security.oauth2.client.registration.eosc.client-name` | Display name for the login button (default: `EOSC`)                             |
| `spring.security.oauth2.client.registration.eosc.scope` | Requested scopes (default: `openid`, `email`, `profile`, `entitlements`) |
| `spring.security.oauth2.resourceserver.jwt.issuer-uri` | Issuer URI for JWT bearer token validation                                      |

## Authentication

The service uses EOSC AAI as its identity provider and supports two authentication flows:

### OAuth2 login (browser flow)

Navigating to a protected endpoint redirects the browser to the EOSC AAI login page. After a successful login the service creates a server-side session identified by the `NE_SESSION` cookie (configurable via `server.servlet.session.cookie.name`).

This flow requires the `spring.security.oauth2.client.*` properties to be set (see [OAuth2 / EOSC AAI properties](#oauth2--eosc-aai-properties)).

### Bearer token (API / machine access)

Obtain an access token from the EOSC AAI token endpoint and pass it in the `Authorization` header:

```bash
curl -X PUT http://localhost:8888/api/endpoint \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '...'
```

The service validates the token against the EOSC AAI JWKS using the issuer URI configured in `spring.security.oauth2.resourceserver.jwt.issuer-uri`. No session is created for this flow.

An unauthenticated or unauthorized request receives `401 Unauthorized` or `403 Forbidden` respectively.

The `ADMIN` authority is currently granted by matching the authenticated user's email against the `security.admin-emails` configuration property. This is a temporary mechanism — it will likely be replaced by a dedicated Keycloak role in a future version.

## API

| Method | Endpoint | Auth required | Description |
|--------|----------|---------------|-------------|
| `GET` | `/api/endpoint` | No | Returns the currently stored capability document |
| `PUT` | `/api/endpoint` | Yes — `ADMIN` role | Replaces the stored capability document and returns the saved payload |

Request and response bodies use `snake_case` JSON:

```json
{
  "node_endpoint": "https://node.eosc-beyond.eu",
  "capabilities": [
    {
      "capability_type": "metadata",
      "endpoint": "https://node.eosc-beyond.eu/api/metadata",
      "version": "1.0.0",
      "api_spec": "https://node.eosc-beyond.eu/api/openapi.json",
      "protocol": "REST",
      "status": "OPERATIONAL"
    }
  ]
}
```

`protocol` and `status` are extensible string fields. Known values are canonicalized, and unknown values are accepted and normalized to uppercase.
Recommended protocols: `REST`, `SOAP`, `gRPC`, `SSE`, `WebSocket`, `RSocket`.
Recommended statuses: `OPERATIONAL`, `MAINTENANCE`, `UNAVAILABLE`.

### Example Update

```bash
curl -X PUT http://localhost:8888/api/endpoint \
  -H "Authorization: Bearer <access_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "node_endpoint": "https://node.eosc-beyond.eu",
    "capabilities": [
      {
        "capability_type": "metadata",
        "endpoint": "https://node.eosc-beyond.eu/api/metadata",
        "version": "1.0.0",
        "api_spec": "https://node.eosc-beyond.eu/api/openapi.json",
        "protocol": "REST",
        "status": "OPERATIONAL"
      },
      {
        "capability_type": "custom-service",
        "endpoint": "https://node.eosc-beyond.eu/custom",
        "protocol": "REST",
        "status": "UNAVAILABLE"
      }
    ]
  }'
```

[eosc-logo]: https://eosc.eu/wp-content/uploads/2024/02/EOSC-Beyond-logo.png