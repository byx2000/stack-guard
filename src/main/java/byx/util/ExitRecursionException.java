package byx.util;

/**
 * 用于快速跳出多层递归，携带runnable作为跳出递归后执行的程序
 */
public class ExitRecursionException extends RuntimeException {
    private final Runnable runnable;

    public ExitRecursionException(Runnable runnable) {
        super(null, null, false, false); // 不记录异常调用栈，加快异常创建速度
        this.runnable = runnable;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
