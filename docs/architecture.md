# System Architecture

> **Notice:** This document defines the system architecture for the Threat-Intelligence Kill-Chain Correlation Engine. Phases 1–7 (Project Skeleton, Custom Data Structures, String Matching & Ingestion, Kill-Chain Reconstruction / Directed Attack Graph, Choke-Point Analysis, Monitoring Placement, and Alert Prioritization & Routing) are fully implemented and verified.

---

## 1. High-Level Pipeline Architecture

The engine processes raw, untrusted security log feeds through five sequential analytical stages:

```text
               RAW LOGS & IoC DEFINITIONS
                           ↓
        [ Stage 1: Ingestion & Signature Matching ] (Phase 3 - Implemented)
          - Custom LogParser & IOCLoader
          - String Algorithms: KMP, Z-Algorithm, Rabin-Karp
          - Output: Structured LogEntry, SignatureMatch
                           ↓
        [ Stage 2: Kill-Chain Reconstruction ] (Phase 4 - Implemented)
          - Custom AttackCorrelator
          - Multi-attribute event correlation (Time, Hosts, Stage, IoC)
          - Output: DirectedGraph (AttackNodes, AttackEdges)
                           ↓
        [ Stage 3: Choke-Point Analysis ] (Phase 5 - Implemented)
          - Tarjan-style ArticulationPoints DFS (undirected projection)
          - Flow Network: FlowNetwork, FlowEdge, MaxFlow & MinCut (residual analysis)
          - Output: Critical cut vertices and min-cut partitions/edges
                           ↓
        [ Stage 4: Monitoring Placement ] (Phase 6 - Implemented)
          - Deterministic 2-Approximation Vertex Cover
          - Greedy Set Cover incident edge optimization
          - Output: MonitoringPoint locations, exact edge coverage metrics
                           ↓
        [ Stage 5: Alert Prioritization & Routing ] (Phase 7 - Implemented)
          - Custom BinaryHeap Priority Queue (Max-Heap)
          - Multi-factor deterministic priority score (Severity, Depth, Sequence)
          - Output: Priority-ordered security alerts via AlertRouter & AlertManager
```

---

## 2. Layered Component Boundaries

```text
+-----------------------------------------------------------------------+
|                             engine.Main                               |
+-----------------------------------------------------------------------+
|                             engine.core                               |
|              - Engine (Orchestrator)                                  |
|              - EngineConfig (Parameters & Tunables)                   |
+-----------------------------------------------------------------------+
| engine.ingestion        | engine.strings        | engine.correlation  |
| - LogParser             | - KMP                 | - AttackCorrelator  |
| - IOCLoader             | - ZAlgorithm          | - KillChain         |
| - SignatureMatcher      | - RabinKarp           | - KillChainStage    |
+-------------------------+-----------------------+---------------------+
| engine.graph            | engine.optimization   | engine.monitoring   |
| - DirectedGraph         | - FlowNetwork/FlowEdge| - MonitoringPlacement
| - DFS & BFS             | - MaxFlow / MinCut    | - MonitoringPoint   |
| - PathFinder            | - ArticulationPoints  | - MonitoringResult  |
+-------------------------+-----------------------+---------------------+
| engine.alerting                                                       |
| - Alert, AlertPriority, BinaryHeap, AlertRouter, AlertManager         |
+-----------------------------------------------------------------------+
| engine.models                                                         |
| - LogEntry, IOC, SignatureMatch, AttackNode, AttackEdge               |
+-----------------------------------------------------------------------+
| engine.datastructures (Zero Java Collections / Zero External Libs)    |
| - DynamicArray, LinkedList, Stack, Queue                              |
| - HashMap, HashSet (Polynomial Hash), BinaryHeap                      |
+-----------------------------------------------------------------------+
| engine.utils                                                          |
| - HashFunctions, TimeUtils, InputValidator                            |
+-----------------------------------------------------------------------+
```

---

## 3. Strict Algorithmic Non-Reliance Principles

1. **Zero Standard Collections in Core Engine:** No `java.util.ArrayList`, `LinkedList`, `HashMap`, `HashSet`, `PriorityQueue`, `Stack`, `Queue`, `TreeMap`, or `TreeSet` are permitted inside the engine packages.
2. **Zero External Graph/Collection Frameworks:** No JGraphT, Guava, Apache Commons Collections, FastUtil, or NetworkX ports.
3. **Internal Mechanics:** All data structures are built from primitive arrays (`Object[]`, `int[]`, etc.) and explicit node reference links.
4. **Untrusted Data Boundary:** The ingestion layer (`InputValidator`) treats all log streams and IoC feeds as untrusted input, performing strict length, boundary, and format sanitization prior to ingestion.
