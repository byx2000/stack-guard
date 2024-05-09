package byx.util;

/**
 * 堆栈保护
 */
public class StackGuard {
    private static final ThreadLocal<Integer> CURRENT_DEPTH_HOLDER = new ThreadLocal<>();

    /**
     * 运行堆栈保护程序
     *
     * @param runnable 待运行程序
     * @param maxDepth 当递归深度达到该值时，强制跳出多层递归并重新执行当前程序
     */
    public static void run(Runnable runnable, int maxDepth) {
        try {
            while (true) {
                try {
                    CURRENT_DEPTH_HOLDER.set(maxDepth);
                    runnable.run();
                    return;
                } catch (ExitRecursionException e) {
                    runnable = e.getRunnable();
                }
            }
        } finally {
            CURRENT_DEPTH_HOLDER.remove();
        }
    }

    /**
     * 检查当前递归深度，如果超过最大值则跳出多层递归
     *
     * @param runnable 跳出递归后重新运行的代码
     */
    public static void guard(Runnable runnable) {
        int depth = CURRENT_DEPTH_HOLDER.get();
        if (depth <= 0) {
            throw new ExitRecursionException(runnable);
        }
        CURRENT_DEPTH_HOLDER.set(depth - 1);
    }
}
