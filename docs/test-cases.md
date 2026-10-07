# Test Case Specifications and Verification Plan

> **Notice:** All test cases documented below form the verification harness for the Threat-Intelligence Kill-Chain Engine. Test results reflect actual standalone test execution on Java 17.

---

## 1. Phase 2 Foundational Data Structures Test Suite

### 1.1 Summary of Test Execution

All 36 unit tests across the 4 foundational data structure suites were executed and verified on Java 17.0.12:

| Test Suite | Class Name | Test Methods | Passed | Failed | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **DynamicArray Suite** | `engine.datastructures.DynamicArrayTest` | 9 | 9 | 0 | **PASS** |
| **LinkedList Suite** | `engine.datastructures.LinkedListTest` | 10 | 10 | 0 | **PASS** |
| **Stack Suite** | `engine.datastructures.StackTest` | 8 | 8 | 0 | **PASS** |
| **Queue Suite** | `engine.datastructures.QueueTest` | 9 | 9 | 0 | **PASS** |
| **Total** | | **36** | **36** | **0** | **100% PASS** |

---

### 1.2 Data Structures Verification Matrix

| Test Case ID | Test Category | Description / Boundary Conditions | Expected Behavior | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-DS-01** | Empty input | Initialize empty `DynamicArray`, `LinkedList`, `Stack`, `Queue` and verify bounds. | `size() == 0`, `isEmpty() == true`, safe underflow handling with exceptions (`IndexOutOfBoundsException` / `IllegalStateException`). | As expected. Throws proper descriptive exceptions. | **PASS** |
| **TC-DS-02** | Single element | Add/remove single element in all basic structures. | Correct head/tail update, capacity stable, size toggles 0/1. | Head and tail properly updated and cleared to null. | **PASS** |
| **TC-DS-03** | Dynamic resizing | Insert elements exceeding initial capacity in `DynamicArray` and `Queue`. | Array doubles capacity smoothly; zero data loss; FIFO/LIFO preserved across resizes. | Tested up to 1000 items in Stack, 50 in DynamicArray, and wrapped circular resize in Queue. | **PASS** |
| **TC-DS-04** | Duplicate data | Insert duplicate elements into `DynamicArray`, `LinkedList`, `Stack`, `Queue`. | Duplicate values preserved in exact insertion sequence; equality checked correctly. | Duplicates correctly stored, retrieved, and removed in order. | **PASS** |
| **TC-DS-05** | Null handling | Store, retrieve, search, and remove `null` values across all four structures. | Null-safe operations without `NullPointerException`. | Null values correctly supported and handled safely. | **PASS** |
| **TC-DS-06** | Circular Queue Wrap | Enqueue and dequeue in `Queue`, wrapping rear pointer around buffer boundary without resize. | Circular index calculation `(index + 1) % capacity` operates in $O(1)$ without shifting. | Verified with front at index 2 and rear wrapped to 0/1. | **PASS** |
| **TC-DS-07** | Queue Wrap-Then-Resize | Enqueue until wrapped buffer is full, then enqueue additional item to trigger dynamic growth. | Allocates double buffer; linearizes circular elements into continuous FIFO sequence. | Elements linearized starting from index 0; strictly preserves FIFO order. | **PASS** |
| **TC-DS-08** | LIFO Stack Ordering | Push elements $A, B, C$ onto Stack. | `pop()` retrieves $C, B, A$ in strict reverse order; `peek()` does not alter stack state. | Verified strict LIFO ordering. | **PASS** |
| **TC-DS-09** | Clear and Reuse | Call `clear()` on all 4 structures and subsequent re-population. | Sets size to 0, frees internal references for GC, subsequent insertions function identically to clean instance. | Verified across all four structures. | **PASS** |
| **TC-DS-10** | Duplicate data (Associative) | Insert duplicate keys into `HashSet` and `HashMap`. | Set size invariant; map updates existing key value without entry duplication. | Deferred to Phase 3. | Not implemented |
| **TC-DS-11** | Hash collisions | Insert keys intentionally engineered with identical polynomial hash modulo table size. | Separate chaining preserves both entries; `get()` retrieves correct values. | Deferred to Phase 3. | Not implemented |
| **TC-DS-12** | Key removal | Remove existing and non-existing keys from `HashMap` and `HashSet`. | Correct bucket list unlinking; `containsKey()` updates accurately. | Deferred to Phase 3. | Not implemented |
| **TC-DS-13** | Binary Heap ordering | Insert elements in random order into Min-Heap and Max-Heap. | `extract()` produces strictly monotonic priority sequence; heap property holds. | Deferred to Phase 3. | Not implemented |

---

## 2. Phase 3 String Matching & Ingestion Test Suite

### 2.1 Summary of Phase 3 Test Execution

