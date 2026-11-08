# Assignment 4 — analysis

Author: Baglankyzy Aisha, group SE-2537. Language: Java.

## Environment and commands

- Language/runtime: Java, OpenJDK 26.0.1 (compiled with `--release 17`); Python 3 for the launcher.
- Machine: macOS 26.6.2 (Darwin 25.6.0), Apple A18 Pro, 8 GB RAM.
- Build, setup and public checks (from the repository root):
  - `python tools/run.py --setup`
  - `python tools/run.py --check` (44 public cases + repeated batch: all PASS)
- Own tests (after the build above has produced `Java/build`):
  - `python tests/student/run_cases.py` — hand-written cases in `tests/student/cases.json` (15 PASS)
  - `python tests/student/random_stress.py <tmp.jsonl>` — 2400 random requests compared with brute force (0 mismatches)
  - `java -Xmx384m -cp Java/build Main < tests/student/stress_big.jsonl` — maximum-size inputs, finishes in about 0.4 s
  - Benchmark: `javac -cp Java/build -d out tests/student/bench/Bench.java && java -cp Java/build:out Bench`

## Algorithm explanations

Notation: n, m are input sizes as in the contracts; z is the number of reported starts.

### lcs
- State: `dp[i][j]` = LCS length of `a[0..i)` and `b[0..j)`. Base: `dp[0][*] = dp[*][0] = 0`.
- Transition: equal last characters → `dp[i-1][j-1]+1`; otherwise `max(dp[i-1][j], dp[i][j-1])`.
- Order: increasing i, then j, so all three neighbours are already computed.
- Correctness: if the last characters match, taking them in the LCS is never worse; otherwise at least one of them is unused.
- Reconstruction: walk back from `(|a|,|b|)`; on a match emit the character and go diagonally, else move to the larger neighbour (ties: decrease i). The string is reversed at the end. Any tie choice yields an optimum.
- Cost: O(|a||b|) time and memory (table of ints).
- Boundary test: `lcs-both-empty` / `lcs-disjoint` (empty answer); the `tea_time`/`team` example shows the answer is a subsequence, not a substring.

### merge
- State: `dp[l][r]` = minimum cost of merging blocks l..r into one block; `dp[i][i] = 0`.
- Transition: `dp[l][r] = min_k dp[l][k] + dp[k+1][r] + sum(l..r)` (the last merge splits at k); sums come from prefix sums in `long`.
- Order: by increasing interval length, so both sub-intervals are ready.
- Reconstruction: `split[l][r]` stores the best k; recursion (depth ≤ 200) emits the left child plan, the right child plan, then `[l,k,r]`, so children are chronologically before their parent.
- Cost: O(n³) time, O(n²) memory. n ≤ 1 returns cost 0 and an empty plan.
- Boundary test: `merge-adjacency` sizes `[5,1,1,5]` → 21; `merge-64bit` (public) and the 200 × 10⁹ stress case check 64-bit sums.

### tree
- State per vertex v: `take[v]` (v chosen, best weight of its subtree) and `skip[v]` (v not chosen).
- Transition: `take[v] = w[v] + Σ skip[c]`, `skip[v] = Σ max(take[c], skip[c])`.
- Order: BFS order from vertex 0 on an adjacency array, processed in reverse, which is a postorder (children before parent) without recursion, so a chain of 100000 vertices is safe.
- Reconstruction: walk in BFS order; a vertex is selected only if its parent is not selected and `take[v] > skip[v]`. Answer weight = `max(take[0], skip[0])`; the empty set is allowed, so all-negative input gives 0.
- Cost: O(n) time and memory. n = 0 returns weight 0 and `[]`.
- Boundary test: `tree-star-negative-centre`, `tree-single-vertex`, and the 100000-vertex chain in `stress_big.jsonl` (detects recursion overflow).

