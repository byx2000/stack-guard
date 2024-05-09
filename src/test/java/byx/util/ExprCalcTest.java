package byx.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static byx.util.StackGuard.guard;
import static byx.util.StackGuard.run;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExprCalcTest {
    @Test
    public void testCalculator1() {
        assertEquals(2, Calculator1.eval("1+1"));
        assertEquals(14, Calculator1.eval("2*(3+4)"));
        assertEquals(45, Calculator1.eval("(2+3)*(4+5)"));
        assertEquals(23, Calculator1.eval("(1+(4+5+2)-3)+(6+8)"));
        assertEquals(-42, Calculator1.eval("((((1*10)-(3*8))*3)*1)"));
        assertEquals(-12, Calculator1.eval("(4+(((5+4)-(7-9))-(10+(9+8))))"));
        assertEquals(-12, Calculator1.eval("(2+6*3+5-(3*14/7+2)*5)+3"));
        assertEquals(-35, Calculator1.eval("5+3-4-(1+2-7+(10-1+3+5+(3-0+(8-(3+(8-(10-(6-10-8-7+(0+0+7)-10+5-3-2+(9+0+(7+(2-(2-(9)-2+5+4+2+(2+9+1+5+5-8-9-2-9+1+0)-(5-(9)-(0-(7+9)+(10+(6-4+6))+0-2+(10+7+(8+(7-(8-(3)+(2)+(10-6+10-(2)-7-(2)+(3+(8))+(1-3-8)+6-(4+1)+(6))+6-(1)-(10+(4)+(8)+(5+(0))+(3-(6))-(9)-(4)+(2))))))-1)))+(9+6)+(0))))+3-(1))+(7))))))))"));
        assertThrows(StackOverflowError.class, () -> Calculator1.eval("-".repeat(100000) + "1"));
        assertThrows(StackOverflowError.class, () -> Calculator1.eval("(".repeat(100000) + "1" + ")".repeat(100000)));
    }

    @Test
    public void testCalculator2() {
        assertEquals(2, Calculator2.eval("1+1"));
        assertEquals(14, Calculator2.eval("2*(3+4)"));
        assertEquals(45, Calculator2.eval("(2+3)*(4+5)"));
        assertEquals(23, Calculator2.eval("(1+(4+5+2)-3)+(6+8)"));
        assertEquals(-42, Calculator2.eval("((((1*10)-(3*8))*3)*1)"));
        assertEquals(-12, Calculator2.eval("(4+(((5+4)-(7-9))-(10+(9+8))))"));
        assertEquals(-12, Calculator2.eval("(2+6*3+5-(3*14/7+2)*5)+3"));
        assertEquals(-35, Calculator2.eval("5+3-4-(1+2-7+(10-1+3+5+(3-0+(8-(3+(8-(10-(6-10-8-7+(0+0+7)-10+5-3-2+(9+0+(7+(2-(2-(9)-2+5+4+2+(2+9+1+5+5-8-9-2-9+1+0)-(5-(9)-(0-(7+9)+(10+(6-4+6))+0-2+(10+7+(8+(7-(8-(3)+(2)+(10-6+10-(2)-7-(2)+(3+(8))+(1-3-8)+6-(4+1)+(6))+6-(1)-(10+(4)+(8)+(5+(0))+(3-(6))-(9)-(4)+(2))))))-1)))+(9+6)+(0))))+3-(1))+(7))))))))"));
        assertEquals(1, Calculator2.eval("-".repeat(100000) + "1"));
        assertEquals(1, Calculator2.eval("(".repeat(100000) + "1" + ")".repeat(100000)));
    }
}

class Calculator1 {
    public static int eval(String expr) {
        return evalExpr(expr, new AtomicInteger(0));
    }

    /**
     * expr = term (+|- term)*
     */
    private static int evalExpr(String expr, AtomicInteger index) {
        int res = evalTerm(expr, index);
        while (index.get() < expr.length() && (expr.charAt(index.get()) == '+' || expr.charAt(index.get()) == '-')) {
            int i = index.getAndIncrement();
            if (expr.charAt(i) == '+') {
                res += evalTerm(expr, index);
            } else {
                res -= evalTerm(expr, index);
            }
        }
        return res;
    }

