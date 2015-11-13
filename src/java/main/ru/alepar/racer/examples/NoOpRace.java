package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Resettable;

public class NoOpRace {

    public static void main(String[] args) throws Exception {
        final Race<Input, Result> race = new Race<>(100_000_000, 10_000, Input.class, Result.class, true,
                (Result result) -> {

                },
                (Input input, Result result) -> {
                    Thread.yield();
                },
                (Input input, Result result) -> {
                    Thread.yield();
                }
        );
        final long start = System.nanoTime();
        race.run();
        final long end = System.nanoTime();
        System.out.println(String.format("taken %.2fs", (end-start)/1_000_000/1000.0));
    }

    public static class Input implements Resettable {
        @Override
        public void reset() {

        }
    }

    public static class Result implements Resettable {
        @Override
        public void reset() {

        }
    }

}
