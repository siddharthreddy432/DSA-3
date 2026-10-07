# Algorithmic Foundations

> **Notice:** This document specifies the algorithmic specifications and theoretical models intended for the Threat-Intelligence Kill-Chain Engine. Every component below is marked with its current implementation status. No implementation details or benchmark figures are fabricated.

---

## 0. Foundational Data Structures

### 0.1 DynamicArray
* **Implementation Status:** Implemented
* **Package:** `engine.datastructures.DynamicArray<T>`
* **Internal Representation:** Contiguous primitive array (`Object[] data`), tracking `size` and `capacity`.
* **Implemented Operations:**
  * `add(T value)`: Amortized $O(1)$ append; doubles capacity when full.
  * `add(int index, T value)`: $O(n)$ insertion with rightward shifting.
  * `get(int index)`: $O(1)$ random access.
  * `set(int index, T value)`: $O(1)$ in-place update.
  * `remove(int index)`: $O(n)$ removal with leftward shifting; trailing slot set to `null` for GC.
  * `contains(T value)`: $O(n)$ linear scan with null-safe value equality.
  * `size()`, `isEmpty()`: $O(1)$ status queries.
  * `clear()`: $O(n)$ reference nullification.
* **Memory Management:** Initial capacity defaults to 10; dynamic expansion policy doubles capacity on overflow.

### 0.2 LinkedList
* **Implementation Status:** Implemented
* **Package:** `engine.datastructures.LinkedList<T>`
* **Internal Representation:** Custom singly linked `Node<T>` objects maintaining `head`, `tail`, and `size`.
* **Implemented Operations:**
  * `addFirst(T value)`: $O(1)$ insertion at head.
  * `addLast(T value)`: $O(1)$ insertion at tail.
  * `get(int index)`: $O(n)$ sequential node traversal.
  * `set(int index, T value)`: $O(n)$ sequential node traversal.
  * `removeFirst()`: $O(1)$ head unlinking.
  * `removeLast()`: $O(n)$ traversal to second-to-last node for singly linked list.
  * `remove(int index)`: $O(n)$ pointer bypass.
  * `contains(T value)`: $O(n)$ node traversal with null-safe value equality.
  * `size()`, `isEmpty()`: $O(1)$ status queries.
  * `clear()`: $O(n)$ sequential node reference unlinking.

### 0.3 Stack
* **Implementation Status:** Implemented
* **Package:** `engine.datastructures.Stack<T>`
* **Internal Representation:** LIFO adapter backed by custom `DynamicArray<T>`.
* **Implemented Operations:**
  * `push(T value)`: Amortized $O(1)$ append.
  * `pop()`: $O(1)$ removal of top element; throws `IllegalStateException` on empty stack.
  * `peek()`: $O(1)$ inspection of top element; throws `IllegalStateException` on empty stack.
  * `size()`, `isEmpty()`: $O(1)$ status queries.
  * `clear()`: $O(n)$ underlying array reference clearing.

### 0.4 Queue
* **Implementation Status:** Implemented
* **Package:** `engine.datastructures.Queue<T>`
* **Internal Representation:** Circular array buffer (`Object[] data`) with `front` and `rear` pointers using modulo arithmetic `(index + 1) % capacity`.
* **Implemented Operations:**
  * `enqueue(T value)`: Amortized $O(1)$ insertion at rear; triggers dynamic buffer doubling and linearization when full.
  * `dequeue()`: $O(1)$ removal at front without shifting remaining elements; front slot set to `null` for GC; throws `IllegalStateException` on empty queue.
  * `peek()`: $O(1)$ inspection at front; throws `IllegalStateException` on empty queue.
  * `size()`, `isEmpty()`: $O(1)$ status queries.
  * `clear()`: $O(n)$ buffer reference clearing.

---

## 1. String Pattern Matching & IoC Detection (Phase 3)

