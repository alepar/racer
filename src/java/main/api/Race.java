package api;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Race<S> {

    private final S[] states;
    private final Thread one, two;
    private final TwoRacers<S> racers;

    private volatile int raceIndex=-1;
    private volatile int oneIndex=-1;
    private volatile int twoIndex=-1;

    public Race(Class<S> clazz, TwoRacers<S> racers, int iter) throws Exception {
        this.racers = racers;

        states = (S[]) Array.newInstance(clazz, iter);
        for (int i = 0; i < states.length; i++) {
            states[i] = clazz.newInstance();
        }
        one = new FirstRacer();
        two = new SecondRacer();
    }

    public void run() {
        one.start(); two.start();

        for (int iter=0; iter < states.length; iter++) {
            while(oneIndex != iter || twoIndex != iter);
            raceIndex = iter;
        }

        final Map<S, AtomicInteger> aggregated = new HashMap<S, AtomicInteger>();
        for (S state : states) {
            AtomicInteger count = aggregated.get(state);
            if (count == null) {
                count = new AtomicInteger();
                aggregated.put(state, count);
            }
            count.incrementAndGet();
        }

        for (Map.Entry<S, AtomicInteger> entry : aggregated.entrySet()) {
            System.out.println(String.format("<%s>:\t%2.4f%%", entry.getKey().toString(), 100.0*entry.getValue().get()/states.length));
        }
    }

    private class FirstRacer extends Thread {
        @Override
        public void run() {
            while (true) {
                oneIndex++;
                if (oneIndex == states.length) return;
                while(oneIndex != raceIndex);
                racers.one(states[raceIndex]);
            }
        }
    }

    private class SecondRacer extends Thread {
        @Override
        public void run() {
            while (true) {
                twoIndex++;
                if (twoIndex == states.length) return;
                while(twoIndex != raceIndex) ;
                racers.two(states[raceIndex]);
            }
        }
    }
}
