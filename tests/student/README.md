# Own tests — Assignment 4

Author: Baglankyzy Aisha, group SE-2537. Language: Java.

Run all commands from the repository root after `python tools/run.py --setup` (it builds `Java/build`).

| File | Purpose | Command |
|---|---|---|
| `cases.json` | 15 hand-written boundary cases with expected fields | `python tests/student/run_cases.py` |
| `run_cases.py` | runner for `cases.json` | see above |
| `random_stress.py` | 2400 random requests (seed 7) compared with brute force | `python tests/student/random_stress.py out.jsonl` |
| `stress_big.jsonl` | maximum-size inputs (100000-vertex chain, suffix on 50000 chars, dense Aho–Corasick, KMP 200000/100000, merge 200 blocks, LCS 1000x1000, RK) | `java -Xmx384m -cp Java/build Main < tests/student/stress_big.jsonl` |
| `bench/Bench.java` | naive/KMP/RK timing for the comparison in ANALYSIS.md | `javac -cp Java/build -d out tests/student/bench/Bench.java && java -cp Java/build:out Bench` |

What each case detects is explained in ANALYSIS.md (sections "Algorithm explanations" and "Own tests").