### 1.1 Knuth-Morris-Pratt (KMP)
* **Implementation Status:** Implemented
* **Package:** `engine.strings.KMP`
* **Purpose:** Exact signature matching against log messages and event payloads using a Longest Proper Prefix which is also Suffix (LPS) array.
* **LPS Construction (`buildLPS`):**
  * Computes the failure function table in $O(m)$ time and $O(m)$ auxiliary space, where $m = \text{pattern.length()}$.
  * For each index $i$, `lps[i]` stores the length of the longest proper prefix of `pattern[0..i]` that is also a suffix of `pattern[0..i]`.
  * Handles repeated prefixes (e.g. `ABABCABAB` $\to$ `[0, 0, 1, 2, 0, 1, 2, 3, 4]`).
* **Search Mechanics (`search`):**
  * Single-pass text scanning without backtracking over the text in $O(n)$ time.
  * When a full match is found at $j = m$, records start offset $i - j$ and shifts pattern to $j = \text{lps}[j - 1]$, enabling full discovery of overlapping matches (e.g. `ABAB` in `ABABAB` at indices 0 and 2).
* **Asymptotics:**
  * Preprocessing: $O(m)$ time, $O(m)$ auxiliary space
  * Search: $O(n)$ time, $O(1)$ auxiliary space
  * Total: $O(n + m)$ time, $O(m)$ auxiliary space.

### 1.2 Z-Algorithm
* **Implementation Status:** Implemented
* **Package:** `engine.strings.ZAlgorithm`
* **Purpose:** Linear-time exact substring search using the fundamental string preprocessing Z-box mechanism.
* **Pattern Z-Array (`buildZ`):**
  * Computes the Z-array for pattern string $P$ in $O(m)$ time and $O(m)$ space.
  * For each index $i \ge 1$, $Z[i]$ represents the length of the longest substring starting from $P[i]$ that matches a prefix of $P$. Standard convention sets $Z[0] = 0$.
  * Maintains a dynamic $[L, R]$ Z-box representing the rightmost segment matching a prefix of $P$.
* **Separator-Free Two-Phase Search (`search`):**
  * Classical Z-search concatenates $P + \$ + T$, which risks delimiter collision if $\$$ appears in log payloads.
  * Our implementation eliminates separators entirely: it preprocesses $P$ into $Z_P$, and dynamically maintains an $[L, R]$ Z-box window over text $T$ matching prefixes of $P$.
  * If $i \le R$:
    * Let $k = i - L$, $\text{rem} = R - i + 1$.
    * If $Z_P[k] < \text{rem}$, match length is $Z_P[k] < m$.
    * If $Z_P[k] \ge \text{rem}$, text matches $P$ for at least $\text{rem}$ characters; extends comparisons beyond $R$ character-by-character and updates $[L, R]$.
  * If $i > R$: compares characters directly from index 0 of $P$, establishing a new $[L, R]$ box.
* **Asymptotics:**
  * Preprocessing: $O(m)$ time, $O(m)$ auxiliary space
  * Search: $O(n)$ time, $O(1)$ auxiliary space
  * Total: $O(n + m)$ time, $O(m)$ space.

### 1.3 Rabin-Karp
* **Implementation Status:** Implemented
* **Package:** `engine.strings.RabinKarp`
* **Purpose:** Polynomial rolling hash sliding window pattern search for signature detection.
* **Rolling Hash Mechanics:**
  * Base radix $d = 256$ (extended ASCII / byte character space).
  * Prime modulus $q = 1000000007L$ ($10^9 + 7$), preventing 64-bit integer overflow during $((\text{hash} \times 256) + c) \pmod q$.
  * Leading term factor: $h = d^{m-1} \pmod q$.
  * Window update from index $i$ to $i+1$:
    $$\text{hash}_{i+1} = \left(d \cdot \left(\text{hash}_i - T[i] \cdot h \pmod q + q\right) + T[i+m]\right) \pmod q$$
* **Mandatory Collision Verification:**
  * A matching hash is *not* assumed to be a matching string.
  * When $\text{patternHash} == \text{windowHash}$, an explicit character-by-character comparison $T[i+j] == P[j]$ for $j \in [0, m-1]$ is performed.
  * Mathematical collisions (e.g. strings with identical polynomial hashes modulo $10^9 + 7$) are guaranteed to be verified and rejected without false positives.
