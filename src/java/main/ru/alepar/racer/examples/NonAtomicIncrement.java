package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Racer;
import ru.alepar.racer.Resettable;

import java.util.Arrays;

public class NonAtomicIncrement {

    public static void main(String[] args) throws Exception {
        final int[] results = new int[2];
        final Race<Input, Result> race = new Race<>(10_000_000, 10_000, Input.class, Result.class, true,
            (result) -> {
                results[result.r1 + result.r2] ++;
            },
            (input, result) -> {
                result.r1 = input.i++;
            },
            (input, result) -> {
                result.r2 = input.i++;
            }
        );
        final long start = System.nanoTime();
        race.run();
        final long end = System.nanoTime();
        System.out.println(String.format("taken %.2fs", (end-start)/1_000_000/1000.0));
        System.out.println(Arrays.toString(results));
    }

    public static class Input implements Resettable {
        int i;

        @Override
        public void reset() {
            i=0;
        }
    }

    public static class Result implements Resettable {

        int r1;
        int r2;

        @Override
        public void reset() {
            r1 = r2 = 0;
        }

    }

}
