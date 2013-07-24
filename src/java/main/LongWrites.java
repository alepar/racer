import api.Race;
import api.Racer;

public class LongWrites {

    public static void main(String[] args) throws Exception {
        final Race<Input, Result> race = new Race<Input, Result>(1000000, Input.class, Result.class,
            new Racer<Input, Result>() {
                @Override
                public void go(Input input, Result result) {
                    input.i = -1;
                }
            },
            new Racer<Input, Result>() {
                @Override
                public void go(Input input, Result result) {
                    result.r1 = input.i;
                }
            }
        );
        final long start = System.currentTimeMillis();
        race.run();
        final long end = System.currentTimeMillis();
        System.out.println(String.format("taken %.2fs", (end-start)/1000.0));
    }

    public static class Input {
        long i;
    }

    public static class Result {

        long r1;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Result result = (Result) o;

            return r1 == result.r1;
        }

        @Override
        public int hashCode() {
            return (int) (r1 ^ (r1 >>> 32));
        }

        @Override
        public String toString() {
            return "" + r1;
        }
    }

}