* **Asymptotics:**
  * Preprocessing: $O(m)$ time, $O(1)$ space
  * Search: Average $O(n + m)$ time, Worst-case $O(nm)$ time (under adversarial collisions)
  * Auxiliary Space: $O(1)$.

### 1.4 Ingestion & Matching Pipeline
* **Implementation Status:** Implemented
* **Packages:** `engine.models`, `engine.ingestion`
* **Models:**
  * `IOC`: Immutable representation with `id`, `type`, `value`, `description`, `severity`.
  * `LogEntry`: Immutable telemetry representation with `timestamp`, `sourceIp`, `targetHost`, `eventType`, `payload`.
  * `SignatureMatch`: Immutable correlation finding with `ioc`, `logEntry`, `algorithmUsed`, `matchOffset`, `matchedValue`.
* **Parsers & Loaders:**
  * `LogParser`: Parses pipe-delimited records (`timestamp|sourceIp|targetHost|eventType|payload`) using first-principles character parsing. Skips comments and malformed lines safely without crashing batch ingestion.
  * `IOCLoader`: Loads pipe-delimited signature rules (`id|type|value|description|severity`).
  * `SignatureMatcher`: Dispatches pattern matching across `KMP`, `Z_ALGORITHM`, and `RABIN_KARP`, returning findings in custom `DynamicArray<SignatureMatch>`.
* **Zero Library Rule:** Zero usage of `java.util` collections or standard search methods (`String.indexOf`, `String.contains`, `Pattern`, `Matcher`). Backed entirely by Phase 2 `DynamicArray`.

---

## 2. Graph Traversal, Path Reconstruction & Kill-Chain Correlation (Phase 4)

### 2.0 Directed Graph Architecture & Representation
* **Implementation Status:** Implemented
* **Packages:** `engine.graph`, `engine.models`
* **Core Abstractions:**
  * `GraphNode<T>`: Adjacency list representation maintaining outgoing directed edges via custom `DynamicArray<GraphEdge<T>>`. Tracks node identifier, generic domain payload, and 0-based topological index for $O(1)$ visited array indexing.
  * `GraphEdge<T>`: Directed relationship representation preserving $u \to v$ orientation with attached descriptive labels and causal metadata.
  * `DirectedGraph<T>`: Core graph container managing node and edge sets. Preserves deterministic insertion order across nodes and adjacency lists. Zero `java.util` collections.
  * `AttackNode`: Immutable domain model representing a concrete compromised state or action, tracking unique ID, source entity/IP, target host, associated `KillChainStage`, timestamp, and optional `SignatureMatch` evidence.
  * `AttackEdge`: Directed causal relationship between `AttackNode` instances (e.g., `INTRA_HOST_ESCALATION`, `LATERAL_PIVOT`, `CAMPAIGN_STEP_PROGRESSION`). Free from future optimization metadata.
  * `AttackPath`: Ordered sequence of `AttackNode` instances and connecting `AttackEdge` hops from ingress to target sink.
* **Asymptotics:**
  * Space Complexity: $O(V + E)$ where $V$ is node count and $E$ is edge count.
  * Node/Edge Addition: $O(1)$ amortized append. Node lookup: $O(V)$ deterministic linear scan without hash maps.

### 2.1 Depth-First Search (DFS)
* **Implementation Status:** Implemented
* **Package:** `engine.graph.DFS`
* **Purpose:** Reachability analysis, cycle-safe component exploration, and deterministic ordering.
* **Mechanism:**
  * Iterative stack traversal utilizing project-specific `Stack<GraphNode<T>>`, eliminating call-stack overflow hazards.
  * Index-based visited tracking via primitive boolean arrays (`boolean[] visited = new boolean[V]`).
  * Preserves deterministic traversal matching edge insertion order by pushing neighbor nodes in reverse order so the first edge is processed first.
