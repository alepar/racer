package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Resettable;

public class NoOpRace {

    private static long blackhole = 0;

    public static void main(String[] args) throws Exception {
        final Race<Input, Result> race = new Race<>(Config.ITERATIONS_TOTAL, Config.ITERATIONS_PER_BATCH, Input.class, Result.class, true,
                (Result result) -> {
                    blackhole += result.i;
                },
                (Input input, Result result) -> {
                    result.i++;
                },
                (Input input, Result result) -> {
                    result.i++;
                }
        );
        final long start = System.nanoTime();
        race.run();
        final long end = System.nanoTime();
        System.out.println(String.format("taken %.2fs", (end-start)/1_000_000/1000.0));
        System.out.println("blackhole = " + blackhole);
    }

    public static class Input implements Resettable {
        @Override
        public void reset() { }
    }

    public static class Result implements Resettable {
        private long i;
        @Override
        public void reset() { }
    }

}
