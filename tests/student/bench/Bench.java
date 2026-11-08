import java.util.*;
/** Run from repo root: java -cp Java/build:tests/student/bench/out Bench (compiled with -cp Java/build). */
public class Bench {
    static Map<String,Object> req(String t, String p) { Map<String,Object> m = new HashMap<>(); m.put("text", t); m.put("pattern", p); return m; }
    static String gen(Random r, int n, int alpha) { StringBuilder b = new StringBuilder(); for (int i = 0; i < n; i++) b.append((char) ('a' + r.nextInt(alpha))); return b.toString(); }
    public static void main(String[] a) {
        long seed = 12345;
        int[][] sizes = {{300, 30}, {1000, 100}, {3000, 300}};
        for (String kind : new String[]{"random", "repetitive"}) {
            for (int[] s : sizes) {
                int n = s[0], m = s[1];
                Random r = new Random(seed);
                String t, p;
                if (kind.equals("random")) { t = gen(r, n, 4); p = t.substring(n / 2, n / 2 + m); }
                else { t = "a".repeat(n); p = "a".repeat(m - 1) + "b"; }
                Map<String,Object> q = req(t, p);
                Object ref = Algorithms.naive(q);
                for (int w = 0; w < 200; w++) { Algorithms.naive(q); Algorithms.kmp(q); Algorithms.rk(q); }
                String[] names = {"naive", "kmp", "rk"};
                for (String name : names) {
                    StringBuilder sb = new StringBuilder();
                    long[] ts = new long[3];
                    boolean ok = true;
                    for (int run = 0; run < 3; run++) {
                        long t0 = System.nanoTime();
                        Object out = null;
                        for (int rep = 0; rep < 20; rep++) out = name.equals("naive") ? Algorithms.naive(q) : name.equals("kmp") ? Algorithms.kmp(q) : Algorithms.rk(q);
                        ts[run] = (System.nanoTime() - t0) / 20;
                        ok &= out.equals(ref);
                    }
                    long[] c = ts.clone(); Arrays.sort(c);
                    System.out.printf("|%s|%d|%d|seed %d|%s|%d|%d|%d|%d|%s|%n", kind, n, m, seed, name, ts[0] / 1000, ts[1] / 1000, ts[2] / 1000, c[1] / 1000, ok ? "yes" : "NO");
                }
            }
        }
    }
}
