package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Resettable;

import java.util.Arrays;

public class DoubleCheckedLocking {

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static void main(String[] args) throws Exception {
        final int[] results = new int[8];
        final Race<Input, Result> race = new Race<>(50_000_000, 100_000, Input.class, Result.class, true,
                (Result result) -> {
                    final int idx = (result.bad << 2) + (result.ref << 1) + (result.c1 + result.c2 - 1);
                    results[idx]++;
                },
                (Input input, Result result) -> {
                    if (input.singleton == null) {
                        synchronized (input) {
                            if (input.singleton == null) {
                                input.singleton = create();
                                result.c1 = 1;
                            }
                        }
                    }
                },
                (Input input, Result result) -> {
                    if (input.singleton == null) {
                        synchronized (input) {
                            if (input.singleton == null) {
                                input.singleton = create();
                                result.c2 = 1;
                            }
                        }
                    }
                },

                (Input input, Result result) -> {
                    if (input.singleton != null) {
                        result.ref = 1;
                        if (input.singleton.field != 1L) {
                            result.bad = 1;
                        }
                    }
                }
        );

        final long start = System.nanoTime();
        race.run();
        final long end = System.nanoTime();

        System.out.println(String.format("taken %.2fs", (end-start)/1_000_000/1000.0));
        System.out.println(Arrays.toString(results));
    }

    public static class Singleton {
        private long field = 1;
    }

    public static Singleton create() {
        return new Singleton();
    }

    public static class Input implements Resettable {
        Singleton singleton;

        @Override
        public void reset() {
            singleton = null;
        }
    }

    public static class Result implements Resettable {

        int ref, bad;
        int c1, c2;

        @Override
        public void reset() {
            c1 = c2 = ref = bad = 0;
        }

    }
}
