# Threat-Intelligence Kill-Chain Correlation Engine

A high-performance Java engine designed for correlating multi-stage cyber attack sequences, reconstructing adversarial kill-chains as directed graphs, conducting choke-point graph analysis, and optimizing sensor monitoring placement using bespoke data structures and algorithms.

---

## 1. Project Purpose

In defensive security operations, isolated log events fail to capture advanced persistent threats (APTs) traversing enterprise networks. This engine ingests raw, untrusted security log streams and processes them through five analytical stages:

```text
RAW SECURITY LOGS
        ↓
1. INGESTION & SIGNATURE MATCHING
   (Log parsing, IoC detection, KMP / Z-Algorithm / Rabin-Karp pattern matching)
        ↓
2. KILL-CHAIN RECONSTRUCTION
   (Multi-attribute temporal & topological correlation into a Directed Attack Graph)
        ↓
3. CHOKE-POINT ANALYSIS
   (Tarjan's Articulation Points DFS, Max-Flow and Min-Cut residual analysis)
        ↓
4. MONITORING PLACEMENT
   (Formal Path Hitting Set sensor optimization with exact attack-path coverage)
        ↓
5. ALERT ROUTING
   (Priority Queue-based alert scheduling via custom Binary Heap)
```

---

## 2. Current Status

**Status: Complete (Phases 1–7 Implemented & Verified — 206/206 Tests Passing)**

At this stage:
* **Phase 1 (Skeleton):** Project structure, build configurations, and verification harnesses established.
* **Phase 2 (Custom Data Structures):** Hand-built `DynamicArray`, `LinkedList`, `Stack`, and `Queue` verified (36/36 tests passing). Zero standard collection usage.
* **Phase 3 (String Algorithms & Ingestion):** First-principles `KMP`, `ZAlgorithm`, `RabinKarp`, `LogParser`, `IOCLoader`, and `SignatureMatcher` verified (59/59 tests passing). Zero library search calls.
* **Phase 4 (Kill-Chain Reconstruction & Graph Traversal):** First-principles `DirectedGraph`, `GraphNode`, `GraphEdge`, iterative `DFS`, level-order `BFS`, `PathFinder`, and domain correlation `KillChain`, `KillChainStage`, `AttackCorrelator` verified (41/41 tests passing).
* **Phase 5 (Choke-Point Analysis & Graph Optimization):** First-principles Edmonds-Karp `MaxFlow`, residual `MinCut` analysis with $(S, T)$ partition and cut edge recovery, and Tarjan-style `ArticulationPoints` on undirected projection verified (29/29 tests passing).
* **Phase 6 (Monitoring Placement):** First-principles 2-Approximation `VertexCover` and `GreedySetCover` incident edge optimization over attack graph projections verified (18/18 tests passing).
* **Phase 7 (Alert Prioritization & Routing):** First-principles array-backed `BinaryHeap` (Max-Heap), 4-tier deterministic `AlertPriority`, `AlertRouter`, and `AlertManager` verified (23/23 tests passing).
* **Total:** 206/206 tests passing across all test suites. No Phase 8 exists.

---

## 3. Core Architectural Restrictions

To guarantee foundational mastery of algorithmic design, the core engine adheres to strict zero-dependency and zero-collection-framework constraints:

### 3.1 Prohibited Inside Core Engine
* **No Java Collection Framework:** Standard classes such as `java.util.ArrayList`, `LinkedList`, `HashMap`, `HashSet`, `PriorityQueue`, `Stack`, `Queue`, `TreeMap`, and `TreeSet` are strictly prohibited within the engine packages.
* **No External Graph or Data Structure Libraries:** JGraphT, Guava, Apache Commons Collections, FastUtil, Eclipse Collections, and NetworkX ports are prohibited.

### 3.2 Required Hand-Built Data Structures
All data structures are implemented manually using primitive arrays and explicit node references:
* **Custom DynamicArray:** Controlled capacity growth with amortized $O(1)$ operations.
* **Custom LinkedList:** Doubly/singly linked node architecture.
* **Custom Stack:** LIFO structure built on manual storage.
* **Custom Queue:** Circular-array FIFO queue with bounded overflow management.
* **Custom BinaryHeap:** Array-backed Min and Max Heap supporting $O(\log n)$ insert/extract and $O(1)$ peek.
* **Custom DirectedGraph:** Adjacency-list directed graph representation built on custom structures.

### 3.3 Algorithms Status
* **String Pattern Matching (Phase 3 - Implemented):** Knuth-Morris-Pratt (KMP), Z-Algorithm, and Rabin-Karp with substring collision verification.
* **Graph Traversal (Phase 4 - Implemented):** Non-recursive Depth-First Search (DFS), Breadth-First Search (BFS), and PathFinder.
* **Choke-Point Analysis (Phase 5 - Implemented):** Edmonds-Karp Max-Flow, residual Min-Cut $(S, T)$ partition, and Tarjan-style Articulation Point detection on undirected connectivity projections.
* **Monitoring Placement (Phase 6 - Implemented):** Deterministic 2-Approximation Vertex Cover and Greedy Set Cover incident edge optimization.
* **Alert Routing (Phase 7 - Implemented):** Array-backed Binary Max-Heap priority queue, multi-factor deterministic Alert Priority evaluation, and Alert Router/Manager.

---

## 4. Building and Running

The project targets **Java 17 LTS**.

### 4.1 Prerequisites
* Java Development Kit (JDK) 17 or higher (`javac` and `java` available on PATH).
* Maven 3.8+ (optional, for standard Maven lifecycle builds).

### 4.2 Building via Standalone Scripts (No Maven Required)
Cross-platform scripts are provided in the `scripts/` directory:

* **Windows (Command Prompt / PowerShell):**
  ```cmd
  scripts\build.bat
  scripts\run.bat
  ```

* **Linux / macOS:**
  ```bash
  chmod +x scripts/build.sh scripts/run.sh
  ./scripts/build.sh
  ./scripts/run.sh
  ```

### 4.3 Building via Maven (When Available)
```bash
mvn clean package
java -jar target/kill-chain-correlation-engine-1.0.0.jar
```

---

## 5. Docker Deployment

A multi-stage `Dockerfile` is provided for containerized compilation and execution. When Docker is installed and running:

```bash
docker build -t threat-intel-engine .
docker run --rm threat-intel-engine
```

*(Note: Docker provides only the execution environment; all algorithmic processing is carried out by the Java engine.)*
