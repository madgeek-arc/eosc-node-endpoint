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
java -jar eosc-node-endpoint-<version>.jar --capabilities.filepath=/path/to/capabilities.json
```

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

* `capabilities.filepath`: path to the JSON storage file.
* `capabilities.cache.ttl`: cache TTL for loaded file contents. Default: `PT60S`.
* `server.port`: HTTP port. Default: `8080`.

```bash
java -jar eosc-node-endpoint-<version>.jar \
  --capabilities.filepath=/path/to/capabilities.json \
  --capabilities.cache.ttl=PT60S \
  --server.port=9090
```

Manual edits to `capabilities.json` are picked up after the cache TTL expires.
The TTL uses Spring Boot duration syntax, for example `PT60S`, `PT5M`, or `1m`.

## API

* `GET /api/endpoint` returns the currently stored capability document.
* `PUT /api/endpoint` replaces the stored capability document and returns the saved payload.

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