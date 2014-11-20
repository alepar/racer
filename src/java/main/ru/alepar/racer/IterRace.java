package ru.alepar.racer;

import java.lang.reflect.Array;

@SuppressWarnings("StatementWithEmptyBody") //those are spinlocks
class IterRace<I, R> {

    private final I[] inputs;
    private final R[] results;

    private final RacerThread[] racerThreads;

    private volatile int raceIndex=-1;

    @SafeVarargs
    public IterRace(int iter, Class<I> inputClass, Class<R> resultClass, Racer<I, R>... racers) throws Exception {
        inputs = createAndFillArray(iter, inputClass);
        results = createAndFillArray(iter, resultClass);
        racerThreads = createRacerThreads(racers);
    }

    public R[] run() {
        for (RacerThread thread : racerThreads) {
            thread.start();
        }

        for (int iter=0; iter <= inputs.length; iter++) {
            while(!allThreadsAt(iter));
            raceIndex = iter;
        }

        return results;
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

}
