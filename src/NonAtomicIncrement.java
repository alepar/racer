import api.Race;
import api.TwoRacers;

public class NonAtomicIncrement {

    public static void main(String[] args) throws Exception {
        final Race<State> race = new Race<State>(State.class, new Racers(), 20000000);
        final long start = System.currentTimeMillis();
        race.run();
        final long end = System.currentTimeMillis();
        System.out.println(String.format("taken %.2fs", (end-start)/1000.0));
    }

    public static class State {
        int i;
        int r1;
        int r2;

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

        @Override
        public String toString() {
            return r1 + ", " + r2;
        }
    }

    private static class Racers implements TwoRacers<State> {
        @Override
        public void one(State state) {
            state.r1 = state.i++;
        }

        @Override
        public void two(State state) {
            state.r2 = state.i++;
        }
    }
}
