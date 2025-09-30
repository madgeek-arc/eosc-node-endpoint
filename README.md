<div align="center">
  <img src='https://eosc.eu/wp-content/uploads/2024/02/EOSC-Beyond-logo.png'></img>
</div>

# Node Endpoints Service (Back-End)

## Description

The **Node Endpoints Service** helps you register and update your node's capabilities.
All information is stored in a simple `capabilities.json` file at your chosen location.

You do **not** need to pre-create this file — it will be created automatically the first time you register capabilities.

---

## Prerequisites

* Java 21
* Maven 3.9+

---

## Installation

Clone the repository and build the project:

```bash
git clone https://github.com/madgeek-arc/eosc-node-endpoint.git
cd eosc-node-endpoint
mvn clean install
```

After a successful build, a `/target` folder will be generated.

---

## Running the Service

From the `/target` folder, run the application with:

```bash
java -jar eosc-node-endpoint-0.0.1-SNAPSHOT.jar --capabilities.filepath=/path/to/capabilities.json
```

* Replace `/path/to/capabilities.json` with your desired location.
* If the file does not exist, it will be created automatically.

---

## Configuration

You can override any Spring Boot property at runtime. For example, to run the service on port `9090`:

```bash
java -jar eosc-node-endpoint-0.0.1-SNAPSHOT.jar \
  --capabilities.filepath=/path/to/capabilities.json \
  --server.port=9090
```