* **Asymptotics:**
  * Time Complexity: $O(V + E)$.
  * Auxiliary Space: $O(V)$ for stack storage and visited tracking.

### 2.2 Breadth-First Search (BFS)
* **Implementation Status:** Implemented
* **Package:** `engine.graph.BFS`
* **Purpose:** Shortest unweighted hop exploration, level-by-level traversal, and reachability validation.
* **Mechanism:**
  * Iterative queue traversal using project-specific circular-array `Queue<GraphNode<T>>`.
  * Index-based primitive visited tracking ensuring each node is enqueued at most once.
* **Asymptotics:**
  * Time Complexity: $O(V + E)$.
  * Auxiliary Space: $O(V)$ for queue storage and visited tracking.

### 2.3 Path Reconstruction (PathFinder)
* **Implementation Status:** Implemented
* **Package:** `engine.graph.PathFinder`
* **Purpose:** Reconstructs actual directed node sequences between source and destination endpoints.
* **Mechanism:**
  * BFS level traversal with parent pointer array (`int[] parentIndex = new int[V]`).
  * On reaching the target destination, parent indices are backtracked from target to source onto custom `Stack<GraphNode<T>>` and popped into a forward `DynamicArray<GraphNode<T>>`.
  * Domain adapter `findAttackPath` wraps path results into typed `AttackPath` models with constructed `AttackEdge` step hops.
  * Strictly avoids weighted shortest-path algorithms reserved for subsequent phases (zero Dijkstra, Bellman-Ford, Floyd-Warshall).
* **Asymptotics:**
  * Time Complexity: $O(V + E)$ for unweighted BFS search and path extraction.
  * Auxiliary Space: $O(V)$ for parent pointers and backtracking stack.

### 2.4 Kill-Chain Modeling & Correlation (AttackCorrelator)
* **Implementation Status:** Implemented
* **Package:** `engine.correlation`
* **Models & Engine:**
  * `KillChainStage`: Comparable stage model supporting canonical sequence (1. RECONNAISSANCE, 2. EXPLOITATION, 3. PRIVILEGE_ESCALATION, 4. LATERAL_MOVEMENT, 5. DEFENSE_EVASION, 6. COMMAND_AND_CONTROL, 7. EXFILTRATION).
  * `KillChain`: Stage registry organizing attack nodes into tactical buckets, with deterministic classification from Phase 3 evidence using KMP pattern inspection without forbidden search APIs.
  * `AttackCorrelator`: Transforms Phase 3 `SignatureMatch` telemetry into `DirectedGraph<AttackNode>`. Sorts matches chronologically via first-principles Insertion Sort ($O(n^2)$ time, $O(1)$ auxiliary space) and applies deterministic correlation rules:
    1. *Intra-Host Escalation*: Successive actions on the same host are linked.
    2. *Lateral Pivot*: Transitions to new target hosts or explicit `LATERAL` / `IPC$` events link preceding pivot hosts to newly compromised endpoints.
    3. *Campaign Sequence*: Consecutive events sharing attacker source IP form a campaign attack thread.
* **Documented Telemetry Limitations:**
  * *Network Topology Absence*: Phase 3 logs lack subnet masks and routing tables; host pivots are inferred from source IP and target host values.
  * *Process Lineage Absence*: Telemetry records commands and payloads but omits PID/PPID process tree references.
  * *Exact Signature Dependence*: Reconstructs paths from verified Phase 3 pattern matches without fuzzy or probabilistic weights.
* **Asymptotics:**
  * Time Complexity: $O(N^2 + V + E)$ where $N$ is evidence count.
  * Space Complexity: $O(N + V + E)$ storage across nodes, edges, and stages.

---

## 3. Choke-Point Identification & Cut Analysis (Phase 5)

