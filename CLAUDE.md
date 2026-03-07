# Continuum Feature Base — Analytics Nodes

> Full ecosystem blueprint: see the [Continuum repo CLAUDE.md](https://github.com/projectcontinuum/Continuum/blob/main/CLAUDE.md) for the complete architecture reference.

## This Repo

16 production-ready analytics and data processing nodes for Continuum workflows.

## Modules

- `features/continuum-feature-analytics/` — Node implementations (group: `com.continuum.base`)
- `worker/` — Spring Boot worker app (group: `com.continuum.feature.base`)

## Nodes

All in `features/continuum-feature-analytics/src/main/kotlin/com/continuum/feature/analytics/node/`:

| Node | Title | Category |
|------|-------|----------|
| `CreateTableNodeModel` | Create Table | Table & Data Structures |
| `ColumnJoinNodeModel` | Column Join Node | Processing |
| `JointNodeModel` | Joint Node | Processing |
| `JoinOnMultipleKeysNodeModel` | Join on Multiple Keys | Join & Merge |
| `PivotColumnsNodeModel` | Pivot Columns | Transform |
| `JsonExploderNodeModel` | JSON Exploder | JSON & Data Parsing |
| `SplitNodeModel` | Column Splitter | Processing |
| `KotlinScriptNodeModel` | Kotlin Script | Transform |
| `DynamicRowFilterNodeModel` | Dynamic Row Filter | Filter & Select |
| `ConditionalSplitterNodeModel` | Conditional Splitter | Flow Control |
| `TimeWindowAggregatorNodeModel` | Time Window Aggregator | Aggregation & Time Series |
| `BatchAccumulatorNodeModel` | Batch Accumulator | Aggregation & Grouping |
| `TextNormalizerNodeModel` | Text Normalizer | String & Text |
| `CryptoHasherNodeModel` | Crypto Hasher | Security & Encryption |
| `AnomalyDetectorZScoreNodeModel` | Anomaly Detector | Analysis & Statistics |
| `RestNodeModel` | REST Client | Integration & API |

## Dependencies (from GitHub Packages)

`continuum-commons:0.0.1`, `continuum-avro-schemas:0.0.1`, `continuum-worker-springboot-starter:0.0.1`

## Build

```bash
cd docker && docker compose up -d
./gradlew build
./gradlew :worker:bootRun
```

## Stack

Kotlin 2.1.0, Spring Boot 3.4.1, JDK 21, Kafka + Confluent Avro, Temporal SDK, AWS SDK, MQTT Paho, FreeMarker, Kotlin Scripting
