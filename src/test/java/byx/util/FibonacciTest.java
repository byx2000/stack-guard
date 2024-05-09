package byx.util;

import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static byx.util.StackGuard.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FibonacciTest {
    @Test
    public void testFibonacci() {
        for (int i = 1; i <= 15; i++) {
            int ans = fib1(i);
            fib2(i, r -> assertEquals(ans, r));
            int temp = i;
            run(() -> fib3(temp, r -> assertEquals(ans, r)), 1000);
        }
    }

    private int fib1(int n) {
        if (n == 1 || n == 2) {
            return n;
        }
        return fib1(n - 1) + fib1(n - 2);
    }

    private void fib2(int n, Consumer<Integer> cont) {
        if (n == 1 || n == 2) {
            cont.accept(n);
            return;
        }
        fib2(n - 1, a ->
            fib2(n - 2, b ->
                cont.accept(a + b)));
    }

    private void fib3(int n, Cont<Integer> cont) {
        guard(() -> fib3(n, cont));
        if (n == 1 || n == 2) {
            cont.accept(n);
            return;
        }
        fib3(n - 1, a ->
            fib3(n - 2, b ->
                cont.accept(a + b)));
    }
}
