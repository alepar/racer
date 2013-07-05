import api.Race;
import api.Racer;

public class IncorrectLabeling {

    public static void main(String[] args) throws Exception {
        final Race<Input, Result> race = new Race<Input, Result>(20000000, Input.class, Result.class,
            new Racer<Input, Result>() {
                @Override
                public void go(Input input, Result result) {
                    input.y = 1;
                    input.x = 1;
                }
            },
            new Racer<Input, Result>() {
                @Override
                public void go(Input input, Result result) {
                    int t = input.y;
                    result.r1 = input.x;
                    result.r2 = input.y;
                }
            }
        );

        final long start = System.currentTimeMillis();
        race.run();
        final long end = System.currentTimeMillis();
        System.out.println(String.format("taken %.2fs", (end-start)/1000.0));
    }

    public static class Input {
        volatile int x;
        int y;
    }

    public static class Result {
        int r1;
        int r2;

        @Override
        public String toString() {
            return r1 + ", " + r2;
        }

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
    }

}
