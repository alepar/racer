import api.Race;
import api.TwoRacers;

public class IncorrectLabeling {

    public static void main(String[] args) throws Exception {
        final Race<State> race = new Race<State>(State.class, new Racers(), 20000000);

        final long start = System.currentTimeMillis();
        race.run();
        final long end = System.currentTimeMillis();
        System.out.println(String.format("taken %.2fs", (end-start)/1000.0));
    }

    private static class Racers implements TwoRacers<State> {
        @Override
        public void one(State state) {
            state.y = 1;
            state.x = 1;
        }

        @Override
        public void two(State state) {
            int t = state.y;
            state.r1 = state.x;
            state.r2 = state.y;
        }
    }

    public static class State {
        volatile int x;
        int y;

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
            State state = (State) o;
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