All 59 unit and integration tests across the 6 Phase 3 test suites were executed and verified on Java 17.0.12:

| Test Suite | Class Name | Test Methods | Passed | Failed | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **KMP Suite** | `engine.strings.KMPTest` | 12 | 12 | 0 | **PASS** |
| **Z-Algorithm Suite** | `engine.strings.ZAlgorithmTest` | 14 | 14 | 0 | **PASS** |
| **Rabin-Karp Suite** | `engine.strings.RabinKarpTest` | 13 | 13 | 0 | **PASS** |
| **Log Parser Suite** | `engine.ingestion.LogParserTest` | 6 | 6 | 0 | **PASS** |
| **IOC Loader Suite** | `engine.ingestion.IOCLoaderTest` | 6 | 6 | 0 | **PASS** |
| **Integration Suite** | `engine.integration.SignatureMatchingIntegrationTest` | 8 | 8 | 0 | **PASS** |
| **Phase 3 Total** | | **59** | **59** | **0** | **100% PASS** |
| **Combined (Phases 2 & 3)** | | **95** | **95** | **0** | **100% PASS** |

---

### 2.2 String Matching & Ingestion Verification Matrix

| Test Case ID | Test Category | Description / Boundary Conditions | Expected Behavior | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-STR-01** | LPS Construction | Construct LPS table for pattern with repeated prefixes (`ABABCABAB`, `AAAA`, `""`). | Correct prefix-suffix lengths: `[0, 0, 1, 2, 0, 1, 2, 3, 4]`, `[0, 1, 2, 3]`, and `[]`. | As expected. Table values verified. | **PASS** |
| **TC-STR-02** | Z-Array Construction | Construct Z-array for repetitive and palindromic strings (`aaaaa`, `abacaba`, `""`). | Correct longest prefix match lengths: `[0, 4, 3, 2, 1]`, `[0, 0, 1, 0, 3, 0, 1]`, `[]`. | As expected. | **PASS** |
| **TC-STR-03** | Basic Match | Single occurrence of multi-word substring in sentence text. | Match offset returned at exact 0-based character index. | Offset 10 returned identically by KMP, Z, and RK. | **PASS** |
| **TC-STR-04** | Pattern Not Found | Substring absent from target text. | Returns 0 matches (empty integer array) in linear time without error. | Empty array `[]` returned by KMP, Z, and RK. | **PASS** |
| **TC-STR-05** | Multiple Matches | Disjoint occurrences of pattern in text (`cat` in text at offsets 0, 16, 33). | All match starting indices returned in increasing order. | Returns `[0, 16, 33]` across all algorithms. | **PASS** |
| **TC-STR-06** | Overlapping Matches | Overlapping instances: `ABAB` in `ABABDABABCABAB` (0, 5, 10), `ABAB` in `ABABAB` (0, 2), `AAA` in `AAAAA` (0, 1, 2). | All overlapping starting indices discovered without skipping. | Returns exact offsets without skipping. | **PASS** |
| **TC-STR-07** | Beginning / End / Exact | Pattern at start (index 0), at end (last index), or pattern identical to text. | Exact offsets returned (0, text.length() - pattern.length(), 0). | Boundary matches discovered correctly. | **PASS** |
| **TC-STR-08** | Pattern > Text | Pattern string length strictly greater than text string length. | Immediate return of empty array; no out-of-bounds access. | Returns `[]` without exception. | **PASS** |
| **TC-STR-09** | Empty / Null Inputs | Text null, pattern null, text empty, pattern empty, or both empty. | Safe return of empty array without throwing `NullPointerException`. | Returns `[]` across all cases. | **PASS** |
| **TC-STR-10** | Single Character | Search single character in text with single, multiple, or zero occurrences. | All matching character indices returned; exact matches returned. | All occurrences of 'a' in `abracadabra` returned. | **PASS** |
| **TC-STR-11** | Special Characters | Patterns containing spaces, quotes, pipes, dollar signs, and slashes. | Clean match without escaping errors or delimiter crashes. | Offset 8 returned for complex command string. | **PASS** |
| **TC-STR-12** | Separator Collision (Z) | Text and pattern containing `#`, `$`, `@`, `\0`, `\|`. | Separator-free two-phase Z-box prevents delimiter collision false matches. | Offset 8 and offsets 0, 34 returned accurately. | **PASS** |
| **TC-STR-13** | Rolling Hash Behavior | Slide window across alphabet, comparing rolling calculation with direct hash. | Rolling window formula matches direct hash computation at every offset. | Strict equality confirmed for all window positions. | **PASS** |
| **TC-STR-14** | Hash Collision Verification | Mathematical collision strings with identical hash ($h = 65$) but different characters. | Character-by-character verification rejects collision; zero false positives. | True match accepted (7); false collision rejected (`[]`). | **PASS** |
| **TC-ING-01** | Valid Log Parsing | Parse 5-field pipe-delimited log entry. | Returns immutable `LogEntry` with correct field values. | All 5 fields parsed and verified. | **PASS** |
| **TC-ING-02** | Malformed Log Rejection | Lines with 4 or 6 fields, empty lines, or null lines. | Throws descriptive `IllegalArgumentException` on individual parsing. | Correctly rejects malformed records. | **PASS** |
| **TC-ING-03** | Safe Batch Log Parsing | Multi-line text with comments (`#`), empty lines, and malformed lines. | Skips comments/malformed lines with warning; returns valid entries. | Exactly 2 valid entries extracted; 2 malformed skipped. | **PASS** |
| **TC-ING-04** | Valid IOC Parsing | Parse 5-field pipe-delimited IOC rule. | Returns immutable `IOC` with trimmed values. | All 5 fields parsed and verified. | **PASS** |
| **TC-ING-05** | Malformed IOC Rejection | Lines with missing/extra fields or empty strings. | Throws descriptive `IllegalArgumentException`. | Correctly rejects malformed IOC definitions. | **PASS** |
| **TC-ING-06** | Safe Batch IOC Loading | Multi-line string with comments and incomplete records. | Skips invalid records with warning; returns valid entries. | Exactly 2 valid IOCs loaded. | **PASS** |
| **TC-INT-01** | Cross-Algorithm Parity | Run KMP, Z-Algorithm, and Rabin-Karp on identical text and pattern pairs. | Strict equality: `KMP(text, pat) == Z(text, pat) == RK(text, pat)`. | Identical offsets confirmed on all test strings. | **PASS** |
| **TC-INT-02** | End-to-End Pipeline | Ingest `logs.txt` and `iocs.txt`, run `SignatureMatcher` with all 3 algorithms. | Match count and metadata match identically across all 3 algorithms. | Discovered 9 matches identically across KMP, Z, and RK. | **PASS** |
| **TC-INT-03** | Metadata Integrity | Validate IOC ID, host, severity, and report formatting of matches. | All expected indicators mapped to correct target hosts and severities. | Verified IOC-001, IOC-002, IOC-003, IOC-004, IOC-007. | **PASS** |