    /**
     * term = fact (*|/ fact)*
     */
    private static int evalTerm(String expr, AtomicInteger index) {
        int res = evalFact(expr, index);
        while (index.get() < expr.length() && (expr.charAt(index.get()) == '*' || expr.charAt(index.get()) == '/')) {
            int i = index.getAndIncrement();
            if (expr.charAt(i) == '*') {
                res *= evalFact(expr, index);
            } else {
                res /= evalFact(expr, index);
            }
        }
        return res;
    }

    /**
     * fact = (expr)
     *      | -fact
     *      | num
     */
    private static int evalFact(String expr, AtomicInteger index) {
        if (expr.charAt(index.get()) == '(') {
            index.incrementAndGet();
            int res = evalExpr(expr, index);
            index.incrementAndGet();
            return res;
        } else if (expr.charAt(index.get()) == '-') {
            index.incrementAndGet();
            return -evalFact(expr, index);
        } else {
            return evalNum(expr, index);
        }
    }

    /**
     * num = [0-9]+
     */
    private static int evalNum(String expr, AtomicInteger index) {
        int res = 0;
        while (index.get() < expr.length() && Character.isDigit(expr.charAt(index.get()))) {
            res = res * 10 + (expr.charAt(index.get()) - '0');
            index.incrementAndGet();
        }
        return res;
    }
}

class Calculator2 {
    public static int eval(String expr) {
        AtomicInteger result = new AtomicInteger();
        run(() -> evalExpr(expr, new AtomicInteger(0), result::set), 1000);
        return result.get();
    }

    /**
     * expr = term (+|- term)*
     */
    private static void evalExpr(String expr, AtomicInteger index, Cont<Integer> cont) {
        guard(() -> evalExpr(expr, index, cont));
        evalTerm(expr, index, res -> evalExprLoop(expr, index, res, cont));
    }

    private static void evalExprLoop(String expr, AtomicInteger index, int res, Cont<Integer> cont) {
        guard(() -> evalExprLoop(expr, index, res, cont));
        if (index.get() < expr.length() && (expr.charAt(index.get()) == '+' || expr.charAt(index.get()) == '-')) {
            int i = index.getAndIncrement();
            if (expr.charAt(i) == '+') {
                evalTerm(expr, index, r -> evalExprLoop(expr, index, res + r, cont));
            } else {
                evalTerm(expr, index, r -> evalExprLoop(expr, index, res - r, cont));
            }
        } else {
            cont.accept(res);
        }
    }

    /**
     * term = fact (*|/ fact)*
     */
    private static void evalTerm(String expr, AtomicInteger index, Cont<Integer> cont) {
        guard(() -> evalTerm(expr, index, cont));
        evalFact(expr, index, res -> evalTermLoop(expr, index, res, cont));
    }

    private static void evalTermLoop(String expr, AtomicInteger index, int res, Cont<Integer> cont) {
        guard(() -> evalTermLoop(expr, index, res, cont));
        if (index.get() < expr.length() && (expr.charAt(index.get()) == '*' || expr.charAt(index.get()) == '/')) {
            int i = index.getAndIncrement();
            if (expr.charAt(i) == '*') {
                evalFact(expr, index, r -> evalTermLoop(expr, index, res * r, cont));
            } else {
                evalFact(expr, index, r -> evalTermLoop(expr, index, res / r, cont));
            }
        } else {
            cont.accept(res);
        }
    }

    /**
     * fact = (expr)
     *      | -fact
     *      | num
     */
    private static void evalFact(String expr, AtomicInteger index, Cont<Integer> cont) {
        guard(() -> evalFact(expr, index, cont));
        if (expr.charAt(index.get()) == '(') {
            index.incrementAndGet();
            evalExpr(expr, index, res -> {
                index.incrementAndGet();
                cont.accept(res);
            });
        } else if (expr.charAt(index.get()) == '-') {
            index.incrementAndGet();
            evalFact(expr, index, res -> cont.accept(-res));
        } else {
            evalNum(expr, index, cont);
        }
    }

    /**
     * num = [0-9]+
     */
    private static void evalNum(String expr, AtomicInteger index, Cont<Integer> cont) {
        guard(() -> evalNum(expr, index, cont));
        int res = 0;
        while (index.get() < expr.length() && Character.isDigit(expr.charAt(index.get()))) {
            res = res * 10 + (expr.charAt(index.get()) - '0');
            index.incrementAndGet();
        }
        cont.accept(res);
    }
}
