package ru.alepar.racer;

import com.google.common.collect.Maps;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("StatementWithEmptyBody")
public class Race<I, R> {

    private final I[] inputs;
    private final R[] results;

    private final RacerThread[] racerThreads;

    private volatile int raceIndex=-1;
    private int lastStatus;

    public Race(int iter, Class<I> inputClass, Class<R> resultClass, Racer<I, R>... racers) throws Exception {
        inputs = createAndFillArray(iter, inputClass);
        results = createAndFillArray(iter, resultClass);
        racerThreads = createRacerThreads(racers);
    }

    public void run() {
        for (RacerThread thread : racerThreads) {
            thread.start();
        }

        lastStatus = 0;
        for (int iter=0; iter <= inputs.length; iter++) {
            while(!allThreadsAt(iter));
            raceIndex = iter;
            reportProgress();
        }

        final Map<R, AtomicInteger> aggregated = Maps.newHashMap();
        for (R result : results) {
            AtomicInteger count = aggregated.get(result);
            if (count == null) {
                count = new AtomicInteger();
                aggregated.put(result, count);
            }
            count.incrementAndGet();
        }

        for (Map.Entry<R, AtomicInteger> entry : aggregated.entrySet()) {
            System.out.println(String.format("<%s>:\t%2.4f%%", entry.getKey().toString(), 100.0*entry.getValue().get()/results.length));
        }
    }

    private void reportProgress() {
        final int status = (int)(10000.0 * raceIndex / inputs.length);
        if (status != lastStatus) {
            System.out.print(String.format("race progress %.2f%%   \r", status / 100.0));
            lastStatus = status;
        }
    }

    @SuppressWarnings("unchecked")
    private RacerThread[] createRacerThreads(Racer<I, R>[] racers) {
        final RacerThread[] threads = (RacerThread[]) Array.newInstance(RacerThread.class, racers.length);
        for (int i=0; i<racers.length; i++) {
            threads[i] = new RacerThread(racers[i]);
        }
        return threads;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] createAndFillArray(int length, Class<T> clazz) throws InstantiationException, IllegalAccessException {
        final T[] array = (T[]) Array.newInstance(clazz, length);
        for (int i = 0; i < length; i++) {
            array[i] = clazz.newInstance();
        }
        return array;
    }

    private boolean allThreadsAt(int index) {
        for (RacerThread racer : racerThreads) {
            if (racer.index != index) {
                return false;
            }
        }
        return true;
    }

    private class RacerThread extends Thread {
        private final Racer<I, R> racer;
        private volatile int index = -1;

        public RacerThread(Racer<I, R> racer) {
            this.racer = racer;
        }

        @Override
        public void run() {
            while (true) {
                index++;
                if (index == inputs.length) return;
                while(index != raceIndex);
                racer.go(inputs[raceIndex], results[raceIndex]);
            }
        }
    }

    public static interface Racer<I, R> {
        void go(I input, R result);
    }
}
