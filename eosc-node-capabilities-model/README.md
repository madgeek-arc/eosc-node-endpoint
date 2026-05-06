# EOSC Node Capabilities Model

Reusable Java model classes for EOSC Node endpoint capability documents.

## Maven

```xml
<dependency>
  <groupId>gr.uoa.di.madgik</groupId>
  <artifactId>eosc-node-capabilities-model</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Contents

The module contains:

| Type | Purpose |
|------|---------|
| `NodeCapabilities` | Full capability document for one node endpoint |
| `Capability` | One advertised endpoint capability |
| `Protocol` | Recommended protocol values |
| `Status` | Recommended operational status values |

Request and response JSON uses `snake_case` through Jackson annotations:

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