### 3.1 Capacity Model & Flow Network Representation
* **Implementation Status:** Implemented
* **Packages:** `engine.optimization`
* **Core Abstractions:**
  * `FlowEdge`: Directed flow edge tracking endpoints `from` and `to`, integer `capacity`, current `flow`, and residual capacity calculations (`residualCapacityTo(v)`).
  * `FlowNetwork`: Flow network representation managing vertex-to-index mappings (`indexedVertices`), adjacency lists of outgoing and backward residual edges via `DynamicArray<FlowEdge>`, and a 2D residual capacity matrix `residualCapacity[u][v]`.
  * `MaxFlowResult`: Immutable result container encapsulating `maxFlow` integer value, source/sink identifiers, and flow edge assignments.
  * `MinCutResult`: Immutable result container holding `cutCapacity`, source-side reachable partition `DynamicArray<T> sourcePartition`, sink-side partition `DynamicArray<T> sinkPartition`, and crossing `DynamicArray<FlowEdge> cutEdges`.
* **Telemetry Distinction (Capacity vs. Connectivity):**
  * *Critical Distinction*: Raw security telemetry ingested in Phase 3 contains timestamp, host, and payload strings, but does **not** contain physical network bandwidth or throughput capacities.
  * *Attack Connectivity vs. Capacity*: Phase 4 `AttackEdge` strictly models causal attack relationships (`LATERAL_PIVOT`, `INTRA_HOST_ESCALATION`), not link capacity. To avoid corrupting Phase 4 domain semantics, `AttackEdge` was not altered.
  * *Synthetic Capacity Model*: A dedicated `FlowNetwork` abstraction in `engine.optimization` represents flow topologies. When analyzing a Phase 4 `DirectedGraph<T>`, `FlowNetwork.fromDirectedGraph(graph, defaultCapacity)` projects directed connectivity with an explicit integer capacity (defaulting to 1 for unweighted bottleneck analysis, or custom algorithmic weights for test simulations).

### 3.2 Maximum Flow (Edmonds-Karp)
* **Implementation Status:** Implemented
* **Package:** `engine.optimization.MaxFlow`
* **Purpose:** Computes the maximum attacker throughput or concurrent compromise paths through a directed choke-point network.
* **Mechanism:**
  * First-principles implementation of the Edmonds-Karp specialization of the Ford-Fulkerson method.
  * Employs Breadth-First Search (BFS) using Phase 2 circular `Queue<Integer>` to systematically discover the shortest augmenting path (in terms of edge count) from source to sink in the residual network.
  * Tracks augmenting path predecessors via primitive integer array `parentEdgeIndex[V]`.
  * Computes bottleneck residual capacity along the discovered path:
    $$\Delta = \min_{(u, v) \in P} c_f(u, v)$$
  * Repeatedly augments flow across forward and reverse residual edges until no $s \to t$ path exists in the residual network ($c_f(u, v) \le 0$ across all cuts).
* **Defensive Validation & Edge Cases:**
  * Source equals sink: throws `IllegalArgumentException` (infinite capacity loop prevented).
  * Source or sink not found in network: throws `IllegalArgumentException`.
  * Disconnected sink, zero-capacity edges, multiple parallel routes, cyclic topologies: handled deterministically returning zero or bottleneck flow.
* **Asymptotics:**
  * Time Complexity: $O(V \cdot E^2)$ where $V$ is vertex count and $E$ is edge count (since each augmenting path increases distance or saturates a critical edge; at most $O(VE)$ augmentations, each BFS takes $O(E)$).
  * Space Complexity: $O(V^2 + E)$ for residual capacity matrix and edge adjacency lists.

### 3.3 Minimum Cut (Max-Flow Min-Cut Theorem)
* **Implementation Status:** Implemented
* **Package:** `engine.optimization.MinCut`
* **Purpose:** Identifies the minimum-capacity set of communication links or lateral movement pivots whose severance completely partitions the network, isolating adversary entry points from sensitive target systems.
* **Mechanism:**
  * Directly computes maximum flow via `MaxFlow.compute(network, source, sink)` to obtain the fully saturated residual graph.
  * Runs a BFS reachability traversal starting from source $s$ on the final residual graph where an edge $(u, v)$ is traversable if and only if $c_f(u, v) > 0$.
  * Partitions vertices into two disjoint sets:
    * Source-side partition $S$: vertices reachable from $s$ in the residual graph.
    * Sink-side partition $T = V \setminus S$: vertices unreachable from $s$.
  * Reconstructs cut edges: all original forward network edges $(u, v)$ such that $u \in S$ and $v \in T$.
  * By the Max-Flow Min-Cut Theorem, the sum of original capacities of these cut edges strictly equals the computed `maxFlow`.