---

## 3. Graph Construction & Traversal (Phase 5 Verification)

| Test Case ID | Test Category | Description / Boundary Conditions | Expected Behavior | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-GRP-01** | Graph with no edges | Add isolated host nodes without lateral transitions. | BFS and DFS traverse only starting node; zero paths found. | Not implemented |
| **TC-GRP-02** | Disconnected graph | Two distinct subgraphs with independent adversary activities. | Traversal identifies reachability restricted strictly to connected components. | Not implemented |
| **TC-GRP-03** | Graph with cycles | Adversary repeatedly pivots between two internal compromised hosts (cyclic loop). | Visited set prevents infinite loops; DFS/BFS complete deterministically. | Not implemented |
| **TC-GRP-04** | Single attack path | Direct sequence from Initial Access to Target Database ($A \to B \to C$). | BFS/DFS extract exact linear sequence of length 2 edges. | Not implemented |
| **TC-GRP-05** | Multiple attack paths | Redundant lateral movement vectors reaching the target asset. | All distinct paths enumerated correctly without duplication. | Not implemented |

---

## 4. Optimization & Choke-Point Analysis (Phase 6 Verification)

| Test Case ID | Test Category | Description / Boundary Conditions | Expected Behavior | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-OPT-01** | Articulation points | Linear bridge topology ($A \to B \to C$) and diamond topology ($A \to B, C \to D$). | Node $B$ identified as articulation point in linear topology; none in diamond. | Not implemented |
| **TC-OPT-02** | Flow network capacity | Single-source single-sink network with varying edge bandwidths and zero-capacity edges. | Max-Flow matches minimum cut value by Max-Flow Min-Cut Theorem. | Not implemented |
| **TC-OPT-03** | Min-Cut derivation | Residual graph partition from saturated flow network. | Derives exact minimal bottleneck edge set separating ingress from target. | Not implemented |
| **TC-OPT-04** | Monitoring coverage | Selected sensor placement set evaluated against all enumerated attack paths. | Computes exact coverage percentage ($k / N \times 100\%$); reports true coverage. | Not implemented |

---

## 5. Alert Prioritization (Phase 7 Verification)

| Test Case ID | Test Category | Description / Boundary Conditions | Expected Behavior | Status |
| :--- | :--- | :--- | :--- | :--- |
| **TC-ALT-01** | Alert priority ordering | Ingest events of varying severities (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`). | Binary Heap extracts `CRITICAL` alerts first; tie-breaks by depth/timestamp. | Not implemented |
