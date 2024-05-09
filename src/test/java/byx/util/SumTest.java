package byx.util;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static byx.util.StackGuard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SumTest {
    @Test
    public void testSum() {
        for (int i = 1; i <= 100; i++) {
            long ans = sum1(i);
            sum2(i, r -> assertEquals(ans, r));
            int temp = i;
            run(() -> sum3(temp, r -> assertEquals(ans, r)), 1000);
        }

        assertThrows(StackOverflowError.class, () -> sum1(1000000));
        assertThrows(StackOverflowError.class, () -> sum2(1000000, System.out::println));
        run(() -> sum3(1000000, r -> assertEquals(500000500000L, r)), 1000);
    }

    private long sum1(long n) {
        if (n == 1) {
            return 1;
        }
        return sum1(n - 1) + n;
    }

    private void sum2(long n, Consumer<Long> cont) {
        if (n == 1) {
            cont.accept(1L);
            return;
        }
        sum2(n - 1, r -> cont.accept(n + r));
    }

    private void sum3(long n, Cont<Long> cont) {
        guard(() -> sum3(n, cont));
        if (n == 1) {
            cont.accept(1L);
            return;
        }
        sum3(n - 1, r -> cont.accept(n + r));
    }
}
