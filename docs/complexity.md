# Algorithmic Complexity Specifications

> **Notice:** The tables below summarize the computational complexities of the foundational data structures and planned algorithms for the Threat-Intelligence Kill-Chain Engine. Implementation statuses reflect Phase 2.

---

## 1. Phase 2 Foundational Data Structures Complexity

The following matrix specifies the operation time complexity, amortized complexity where applicable, worst-case resize cost, auxiliary space per operation, and total data-structure storage.

| Structure | Operation | Operation Time (Best) | Operation Time (Average) | Operation Time (Worst) | Amortized Time | Worst-Case Resize Cost | Auxiliary Space (per op) | Total Storage | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **DynamicArray** | `add(T)` | $\Omega(1)$ | $\Theta(1)$ | $O(n)$ (when full) | $O(1)$ amortized | $O(n)$ array copy | $O(1)$ (normal) / $O(n)$ (resize) | $O(C)$ where $C \ge n$ (capacity) | Implemented |
| | `add(index, T)` | $\Omega(1)$ (tail) | $\Theta(n)$ | $O(n)$ | N/A | $O(n)$ array copy | $O(1)$ (normal) / $O(n)$ (resize) | $O(C)$ | Implemented |
| | `get(index)` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `set(index, T)` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `remove(index)` | $\Omega(1)$ (tail) | $\Theta(n)$ | $O(n)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `contains(T)` | $\Omega(1)$ (head) | $\Theta(n)$ | $O(n)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `size() / isEmpty()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `clear()` | $\Omega(n)$ | $\Theta(n)$ | $O(n)$ (nulling slots) | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| **LinkedList** | `addFirst(T)` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None (node allocation) | $O(1)$ | $O(n)$ nodes ($32{-}48$ bytes/node) | Implemented |
| | `addLast(T)` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ (tail pointer) | N/A | None (node allocation) | $O(1)$ | $O(n)$ | Implemented |
| | `get(index)` | $\Omega(1)$ (index 0) | $\Theta(n)$ | $O(n)$ | N/A | None | $O(1)$ | $O(n)$ | Implemented |
| | `set(index, T)` | $\Omega(1)$ (index 0) | $\Theta(n)$ | $O(n)$ | N/A | None | $O(1)$ | $O(n)$ | Implemented |
| | `removeFirst()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(n)$ | Implemented |
| | `removeLast()` | $\Omega(1)$ ($n=1$) | $\Theta(n)$ | $O(n)$ (singly linked) | N/A | None | $O(1)$ | $O(n)$ | Implemented |
| | `remove(index)` | $\Omega(1)$ (index 0) | $\Theta(n)$ | $O(n)$ | N/A | None | $O(1)$ | $O(n)$ | Implemented |
| | `contains(T)` | $\Omega(1)$ (head) | $\Theta(n)$ | $O(n)$ | N/A | None | $O(1)$ | $O(n)$ | Implemented |
| | `size() / isEmpty()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(n)$ | Implemented |
| | `clear()` | $\Omega(n)$ | $\Theta(n)$ | $O(n)$ (unlinking nodes) | N/A | None | $O(1)$ | $O(n)$ | Implemented |
| **Stack** | `push(T)` | $\Omega(1)$ | $\Theta(1)$ | $O(n)$ (backing resize) | $O(1)$ amortized | $O(n)$ array copy | $O(1)$ (normal) / $O(n)$ (resize) | $O(C)$ backing array | Implemented |
| | `pop()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `peek()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `size() / isEmpty()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `clear()` | $\Omega(n)$ | $\Theta(n)$ | $O(n)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| **Queue** | `enqueue(T)` | $\Omega(1)$ | $\Theta(1)$ | $O(n)$ (circular resize) | $O(1)$ amortized | $O(n)$ re-alignment copy | $O(1)$ (normal) / $O(n)$ (resize) | $O(C)$ circular array | Implemented |
| | `dequeue()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ (modulo advance) | N/A | None (no element shift) | $O(1)$ | $O(C)$ | Implemented |
| | `peek()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `size() / isEmpty()` | $\Omega(1)$ | $\Theta(1)$ | $O(1)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |
| | `clear()` | $\Omega(n)$ | $\Theta(n)$ | $O(n)$ | N/A | None | $O(1)$ | $O(C)$ | Implemented |

---

---

## 2. Phase 3 String Matching & Ingestion Complexity

The table below documents the computational complexity of the implemented Phase 3 string matching algorithms and ingestion components.

