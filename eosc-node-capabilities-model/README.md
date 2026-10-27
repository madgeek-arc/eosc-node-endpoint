# EOSC Node Capabilities Model

Reusable Java model classes for EOSC Node endpoint capability documents.

## Maven

<!-- x-release-please-start-version -->
```xml
<dependency>
  <groupId>gr.uoa.di.madgik</groupId>
  <artifactId>eosc-node-capabilities-model</artifactId>
  <version>1.1.1-SNAPSHOT</version>
</dependency>
```
<!-- x-release-please-end -->

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
      "id": "28343877-6f4f-55f7-9322-15e77ba78da7",
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

`id` is a UUID. When it is absent, `Capability.deriveId(capabilityType, endpoint)` provides a deterministic version 5 UUID from the capability type (trimmed, case-insensitive) and the normalized endpoint, so the same capability always gets the same id. An existing id is never replaced.

`protocol` and `status` are extensible string fields. Known values are canonicalized, and unknown values are accepted and normalized to uppercase.

Recommended protocols: `REST`, `SOAP`, `gRPC`, `SSE`, `WebSocket`, `RSocket`.

Recommended statuses: `OPERATIONAL`, `MAINTENANCE`, `UNAVAILABLE`.
