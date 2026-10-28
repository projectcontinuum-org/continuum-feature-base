# DOX framework

- DOX is highly performant AGENTS.md hierarchy installed here
- Agent must follow DOX instructions across any edits

## Core Contract

- AGENTS.md files are binding work contracts for their subtrees
- Work products, source materials, instructions, records, assets, and durable docs must stay understandable from the nearest applicable AGENTS.md plus every parent AGENTS.md above it

## Read Before Editing

1. Read the root AGENTS.md
2. Identify every file or folder you expect to touch
3. Walk from the repository root to each target path
4. Read every AGENTS.md found along each route
5. If a parent AGENTS.md lists a child AGENTS.md whose scope contains the path, read that child and continue from there
6. Use the nearest AGENTS.md as the local contract and parent docs for repo-wide rules
7. If docs conflict, the closer doc controls local work details, but no child doc may weaken DOX

Do not rely on memory. Re-read the applicable DOX chain in the current session before editing.

## Update After Editing

Every meaningful change requires a DOX pass before the task is done.

Update the closest owning AGENTS.md when a change affects:

- purpose, scope, ownership, or responsibilities
- durable structure, contracts, workflows, or operating rules
- required inputs, outputs, permissions, constraints, side effects, or artifacts
- user preferences about behavior, communication, process, organization, or quality
- AGENTS.md creation, deletion, move, rename, or index contents

Update parent docs when parent-level structure, ownership, workflow, or child index changes. Update child docs when parent changes alter local rules. Remove stale or contradictory text immediately. Small edits that do not change behavior or contracts may leave docs unchanged, but the DOX pass still must happen.

## Hierarchy

- Root AGENTS.md is the DOX rail: project-wide instructions, global preferences, durable workflow rules, and the top-level Child DOX Index
- Child AGENTS.md files own domain-specific instructions and their own Child DOX Index
- Each parent explains what its direct children cover and what stays owned by the parent
- The closer a doc is to the work, the more specific and practical it must be

## Child Doc Shape

- Create a child AGENTS.md when a folder becomes a durable boundary with its own purpose, rules, responsibilities, workflow, materials, or quality standards
- Work Guidance must reflect the current standards of the project or user instructions; if there are no specific standards or instructions yet, leave it empty
- Verification must reflect an existing check; if no verification framework exists yet, leave it empty and update it when one exists

Default section order:
- Purpose
- Ownership
- Local Contracts
- Work Guidance
- Verification
- Child DOX Index

## Style

- Keep docs concise, current, and operational
- Document stable contracts, not diary entries
- Put broad rules in parent docs and concrete details in child docs
- Prefer direct bullets with explicit names
- Do not duplicate rules across many files unless each scope needs a local version
- Delete stale notes instead of explaining history
- Trim obvious statements, repeated rules, misplaced detail, and warnings for risks that no longer exist

## Closeout

1. Re-check changed paths against the DOX chain
2. Update nearest owning docs and any affected parents or children
3. Refresh every affected Child DOX Index
4. Remove stale or contradictory text
5. Run existing verification when relevant
6. Report any docs intentionally left unchanged and why

## User Preferences

When the user requests a durable behavior change, record it here or in the relevant child AGENTS.md

## Project Reference

> Full ecosystem blueprint: see the [Continuum repo AGENTS.md](https://github.com/projectcontinuum-org/continuum-platform-core/blob/main/AGENTS.md) for the complete architecture reference.

### This Repo

16 production-ready analytics and data processing nodes for Continuum workflows. Nodes live in `features/continuum-feature-analytics/` and are served by the Spring Boot worker in `worker/`.

### Prerequisites

- JDK 21
- Docker & Docker Compose

### Commands

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

### Architecture

#### Module Dependency Chain

```
worker  →  features:continuum-feature-analytics  →  continuum-commons
                                                  →  continuum-avro-schemas
worker  →  continuum-worker-springboot-starter
```

The `worker` module is a thin Spring Boot shell (`App.kt` + `application.yml`). It has no node logic — it exists solely to pull in the analytics feature and let `continuum-worker-springboot-starter` auto-discover and register all nodes with Temporal.

#### Node Contract

Every node is a Kotlin class annotated with `@ContinuumNode` that extends `ProcessNodeModel` from `continuum-commons`. The framework scans for these at startup via `AutoConfigure` (`@ComponentScan` on the `analytics` package).

Each node must declare:
- `inputPorts` / `outputPorts` — keyed port maps with name and content type
- `categories` — list of UI category strings
- `metadata` — `ContinuumWorkflowModel.NodeData` containing id (always `this.javaClass.name`), title, description, SVG icon, default property values, and JSON Schema + JSON Forms UI schema for the properties panel
- `override fun execute(properties, inputs, nodeOutputWriter, nodeProgressCallback, ...)` — the processing logic

#### Data Flow in `execute`

Nodes read rows from `inputs["portName"]?.use { reader -> ... reader.read() }` (returns `Map<String, Any>?`, null signals end of stream) and write rows via `nodeOutputWriter.createOutputPortWriter("portName").use { writer -> writer.write(rowIndex, rowMap) }`. Always use `.use {}` on both reader and writer to ensure cleanup. Row indices are `Long` starting from 0.

#### FreeMarker Templating

`CreateTableNodeModel` and `RestNodeModel` both embed a static `freemarkerConfig` in their companion objects. In `RestNodeModel`, row data is passed as `mapOf("row" to row)` so templates reference columns as `${row.columnName}`. In `CreateTableNodeModel`, no data model is passed (templates generate static data).

#### Properties Schema Pattern

Each node embeds its JSON Schema (`propertiesSchema`) and JSON Forms UI schema (`propertiesUiSchema`) as inline JSON strings parsed at construction time via `objectMapper.readValue(...)`. These drive the node's configuration panel in the Workbench UI.

#### Gradle Plugins

Both modules use custom Continuum Gradle plugins:
- `org.projectcontinuum.feature` — for feature library modules (analytics)
- `org.projectcontinuum.worker` — for the Spring Boot worker application

Platform and feature versions are centrally managed in `gradle.properties` (`continuumPlatformVersion`, `featureVersion`). The `continuum { continuumVersion.set(...) }` block in each `build.gradle.kts` wires the platform BOM.

#### Infrastructure (docker-compose)

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

## Child DOX Index

This project is not yet indexed. Before continuing you must scan the project, build the DOX tree and replace this message with the actual index. Go deep and scan files recursively to properly evaluate complexity and create nested DOX files where needed.