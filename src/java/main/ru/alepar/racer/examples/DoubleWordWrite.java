package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Resettable;

import java.util.Arrays;

public class DoubleWordWrite {

    public static void main(String[] args) throws Exception {
        final int[] results = new int[4];
        final Race<Input, Result> race = new Race<>(Config.ITERATIONS_TOTAL, Config.ITERATIONS_PER_BATCH, Input.class, Result.class, true,
            (result) -> {
                results[result.r] ++;
            },
            (i, o) -> {
                i.v = 1; // value
                i.k = 1; // key
            },
            (i, o) -> {
                int k = i.k;
                int v = i.v;

                o.r = (k << 1) + (v);
            }
        );
        final long start = System.nanoTime();
        race.run();
        final long end = System.nanoTime();
        System.out.println(String.format("taken %.2fs", (end-start)/1_000_000/1000.0));
        System.out.println(Arrays.toString(results));
    }

    public static class Input implements Resettable {

        int k, v;

        @Override
        public void reset() {
            v = 0;
            k = 0;
        }
    }

    public static class Result implements Resettable {

        int r;

        @Override
        public void reset() {
            r = 0;
        }

    }

}