### naive
- For every start s in 0..n−m compare characters left to right. Empty pattern: every s in 0..n is a match (loop condition `s + m <= n`); a longer pattern gives no iterations.
- Cost: O(nm + n + 1) time, O(1) auxiliary memory.
- Boundary test: `naive-last-start` (match at the last valid start n−m).

### kmp
- `pi[i]` = length of the longest proper prefix of `p[0..i]` that is also its suffix.
- The search keeps k = matched length. On a mismatch k falls back to `pi[k-1]`; after a full match (k = m) the same fallback is applied before the next character, which keeps overlapping matches. Empty pattern is handled before any indexing.
- Correctness: invariant — k is the longest prefix of p that is a suffix of the text read so far; amortised fallback analysis gives O(n + m) steps.
- Cost: O(n + m + z + 1) time, O(m + 1) auxiliary memory.
- Boundary test: `kmp-fallback-after-match` (`aaaa`, `aa` → 0, 1, 2).

### rk
- Hash `H(s) = Σ s[i]·31^(len-1-i) mod 101` over ASCII codes; the leftmost character has the highest power. Roll: `h = ((h − t[s]·31^(m-1)) mod 101 + 101) mod 101`, then `h = (h·31 + t[s+m]) mod 101`.
- Equal hashes are only candidates: characters are compared before a start is reported. m = 0 and m > n are handled before a window is built.
- Cost: O(n + m + c·m) where c = number of equal-hash windows; with modulus 101 this can approach O(nm), so no linear worst case is claimed. Memory: O(1) auxiliary plus output.
- Boundary test: `rk-collision-ab-dj` — `ab` and `dj` both hash to 75, so a version without verification would wrongly report 0.

### suffix
- Prefix doubling: `rank[i]` = class of the first k characters of suffix i. In each round the suffixes are sorted by the pair `(rank[i], rank[i+k])` (missing second key = −1) using two stable counting-sort passes, and ranks are recomputed. It stops when all ranks are distinct. O(n log n) time, O(n) memory; no suffix strings are built.
- Kasai: `rk` is the inverse of SA; processing i in text order, `h` is the LCP with the previous suffix in SA order. Moving from suffix i to i+1 reduces the LCP by at most 1, so `h` is decremented (not reset) and total work is O(n). `lcp[0] = 0`.
- Repeated substring: the longest substring occurring at two starts equals the maximum adjacent LCP, since two suffixes sharing a prefix also share it with every suffix between them in sorted order. The start of the maximum is remembered, so the answer is `text.substring(pos, pos+max)`; overlaps are allowed (`ana` in `banana`).
- Boundary test: `suffix-no-repeat` (`abc` → `repeated = ""`), empty text, and `a`×50000 (many equal ranks, largest LCP values).

### aho
- Trie over the 95-character alphabet (codes 32–126) stored in a flat `int[states*95]`; the terminal state of every pattern index is remembered, so duplicate patterns share one state but keep separate columns.
- BFS computes failure links and fills missing transitions with `go[fail[v]][c]` (full automaton).
- For each text a fresh `visit[]` array counts visits per state; then states are processed in reverse BFS order adding `visit[u]` to `visit[fail[u]]`. Thus `visit[state]` is the number of occurrences of every pattern ending in that state, including patterns that are suffixes of longer matched strings (`he` inside `she`). The answer for pattern i is `visit[term[i]]`.
- Cost: build O(L + 95·S + p); per text O(|text| + S + p); output O(q·p). No per-match enumeration, so dense inputs stay fast.
- Boundary test: `aho-nested-patterns` (`a`,`aa`,`aaa` on `aaaa` → 4,3,2), `aho-reset-between-texts`, empty dictionary and empty texts (public tests).

## Own tests

