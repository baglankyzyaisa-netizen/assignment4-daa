import java.util.*;

/** Implementations of the Assignment 4 algorithms. docs/guide/CONTRACTS.md defines each field and limit. */
public final class Algorithms {
    private static Map<String, Object> result() { return new LinkedHashMap<>(); }

    private static List<Object> toList(int[] a, int len) {
        List<Object> r = new ArrayList<>(len);
        for (int i = 0; i < len; i++) r.add((long) a[i]);
        return r;
    }

    /** LCS: dp[i][j] = LCS length of a[0..i) and b[0..j). */
    static Map<String, Object> lcs(Map<String, Object> input) {
        String a = (String) input.get("a"), b = (String) input.get("b");
        int n = a.length(), m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 1; i <= n; i++)
            for (int j = 1; j <= m; j++)
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? dp[i - 1][j - 1] + 1 : Math.max(dp[i - 1][j], dp[i][j - 1]);
        StringBuilder sb = new StringBuilder();
        int i = n, j = m;
        while (i > 0 && j > 0) {
            if (a.charAt(i - 1) == b.charAt(j - 1)) { sb.append(a.charAt(i - 1)); i--; j--; }
            else if (dp[i - 1][j] >= dp[i][j - 1]) i--;
            else j--;
        }
        Map<String, Object> r = result();
        r.put("length", (long) dp[n][m]);
        r.put("sequence", sb.reverse().toString());
        return r;
    }

    private static int[][] mergeSplit;

    private static void buildPlan(int l, int r, List<Object> plan) {
        if (l >= r) return;
        int k = mergeSplit[l][r];
        buildPlan(l, k, plan);
        buildPlan(k + 1, r, plan);
        plan.add(Arrays.asList((long) l, (long) k, (long) r));
    }

    /** Interval DP: dp[l][r] = min over k of dp[l][k] + dp[k+1][r] + sum(l..r). */
    static Map<String, Object> merge(Map<String, Object> input) {
        List<?> sz = (List<?>) input.get("sizes");
        int n = sz.size();
        Map<String, Object> res = result();
        List<Object> plan = new ArrayList<>();
        if (n <= 1) {
            res.put("cost", 0L);
            res.put("plan", plan);
            return res;
        }
        long[] pre = new long[n + 1];
        for (int i = 0; i < n; i++) pre[i + 1] = pre[i] + ((Number) sz.get(i)).longValue();
        long[][] dp = new long[n][n];
        int[][] split = new int[n][n];
        for (int len = 2; len <= n; len++) {
            for (int l = 0; l + len - 1 < n; l++) {
                int r = l + len - 1;
                long best = Long.MAX_VALUE;
                int bk = l;
                for (int k = l; k < r; k++) {
                    long c = dp[l][k] + dp[k + 1][r];
                    if (c < best) { best = c; bk = k; }
                }
                dp[l][r] = best + pre[r + 1] - pre[l];
                split[l][r] = bk;
            }
        }
        mergeSplit = split;
        buildPlan(0, n - 1, plan);
        mergeSplit = null;
        res.put("cost", dp[0][n - 1]);
        res.put("plan", plan);
        return res;
    }

    /** Tree DP with take/skip, iterative (BFS order processed in reverse). */
    static Map<String, Object> tree(Map<String, Object> input) {
        int n = ((Number) input.get("n")).intValue();
        Map<String, Object> res = result();
        if (n == 0) {
            res.put("weight", 0L);
            res.put("selected", new ArrayList<>());
            return res;
        }
        List<?> ws = (List<?>) input.get("weights");
        List<?> es = (List<?>) input.get("edges");
        long[] w = new long[n];
        for (int i = 0; i < n; i++) w[i] = ((Number) ws.get(i)).longValue();
        int[] head = new int[n], nxt = new int[2 * es.size()], to = new int[2 * es.size()];
        Arrays.fill(head, -1);
        int ec = 0;
        for (Object o : es) {
            List<?> e = (List<?>) o;
            int u = ((Number) e.get(0)).intValue(), v = ((Number) e.get(1)).intValue();
            to[ec] = v; nxt[ec] = head[u]; head[u] = ec++;
            to[ec] = u; nxt[ec] = head[v]; head[v] = ec++;
        }
        int[] parent = new int[n], order = new int[n];
        parent[0] = -1;
        int qh = 0, qt = 0;
        order[qt++] = 0;
        while (qh < qt) {
            int v = order[qh++];
            for (int e = head[v]; e != -1; e = nxt[e]) {
                int u = to[e];
                if (u != parent[v]) { parent[u] = v; order[qt++] = u; }
            }
        }
        long[] take = new long[n], skip = new long[n];
        for (int i = 0; i < n; i++) take[i] = w[i];
        for (int idx = n - 1; idx >= 0; idx--) {
            int v = order[idx];
            int p = parent[v];
            if (p >= 0) {
                take[p] += skip[v];
                skip[p] += Math.max(take[v], skip[v]);
            }
        }
        boolean[] chosen = new boolean[n];
        List<Object> sel = new ArrayList<>();
        for (int idx = 0; idx < n; idx++) {
            int v = order[idx];
            boolean parentTaken = parent[v] >= 0 && chosen[parent[v]];
            if (!parentTaken && take[v] > skip[v]) {
                chosen[v] = true;
                sel.add((long) v);
            }
        }
        res.put("weight", Math.max(take[0], skip[0]));
        res.put("selected", sel);
        return res;
    }

    /** Naive search: try every start. */
    static Map<String, Object> naive(Map<String, Object> input) {
        String t = (String) input.get("text"), p = (String) input.get("pattern");
        int n = t.length(), m = p.length();
        List<Object> pos = new ArrayList<>();
        for (int s = 0; s + m <= n; s++) {
            int j = 0;
            while (j < m && t.charAt(s + j) == p.charAt(j)) j++;
            if (j == m) pos.add((long) s);
        }
        Map<String, Object> r = result();
        r.put("positions", pos);
        return r;
    }

    /** KMP with prefix function. */
    static Map<String, Object> kmp(Map<String, Object> input) {
        String t = (String) input.get("text"), p = (String) input.get("pattern");
        int n = t.length(), m = p.length();
        List<Object> pos = new ArrayList<>();
        if (m == 0) {
            for (int i = 0; i <= n; i++) pos.add((long) i);
        } else {
            int[] pi = new int[m];
            for (int i = 1, k = 0; i < m; i++) {
                while (k > 0 && p.charAt(i) != p.charAt(k)) k = pi[k - 1];
                if (p.charAt(i) == p.charAt(k)) k++;
                pi[i] = k;
            }
            for (int i = 0, k = 0; i < n; i++) {
                while (k > 0 && (k == m || t.charAt(i) != p.charAt(k))) k = pi[k - 1];
                if (t.charAt(i) == p.charAt(k)) k++;
                if (k == m) pos.add((long) (i - m + 1));
            }
        }
        Map<String, Object> r = result();
        r.put("positions", pos);
        return r;
    }

    /** Rabin-Karp, base 31, mod 101, with character verification. */
    static Map<String, Object> rk(Map<String, Object> input) {
        String t = (String) input.get("text"), p = (String) input.get("pattern");
        int n = t.length(), m = p.length();
        final long B = 31, M = 101;
        List<Object> pos = new ArrayList<>();
        if (m == 0) {
            for (int i = 0; i <= n; i++) pos.add((long) i);
        } else if (m <= n) {
            long high = 1;
            for (int i = 0; i < m - 1; i++) high = high * B % M;
            long hp = 0, ht = 0;
            for (int i = 0; i < m; i++) {
                hp = (hp * B + p.charAt(i)) % M;
                ht = (ht * B + t.charAt(i)) % M;
            }
            for (int s = 0; ; s++) {
                if (hp == ht) {
                    int j = 0;
                    while (j < m && t.charAt(s + j) == p.charAt(j)) j++;
                    if (j == m) pos.add((long) s);
                }
                if (s + m >= n) break;
                ht = ((ht - t.charAt(s) * high) % M + M) % M;
                ht = (ht * B + t.charAt(s + m)) % M;
            }
        }
        Map<String, Object> r = result();
        r.put("positions", pos);
        return r;
    }

    /** Suffix array by prefix doubling (radix sort of rank pairs) plus Kasai LCP. */
    static Map<String, Object> suffix(Map<String, Object> input) {
        String s = (String) input.get("text");
        int n = s.length();
        Map<String, Object> res = result();
        if (n == 0) {
            res.put("sa", new ArrayList<>());
            res.put("lcp", new ArrayList<>());
            res.put("repeated", "");
            return res;
        }
        int[] sa = new int[n], rank = new int[n], tmp = new int[n], sa2 = new int[n];
        for (int i = 0; i < n; i++) { sa[i] = i; rank[i] = s.charAt(i); }
        int maxR = Math.max(n, 256) + 1;
        int[] cnt = new int[maxR + 1];
        // initial sort by first character
        for (int i = 0; i < n; i++) cnt[rank[i] + 1]++;
        for (int i = 1; i <= maxR; i++) cnt[i] += cnt[i - 1];
        for (int i = 0; i < n; i++) sa[cnt[rank[i]]++] = i;
        int[] r0 = new int[n];
        r0[sa[0]] = 0;
        for (int i = 1; i < n; i++) r0[sa[i]] = r0[sa[i - 1]] + (s.charAt(sa[i]) != s.charAt(sa[i - 1]) ? 1 : 0);
        rank = r0;
        for (int k = 1; rank[sa[n - 1]] < n - 1; k <<= 1) {
            // sort by second key: suffixes with i+k>=n come first
            int idx = 0;
            for (int i = n - k; i < n; i++) if (i >= 0) sa2[idx++] = i;
            for (int i = 0; i < n; i++) if (sa[i] >= k) sa2[idx++] = sa[i] - k;
            // stable counting sort by first key
            Arrays.fill(cnt, 0);
            for (int i = 0; i < n; i++) cnt[rank[i] + 1]++;
            for (int i = 1; i <= maxR; i++) cnt[i] += cnt[i - 1];
            for (int i = 0; i < n; i++) sa[cnt[rank[sa2[i]]]++] = sa2[i];
            tmp[sa[0]] = 0;
            for (int i = 1; i < n; i++) {
                int a = sa[i - 1], b = sa[i];
                int a2 = a + k < n ? rank[a + k] : -1, b2 = b + k < n ? rank[b + k] : -1;
                tmp[b] = tmp[a] + (rank[a] != rank[b] || a2 != b2 ? 1 : 0);
            }
            int[] sw = rank; rank = tmp; tmp = sw;
        }
        int[] rk = new int[n];
        for (int i = 0; i < n; i++) rk[sa[i]] = i;
        int[] lcp = new int[n];
        int h = 0, best = 0, bestPos = 0;
        for (int i = 0; i < n; i++) {
            if (rk[i] > 0) {
                int j = sa[rk[i] - 1];
                while (i + h < n && j + h < n && s.charAt(i + h) == s.charAt(j + h)) h++;
                lcp[rk[i]] = h;
                if (h > best) { best = h; bestPos = i; }
                if (h > 0) h--;
            } else {
                h = 0;
            }
        }
        res.put("sa", toList(sa, n));
        res.put("lcp", toList(lcp, n));
        res.put("repeated", s.substring(bestPos, bestPos + best));
        return res;
    }

    /** Aho-Corasick with flat 95-symbol goto table and reverse-failure count propagation. */
    static Map<String, Object> aho(Map<String, Object> input) {
        List<?> pats = (List<?>) input.get("patterns");
        List<?> texts = (List<?>) input.get("texts");
        int p = pats.size();
        int total = 0;
        for (Object o : pats) total += ((String) o).length();
        final int A = 95;
        int maxStates = total + 1;
        int[] go = new int[maxStates * A];
        Arrays.fill(go, -1);
        int states = 1;
        int[] term = new int[p];
        for (int i = 0; i < p; i++) {
            String w = (String) pats.get(i);
            int v = 0;
            for (int c = 0; c < w.length(); c++) {
                int ch = w.charAt(c) - 32;
                if (go[v * A + ch] == -1) go[v * A + ch] = states++;
                v = go[v * A + ch];
            }
            term[i] = v;
        }
        int[] fail = new int[states], order = new int[states];
        int qh = 0, qt = 0;
        for (int ch = 0; ch < A; ch++) {
            int u = go[ch];
            if (u == -1) go[ch] = 0;
            else { fail[u] = 0; order[qt++] = u; }
        }
        while (qh < qt) {
            int v = order[qh++];
            for (int ch = 0; ch < A; ch++) {
                int u = go[v * A + ch];
                if (u == -1) go[v * A + ch] = go[fail[v] * A + ch];
                else { fail[u] = go[fail[v] * A + ch]; order[qt++] = u; }
            }
        }
        List<Object> rows = new ArrayList<>();
        for (Object o : texts) {
            String t = (String) o;
            long[] visit = new long[states];
            int v = 0;
            for (int i = 0; i < t.length(); i++) {
                v = go[v * A + (t.charAt(i) - 32)];
                visit[v]++;
            }
            for (int i = qt - 1; i >= 0; i--) {
                int u = order[i];
                visit[fail[u]] += visit[u];
            }
            List<Object> row = new ArrayList<>(p);
            for (int i = 0; i < p; i++) row.add(visit[term[i]]);
            rows.add(row);
        }
        Map<String, Object> r = result();
        r.put("counts", rows);
        return r;
    }
}
