# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> Full ecosystem blueprint: see the [Continuum repo CLAUDE.md](https://github.com/projectcontinuum/Continuum/blob/main/CLAUDE.md) for the complete architecture reference.

## This Repo

16 production-ready analytics and data processing nodes for Continuum workflows. Nodes live in `features/continuum-feature-analytics/` and are served by the Spring Boot worker in `worker/`.

## Prerequisites

- JDK 21
- Docker & Docker Compose
- GitHub PAT with `read:packages` scope set as `GITHUB_USERNAME` and `GITHUB_TOKEN` env vars (for GitHub Packages dependencies)

## Commands

```bash
# Start local infrastructure (Temporal, Kafka, MinIO, API server, Workbench)
cd docker && docker compose up -d

# Build all modules
./gradlew build

# Run the worker
./gradlew :worker:bootRun

# Run all tests
./gradlew test

# Run tests for a single module
./gradlew :features:continuum-feature-analytics:test

# Run a specific test class
./gradlew :features:continuum-feature-analytics:test --tests "org.projectcontinuum.feature.base.analytics.node.CreateTableNodeModelTest"
```

## Architecture

### Module Dependency Chain

```
worker  →  features:continuum-feature-analytics  →  continuum-commons (GitHub Packages)
                                                  →  continuum-avro-schemas (GitHub Packages)
worker  →  continuum-worker-springboot-starter (GitHub Packages)
```

The `worker` module is a thin Spring Boot shell (`App.kt` + `application.yml`). It has no node logic — it exists solely to pull in the analytics feature and let `continuum-worker-springboot-starter` auto-discover and register all nodes with Temporal.

### Node Contract

Every node is a Kotlin class annotated with `@ContinuumNode` that extends `ProcessNodeModel` from `continuum-commons`. The framework scans for these at startup via `AutoConfigure` (`@ComponentScan` on the `analytics` package).

Each node must declare:
- `inputPorts` / `outputPorts` — keyed port maps with name and content type
- `categories` — list of UI category strings
- `metadata` — `ContinuumWorkflowModel.NodeData` containing id (always `this.javaClass.name`), title, description, SVG icon, default property values, and JSON Schema + JSON Forms UI schema for the properties panel
- `override fun execute(properties, inputs, nodeOutputWriter, nodeProgressCallback, ...)` — the processing logic

### Data Flow in `execute`

Nodes read rows from `inputs["portName"]?.use { reader -> ... reader.read() }` (returns `Map<String, Any>?`, null signals end of stream) and write rows via `nodeOutputWriter.createOutputPortWriter("portName").use { writer -> writer.write(rowIndex, rowMap) }`. Always use `.use {}` on both reader and writer to ensure cleanup. Row indices are `Long` starting from 0.

### FreeMarker Templating

`CreateTableNodeModel` and `RestNodeModel` both embed a static `freemarkerConfig` in their companion objects. In `RestNodeModel`, row data is passed as `mapOf("row" to row)` so templates reference columns as `${row.columnName}`. In `CreateTableNodeModel`, no data model is passed (templates generate static data).

### Properties Schema Pattern

Each node embeds its JSON Schema (`propertiesSchema`) and JSON Forms UI schema (`propertiesUiSchema`) as inline JSON strings parsed at construction time via `objectMapper.readValue(...)`. These drive the node's configuration panel in the Workbench UI.

### Gradle Plugins

Both modules use custom Continuum Gradle plugins:
- `org.projectcontinuum.feature` — for feature library modules (analytics)
- `org.projectcontinuum.worker` — for the Spring Boot worker application

Platform and feature versions are centrally managed in `gradle.properties` (`continuumPlatformVersion`, `featureVersion`). The `continuum { continuumVersion.set(...) }` block in each `build.gradle.kts` wires the platform BOM.

### Infrastructure (docker-compose)

| Service | Port | Purpose |
|---------|------|---------|
| Temporal | 7233 | Workflow orchestration |
| Temporal UI | 38081 | Temporal dashboard |
| Kafka (3-node KRaft) | 39092–39094 | Event streaming |
| Schema Registry | 38080 | Confluent Avro schemas |
| Kafka UI | 38082 | Kafka dashboard |
| MinIO | 39000/39001 | S3-compatible object storage |
| continuum-api-server | 8080 | Platform API |
| continuum-message-bridge | 8082 | Kafka↔MQTT bridge |
| continuum-workbench | 3002 | Browser workflow editor |
| Mosquitto (MQTT) | 31883/31884 | MQTT broker |

The worker connects to Temporal at `localhost:7233` (configurable via `TEMPORAL_SERVER_ADDRESS` and `TEMPORAL_NAMESPACE` env vars — see `application.yml`).