|Command|Input or fixture path|Expected result|Actual result / log|What it checks|
|---|---|---|---|---|
|`python tools/run.py --check`|`tests/public.json`|44 cases and batch repeated twice pass|all PASS|public contract, state reset between requests|
|`python tests/student/run_cases.py`|`tests/student/cases.json` (15 cases)|`expect` fields match|15 PASS|boundary cases per algorithm above|
|`python tests/student/random_stress.py /tmp/x.jsonl`|generated, seed 7, 300 × 8 task types|equal to brute force (LCS length, merge cost, tree weight by subset enumeration, search positions, suffix array by sorting, LCP recomputed, Aho counts)|checked 2400, bad 0|general correctness of every task|
|`java -Xmx384m -cp Java/build Main < tests/student/stress_big.jsonl`|`tests/student/stress_big.jsonl`|completes, no crash/stack overflow, fast|9 results, about 0.4 s total|100000-vertex chain, suffix on `a`×50000 and `ab`×25000, dense Aho (10000 equal patterns × 100 texts), KMP 200000/100000, merge 200 × 10⁹ (cost 1544000000000, exceeds 32 bits), LCS 1000×1000, RK 10000/2000|

## Search comparison

Inputs are identical for naive/KMP/RK; `Bench.java` checks that all three return the same result as naive (column "Correct outputs checked"). Random case: text over `{a,b,c,d}` from `java.util.Random(12345)`, pattern = the substring at n/2 of length m (one planted occurrence). Repetitive case: text `a`×n, pattern `a`×(m−1) + `b` (no match; worst case for naive). Timing scope: only the `Algorithms.*` call on an already parsed request (no JSON, no compilation); 200 warm-up iterations of each algorithm per size for the JIT, then 3 runs, each run being the mean of 20 calls. Unit: microseconds per call. Median of the 3 runs shown.

|Case|n|m|Seed/generator|Algorithm|Run 1|Run 2|Run 3|Median|Correct outputs checked|
|---|---:|---:|---|---|---:|---:|---:|---:|---|
|random|300|30|seed 12345|naive|3|3|4|3|yes|
|random|300|30|seed 12345|kmp|4|4|4|4|yes|
|random|300|30|seed 12345|rk|4|4|4|4|yes|
|random|1000|100|seed 12345|naive|13|13|12|13|yes|
|random|1000|100|seed 12345|kmp|15|14|15|15|yes|
|random|1000|100|seed 12345|rk|16|16|16|16|yes|
|random|3000|300|seed 12345|naive|4|4|5|4|yes|
|random|3000|300|seed 12345|kmp|6|5|5|5|yes|
|random|3000|300|seed 12345|rk|40|34|32|34|yes|
|repetitive|300|30|seed 12345|naive|6|6|6|6|yes|
|repetitive|300|30|seed 12345|kmp|0|0|1|0|yes|
|repetitive|300|30|seed 12345|rk|2|2|2|2|yes|
|repetitive|1000|100|seed 12345|naive|81|85|87|85|yes|
|repetitive|1000|100|seed 12345|kmp|3|3|3|3|yes|
|repetitive|1000|100|seed 12345|rk|10|10|10|10|yes|
|repetitive|3000|300|seed 12345|naive|639|633|649|639|yes|
|repetitive|3000|300|seed 12345|kmp|8|8|8|8|yes|
|repetitive|3000|300|seed 12345|rk|28|28|29|28|yes|

Observations (not a proof of asymptotic complexity): on random text almost every naive window fails at the first character, so naive is about as fast as KMP and RK. The random timings are tiny and not monotone in n (naive at 3000 is faster than at 1000), which is likely JIT/measurement noise at microsecond scale; this was not investigated further. On the repetitive input naive does about (n−m+1)·(m−1) comparisons and its time grows roughly quadratically (85 → 639 µs) while KMP stays near-linear (3 → 8 µs). RK with modulus 101 gets false candidates: in the random 3000/300 case it is slower than KMP, plausibly because colliding windows cost an extra O(m) verification (not verified by counting candidates). With only 101 residues about 1 in 101 unrelated windows is expected to collide, so this tiny modulus makes the c·m term visible; this says nothing about production hashes with large moduli. The known collision case (`ab` vs `dj`) is a correctness test (`rk-collision-ab-dj` and public `rk-collision`).

## Sources and AI assistance

