package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Racer;

public class NonAtomicIncrement {

    public static void main(String[] args) throws Exception {
        final Race<Input, Result> race = new Race<>(1000000, 10000, Input.class, Result.class, true,
            new Racer<Input, Result>() {
                @Override
                public void go(Input input, Result result) {
                    result.r1 = input.i++;
                }
            },
            new Racer<Input, Result>() {
                @Override
                public void go(Input input, Result result) {
                    result.r2 = input.i++;
                }
            }
        );
        final long start = System.currentTimeMillis();
        race.run();
        final long end = System.currentTimeMillis();
        System.out.println(String.format("taken %.2fs", (end-start)/1000.0));
    }

    public static class Input {
        int i;
    }

    public static class Result {

        int r1;
        int r2;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Result state = (Result) o;
            return r1 == state.r1 && r2 == state.r2;

        }

        @Override
        public int hashCode() {
            int result = r1;
            result = 31 * result + r2;
            return result;
        }

        @Override
        public String toString() {
            return r1 + ", " + r2;
        }
    }

}
