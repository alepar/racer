package api;

public interface Racer<I, R> {
    void go(I input, R result);
}
