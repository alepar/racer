package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Racer;
import ru.alepar.racer.Resettable;

import java.util.HashMap;
import java.util.Map;

public class LongWrites {

    public static void main(String[] args) throws Exception {
        final Map<Long, Integer> results = new HashMap<>();
        final Race<Input, Result> race = new Race<>(Config.ITERATIONS_TOTAL, Config.ITERATIONS_PER_BATCH, Input.class, Result.class, true,
                (result) -> {
                    Integer count = results.get(result.r1);
                    if (count == null) {
                        count = 0;
                    }
                    results.put(result.r1, count+1);
                },
                (input, result) -> {
                    input.i = -1;
                },
                (input, result) -> {
                    result.r1 = input.i;
                }
        );
        final long start = System.nanoTime();
        race.run();
        final long end = System.nanoTime();
        System.out.println(String.format("taken %.2fs", (end-start)/1_000_000/1000.0));
        System.out.println(results);
    }

    public static class Input implements Resettable {
        long i;

        @Override
        public void reset() {
            i=0;
        }
    }

    public static class Result implements Resettable {

        long r1;

        @Override
        public void reset() {
            r1 = 0;
        }

    }

}