| Component | Operation | Time (Best) | Time (Average) | Time (Worst) | Auxiliary Space | Total Storage | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **KMP** | `buildLPS(pattern)` | $\Omega(m)$ | $\Theta(m)$ | $O(m)$ | $O(m)$ | $O(m)$ integer array | Implemented |
| | `search(text, pattern)` | $\Omega(n)$ | $\Theta(n)$ | $O(n)$ | $O(1)$ | $O(k)$ matches ($k \le n$) | Implemented |
| | **Total KMP Pipeline** | $\Omega(n + m)$ | $\Theta(n + m)$ | $O(n + m)$ | $O(m)$ | $O(m + k)$ | Implemented |
| **Z-Algorithm** | `buildZ(pattern)` | $\Omega(m)$ | $\Theta(m)$ | $O(m)$ | $O(m)$ | $O(m)$ integer array | Implemented |
| | `search(text, pattern)` | $\Omega(n)$ | $\Theta(n)$ | $O(n)$ | $O(1)$ | $O(k)$ matches ($k \le n$) | Implemented |
| | **Total Z Pipeline** | $\Omega(n + m)$ | $\Theta(n + m)$ | $O(n + m)$ | $O(m)$ | $O(m + k)$ | Implemented |
| **Rabin-Karp** | `computeHash(s, start, len)` | $\Omega(m)$ | $\Theta(m)$ | $O(m)$ | $O(1)$ | $O(1)$ 64-bit integer | Implemented |
| | `search(text, pattern)` | $\Omega(n + m)$ | $\Theta(n + m)$ | $O(n \cdot m)$ | $O(1)$ | $O(k)$ matches | Implemented |
| **LogParser** | `parseLine(line)` | $\Omega(L)$ | $\Theta(L)$ | $O(L)$ | $O(L)$ tokens | $O(L)$ per LogEntry | Implemented |
| | `parseFile / parseContent` | $\Omega(N \cdot L)$ | $\Theta(N \cdot L)$ | $O(N \cdot L)$ | $O(L)$ per line | $O(N \cdot L)$ DynamicArray | Implemented |
| **IOCLoader** | `parseLine(line)` | $\Omega(K)$ | $\Theta(K)$ | $O(K)$ | $O(K)$ tokens | $O(K)$ per IOC | Implemented |
| | `loadFile / loadContent` | $\Omega(M \cdot K)$ | $\Theta(M \cdot K)$ | $O(M \cdot K)$ | $O(K)$ per line | $O(M \cdot K)$ DynamicArray | Implemented |
| **SignatureMatcher** | `matchAll(logs, iocs, algo)` | $\Omega(N \cdot M \cdot (n_i + m_j))$ | $\Theta(N \cdot M \cdot (n_i + m_j))$ | $O(N \cdot M \cdot n_i \cdot m_j)$ (RK worst) | $O(m_{\max})$ | $O(S)$ matches | Implemented |

*Notes on Ingestion & Matching Variables:*
* $n$ = text length, $m$ = pattern length.
* $L$ = log line length, $N$ = total log entries.
* $K$ = IoC line length, $M$ = total IoC definitions.
* $k$ = number of match occurrences.

---

## 3. Foundational Memory & Resizing Specifications

### 3.1 DynamicArray
* **Initial Capacity:** 10 elements by default, configurable via constructor.
* **Growth Policy:** Geometric scaling by factor of 2 ($\text{newCapacity} = 2 \times \text{capacity}$).
* **Amortized Analysis:** By the aggregate method, inserting $N$ elements requires at most $N + 2N = 3N$ total element operations, yielding an amortized cost of $\Theta(1)$ per append.
* **Storage Overhead:** Array contiguous references: $O(C)$, where $C$ is internal capacity ($C < 2n$).

### 3.2 LinkedList
* **Node Overhead:** Each `Node<T>` allocates 2 references (data object pointer and next node pointer), incurring typical 64-bit JVM object overhead (~24-32 bytes per node).
* **Pointer Integrity:** Direct $O(1)$ head and tail references guarantee strict $O(1)$ queue/list appending (`addLast`) and prepending (`addFirst`). Singly linked nature requires $O(n)$ traversal to identify the predecessor node during `removeLast()`.

### 3.3 Stack
* **Backing Engine:** Custom `DynamicArray<T>`.
* **Behavior:** LIFO access pattern operates strictly on index `size - 1`. `pop()` and `peek()` are guaranteed strict $O(1)$ operations with zero shifting.

### 3.4 Queue
* **Backing Engine:** Circular buffer array with modular arithmetic pointers `(index + 1) % capacity`.
* **Zero-Shift Dequeue:** Advancing `front` pointer eliminates the $O(n)$ left-shifting cost of naive array queues, guaranteeing strict $O(1)$ dequeue.
* **Dynamic Re-alignment:** When full, the queue allocates a doubled array and linearizes the circular contents into contiguous order starting at index 0, resetting `front = 0` and `rear = size`.

---

## 4. Future Algorithmic Components (Phase 4+ Theoretical Bounds)

| Algorithm / Component | Best Case Time | Average Case Time | Worst Case Time | Space Complexity | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Custom HashMap (Chaining)** | $\Omega(1)$ | $\Theta(1)$ amortized | $O(n)$ | $O(n)$ | Planned (Phase 4) |
| **Custom HashSet** | $\Omega(1)$ | $\Theta(1)$ amortized | $O(n)$ | $O(n)$ | Planned (Phase 4) |
| **Depth-First Search (DFS)** | $\Omega(V + E)$ | $\Theta(V + E)$ | $O(V + E)$ | $O(V)$ | Planned (Phase 5) |
| **Breadth-First Search (BFS)** | $\Omega(V + E)$ | $\Theta(V + E)$ | $O(V + E)$ | $O(V)$ | Planned (Phase 5) |
| **Binary Heap (Insert/Extract)** | $\Omega(1)$ peek | $\Theta(\log n)$ | $O(\log n)$ | $O(n)$ | Planned (Phase 5) |
| **Tarjan Articulation Points** | $\Omega(V + E)$ | $\Theta(V + E)$ | $O(V + E)$ | $O(V)$ | Planned (Phase 6) |
| **Max-Flow (Edmonds-Karp)** | $\Omega(V + E)$ | $O(V \cdot E^2)$ | $O(V \cdot E^2)$ | $O(V^2)$ or $O(V + E)$ | Planned (Phase 6) |
| **Min-Cut Derivation** | $\Omega(V + E)$ | $\Theta(V + E)$ | $O(V + E)$ | $O(V)$ | Planned (Phase 6) |
| **Path-Hitting Set / Monitoring** | $\Omega(\|P\|)$ | Greedy approx / Exact | Exponential (Exact) / $O(\|P\| \cdot \|V\|)$ (Greedy) | $O(\|P\| + \|V\|)$ | Planned (Phase 7) |
