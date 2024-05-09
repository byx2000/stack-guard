package byx.util;

/**
 * continuation
 */
public interface Cont<T> {
    void doAccept(T t);

    default void accept(T t) {
        StackGuard.guard(() -> this.accept(t));
        this.doAccept(t);
    }
}