* **Asymptotics:**
  * Time Complexity: $O(V \cdot E^2)$ dominated by Max-Flow computation, followed by $O(V + E)$ residual BFS reachability and cut extraction.
  * Space Complexity: $O(V + E)$ auxiliary partition arrays.

### 3.4 Articulation Points (Tarjan-Style Low-Link DFS)
* **Implementation Status:** Implemented
* **Package:** `engine.optimization.ArticulationPoints`
* **Purpose:** Discovers structurally single-point-of-failure vertices (choke-point hosts or pivot entities) whose removal increases the number of connected components.
* **Directed-to-Undirected Projection Decision:**
  * *Theoretical Requirement*: Articulation points are formally defined on undirected graphs. Attack graphs in Phase 4 are strictly directed ($u \to v$).
  * *Projection Transformation*: `ArticulationPoints.findArticulationPoints(DirectedGraph<T>)` projects directed attack edges into an undirected connectivity graph:
    $$\forall (u \to v) \in E_{\text{directed}} \implies \{u, v\} \in E_{\text{undirected}}$$
  * This projection does *not* mutate the underlying Phase 4 `DirectedGraph<T>` instance; it builds an internal undirected adjacency list across indexed nodes.
* **Tarjan DFS Mechanism:**
  * Tracks integer discovery times `disc[u]`, low-link reachability values `low[u]`, parent pointers `parent[u]`, and a `visited[u]` boolean array.
  * For each unvisited vertex, executes recursive DFS traversal:
    * `low[u] = disc[u] = ++timer`
    * For each neighbor $v$ of $u$:
      * If $v$ is parent of $u$: skip.
      * If $v$ is already visited: back-edge detected; update `low[u] = Math.min(low[u], disc[v])`.
      * If $v$ is unvisited: tree-edge; increment child count; recursively visit $v$; update `low[u] = Math.min(low[u], low[v])`.
      * Articulation condition for non-root: if $u$ is not the DFS root and $\text{low}[v] \ge \text{disc}[u]$, vertex $u$ is an articulation point.
    * Articulation condition for root: if $u$ is the DFS root and has 2 or more independent tree-edge children, $u$ is an articulation point.
  * Supports disconnected graphs by iterating through all vertices to cover every connected component.
  * Results are deduplicated and returned in a `DynamicArray<T>` without using `java.util.HashSet`.
* **Asymptotics:**
  * Time Complexity: $O(V + E)$ for undirected projection construction and single-pass DFS traversal.
  * Space Complexity: $O(V + E)$ for adjacency storage and DFS tracking arrays.


---

## 4. Monitoring Placement & Sensor Optimization (Phase 6)

### 4.1 Vertex Cover Formulation (2-Approximation)
* **Implementation Status:** Implemented
* **Package:** `engine.monitoring.MonitoringPlacement`
* **Models:** `MonitoringPoint<T>`, `MonitoringResult<T>`
* **Undirected Connectivity Projection:**
  * Constructs an undirected view $u -- v$ for every directed edge $u \to v$ without mutating the Phase 4 `DirectedGraph<T>`.
  * Deduplicates bidirectional and parallel edges into unique undirected edge pairs.
* **Deterministic 2-Approximation Heuristic:**
  * While uncovered edges remain:
    1. Selects the first uncovered edge $(u, v)$ in deterministic graph edge order.
    2. Adds both vertices $u$ and $v$ to the monitoring set.
    3. Marks every edge incident to $u$ or $v$ as covered.
  * Guarantees all edges are covered (100% coverage ratio).
