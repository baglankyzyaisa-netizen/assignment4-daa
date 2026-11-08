# Assignment 4 — Dynamic Programming and String Processing

**Author:** Baglankyzy Aisha, group SE-2537. **Language:** Java.

Course: Design and Analysis of Algorithms.

Implemented in [Java/source/Algorithms.java](Java/source/Algorithms.java), with the supplied JSON Lines interface unchanged:

1. LCS (`lcs`)
2. Ordered adjacent-block merging (`merge`)
3. Maximum-weight independent set on a tree (`tree`)
4. Substring search: naive, KMP and exact Rabin–Karp (`naive`, `kmp`, `rk`)
5. Suffix array with Kasai LCP (`suffix`)
6. Aho–Corasick (`aho`)

## Build and check

Requires JDK 17+ and Python 3.12+. From the repository root:

```
python tools/run.py --setup
python tools/run.py --check
```

Own tests are in [tests/student](tests/student/README.md). The report (correctness, complexity, test log and the naive/KMP/RK comparison) is in [ANALYSIS.md](ANALYSIS.md).
