<div align="center">
  <h1>Continuum Feature Base</h1>
  <strong>Core analytics and data processing nodes for <a href="https://github.com/projectcontinuum/Continuum">Project Continuum</a></strong>
</div>

<div align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.1.0-blue?logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Spring_Boot-3.4-green?logo=springboot&logoColor=white" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Nodes-16-orange" alt="16 Nodes">
  <img src="https://img.shields.io/badge/JDK-21-red" alt="JDK 21">
</div>

---

## 🌐 Part of Project Continuum

This is the **base analytics feature repository** for [Project Continuum](https://github.com/projectcontinuum/Continuum) — a distributed, crash-proof workflow execution platform. It provides the foundational set of data processing nodes used in most workflows.

---

## 🔥 What Is This

A standalone Gradle project containing **16 production-ready workflow nodes** for data transformation, filtering, aggregation, REST integration, scripting, and anomaly detection. Ships as a Spring Boot worker that registers all nodes with Temporal for durable execution.

---

## 🧩 Included Nodes

| Category | Node | Description |
|----------|------|-------------|
| **Data** | Create Table | Creates a structured table from FreeMarker template configuration |
| **Data** | Column Join | Joins two columns from left and right tables into one output column |
| **Data** | Joint Node | Joins input strings into one |
| **Data** | Join on Multiple Keys | Performs inner join on two tables using two key columns from each table |
| **Transform** | Pivot Columns | Pivots table so pivot column values become new columns with value column as cell values |
| **Transform** | JSON Exploder | Parses JSON strings and flattens keys into new columns |
| **Transform** | Column Splitter | Splits a column into two parts |
| **Transform** | Kotlin Script | Runs a Kotlin script for each row, adding a script_result column |
| **Filter** | Dynamic Row Filter | Filters rows where the specified column value is greater than the threshold |
| **Filter** | Conditional Splitter | Splits rows into two outputs based on threshold comparison |
| **Aggregation** | Time Window Aggregator | Aggregates values into time windows, summing by window buckets |
| **Aggregation** | Batch Accumulator | Groups rows into batches and adds batch_id and row_count columns |
| **Text** | Text Normalizer | Normalizes text by trimming, lowercasing, and removing non-alphanumeric characters |
| **Security** | Crypto Hasher | Generates SHA-256 hash of column values |
| **Analytics** | Anomaly Detector (Z-Score) | Detects outliers using Z-score method (flags values with \|Z\| > 2) |
| **Integration** | REST Client | Makes HTTP requests for each row using FreeMarker templated URLs and payloads |

---

## 📦 Dependencies

This worker depends on shared libraries published from the [Continuum](https://github.com/projectcontinuum/Continuum) monorepo via GitHub Packages:

| Dependency | Purpose |
|-----------|---------|
| `continuum-commons:0.0.1` | Base node model, data types, Parquet/S3 utilities |
| `continuum-avro-schemas:0.0.1` | Shared Kafka message schemas |
| `continuum-worker-springboot-starter:0.0.1` | Worker framework — registers nodes with Temporal |

Additional infrastructure libraries: Kafka + Confluent Avro, Temporal SDK, AWS SDK (S3), MQTT Paho, FreeMarker, Kotlin Scripting.

---

## 🚀 Quick Start

### Prerequisites

- **JDK 21** — [Eclipse Temurin](https://adoptium.net/) recommended
- **Docker & Docker Compose** — For local infrastructure
- **GitHub PAT** with `read:packages` scope

Set environment variables:

```bash
export GITHUB_USERNAME=your-github-username
export GITHUB_TOKEN=ghp_your-personal-access-token
```

### Run

```bash
# Start infrastructure (Temporal, Kafka, MinIO, API Server, Message Bridge)
cd docker && docker compose up -d

# Build
./gradlew build

# Start the base analytics worker
./gradlew :worker:bootRun
```

Your worker is now running and all 16 nodes are registered with Temporal.

---

## 📁 Project Structure

```
continuum-feature-base/
├── features/
│   └── continuum-feature-analytics/          # 16 analytics nodes
│       ├── build.gradle.kts                  # Depends on continuum-commons + avro-schemas
│       └── src/main/kotlin/.../node/
│           ├── CreateTableNodeModel.kt
│           ├── ColumnJoinNodeModel.kt
│           ├── JointNodeModel.kt
│           ├── JoinOnMultipleKeysNodeModel.kt
│           ├── PivotColumnsNodeModel.kt
│           ├── JsonExploderNodeModel.kt
│           ├── SplitNodeModel.kt
│           ├── KotlinScriptNodeModel.kt
│           ├── DynamicRowFilterNodeModel.kt
│           ├── ConditionalSplitterNodeModel.kt
│           ├── TimeWindowAggregatorNodeModel.kt
│           ├── BatchAccumulatorNodeModel.kt
│           ├── TextNormalizerNodeModel.kt
│           ├── CryptoHasherNodeModel.kt
│           ├── AnomalyDetectorZScoreNodeModel.kt
│           └── RestNodeModel.kt
├── worker/                                   # Spring Boot worker application
│   ├── build.gradle.kts                      # Depends on starter + analytics feature
│   └── src/main/
│       ├── kotlin/.../App.kt
│       └── resources/application.yaml
├── docker/                                   # Local development infrastructure
│   └── docker-compose.yml
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## 🔗 Related Repositories

| Repository | Description |
|-----------|-------------|
| [Continuum](https://github.com/projectcontinuum/Continuum) | Core backend — API server, worker framework, shared libraries |
| [continuum-workbench](https://github.com/projectcontinuum/continuum-workbench) | Browser IDE — Eclipse Theia + React Flow workflow editor |
| **continuum-feature-base** (this repo) | Base analytics nodes — data transforms, REST, scripting, anomaly detection |
| [continuum-feature-ai](https://github.com/projectcontinuum/continuum-feature-ai) | AI/ML nodes — LLM fine-tuning with Unsloth + LoRA |
| [continuum-feature-template](https://github.com/projectcontinuum/continuum-feature-template) | Template — scaffold your own custom worker with nodes |