* **Algorithmic Limitations & Non-Claim:**
  * *Critical Notice*: Minimum Vertex Cover is NP-hard. This implementation provides a polynomial-time 2-approximation; it does **not** guarantee a globally minimal monitoring set on arbitrary graphs.
  * *Physical Realism*: Monitoring points represent algorithmic graph vertices for telemetry collection, not physical hardware sensors or packet taps.
* **Asymptotics:**
  * Time Complexity: $O(V \cdot E)$ where $V$ is node count and $E$ is edge count.
  * Space Complexity: $O(V + E)$ auxiliary tracking arrays and edge lists.

### 4.2 Greedy Set Cover Approximation
* **Implementation Status:** Implemented
* **Package:** `engine.monitoring.MonitoringPlacement`
* **Mechanism:**
  * Formulates edge monitoring as a Set Cover instance: each candidate vertex $u$ covers a subset of incident edges.
  * In each iteration, greedily selects the vertex $u$ that covers the maximum number of currently uncovered edges.
  * Breaks ties deterministically in favor of the lower stable graph index (`bestGain > currentGain`).
  * Supports configurable target coverage ratios (e.g. 100% or partial coverage budgets $p \in [0.0, 1.0]$).
* **Approximation Factor:**
  * Achieves the standard $H(d) = O(\ln \Delta)$ greedy approximation ratio for Set Cover, where $\Delta$ is maximum vertex degree.
* **Asymptotics:**
  * Time Complexity: $O(k \cdot V \cdot E)$ where $k \le V$ is the number of selected monitoring points.
  * Space Complexity: $O(V + E)$.

---

## 5. Priority Scheduling & Alert Routing (Phase 7)

### 5.1 Custom Binary Heap (Binary Max-Heap)
* **Implementation Status:** Implemented
* **Package:** `engine.alerting.BinaryHeap<T>`
* **Internal Representation:** Array-backed complete binary tree using custom `DynamicArray<T>`. Zero `java.util.PriorityQueue` usage.
* **Heap Operations:**
  * `insert(T item)`: Appends element to dynamic array and performs `siftUp` in $O(\log n)$ time.
  * `peek()`: Inspects root element at index 0 in $O(1)$ time. Throws `IllegalStateException` on empty heap.
  * `extractMax()` / `extract()`: Swaps root with tail element, shrinks array, and performs `siftDown` in $O(\log n)$ time.
  * `size()`, `isEmpty()`: $O(1)$ status queries.
  * `clear()`: $O(n)$ array clearing.
* **Asymptotics:**
  * Time Complexity: $O(\log n)$ for insert and extract; $O(1)$ for peek.
  * Space Complexity: $O(n)$ contiguous elements.

### 5.2 Deterministic Alert Priority & Ordering
* **Implementation Status:** Implemented
* **Package:** `engine.alerting.AlertPriority`, `engine.alerting.Alert`
* **Four-Tier Deterministic Comparison Hierarchy:**
  1. *Severity (Primary)*: Higher integer severity dominates (5 > 4 > 3 > 2 > 1).
  2. *Attack Depth (Secondary)*: Greater kill-chain traversal depth dominates on severity ties (e.g. exfiltration depth 6 > lateral depth 3).
  3. *Arrival Sequence (Tertiary)*: Lower monotonic sequence number dominates on depth ties, guaranteeing stable FIFO order among identical alerts.
  4. *Alert ID (Fallback)*: Lexicographical ID comparison ensuring total deterministic ordering.

### 5.3 Alert Router & Alert Manager
* **Implementation Status:** Implemented
* **Package:** `engine.alerting.AlertRouter`, `engine.alerting.AlertManager`
* **Components:**
  * `AlertRouter`: Routes alerts through `BinaryHeap<Alert>` and drains prioritized alert queues.
  * `AlertManager`: High-level facade generating sequential alerts from security findings and dispatching them in ranked priority order.
* **Scope Boundary:**
  * Focuses strictly on core DSA priority scheduling. Zero external email, SMS, websocket, or dashboard dependencies.

