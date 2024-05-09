package byx.util;

import org.junit.jupiter.api.Test;

import static byx.util.StackGuard.guard;
import static byx.util.StackGuard.run;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CatalanTest {
    @Test
    public void testCatalan() {
        for (int i = 0; i <= 10; i++) {
            long ans = catalan1(i);
            int temp = i;
            run(() -> catalan2(temp, r -> assertEquals(ans, r)), 1000);
        }
    }

    private static long catalan1(long n) {
        if (n == 0 || n == 1) {
            return 1;
        }

        long res = 0;
        for (int i = 0; i <= n - 1; i++) {
            res += catalan1(i) * catalan1(n - 1 - i);
        }

        return res;
    }

    private static void catalan2(long n, Cont<Long> cont) {
        guard(() -> catalan2(n, cont));
        if (n == 0 || n == 1) {
            cont.accept(1L);
            return;
        }
        doLoop(n, 0, 0, cont);
    }

    private static void doLoop(long n, int i, long res, Cont<Long> cont) {
        guard(() -> doLoop(n, i, res, cont));
        if (i == n) {
            cont.accept(res);
            return;
        }
        catalan2(i, a ->
            catalan2(n - 1 - i, b ->
                doLoop(n, i + 1, res + a * b, cont)));
    }
}
