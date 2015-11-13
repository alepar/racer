package ru.alepar.racer;

import java.lang.reflect.Array;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Race<I extends Resettable, O extends Resettable> {

    private final int iterationsTotal;
    private final int iterationsPerBatch;
    private final boolean reportProgress;
    private final OutputCollector<O> collector;

    private final I[] inputs;
    private final O[] outputs;
    private final RacerThread[] racerThreads;

    private final CyclicBarrier barrier;

    private volatile int raceIndex;
    private volatile int curBatchLength;

    @SafeVarargs
    public Race(int iterationsTotal, Class<I> inputClass, Class<O> outputClass, OutputCollector<O> collector, Racer<I, O>... racers) {
        this(iterationsTotal, 10000, inputClass, outputClass, false, collector, racers);
    }

    @SafeVarargs
    public Race(int iterationsTotal, int iterationsPerBatch, Class<I> inputClass, Class<O> resultClass, boolean reportProgress, OutputCollector<O> collector, Racer<I, O>... racers) {
        this.iterationsTotal = iterationsTotal;
        this.iterationsPerBatch = iterationsPerBatch;
        this.reportProgress = reportProgress;
        this.collector = collector;

        try {
            inputs = createAndFillArray(iterationsPerBatch, inputClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to create inputs array, check if input class has public access", e);
        }
        try {
            outputs = createAndFillArray(iterationsPerBatch, resultClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to create outputs array, check if input class has public access", e);
        }

        racerThreads = createRacerThreads(racers);
        barrier = new CyclicBarrier(racers.length + 1);
    }

    @SuppressWarnings("unchecked")
    public void run() {
        try {
            int iterLeft = iterationsTotal;
            for (RacerThread thread : racerThreads) {
                thread.start();
            }

            while (iterLeft > 0) {
                curBatchLength = Math.min(iterationsPerBatch, iterLeft);
                runBatch();
                for(int i=0; i<curBatchLength; i++) {
                    collector.collect(outputs[i]);
                }
                iterLeft -= curBatchLength;
                if (reportProgress) {
                    System.out.print(String.format("%.1f%%\r", (1 - (double) iterLeft / iterationsTotal) * 100));
                }
            }

            stopRacerThreads();
        } catch (Exception e) {
            throw new RuntimeException("uncaught exception", e);
        }
    }

    public void runBatch() {
        final int curBatchLength = this.curBatchLength;

        for (int i=0; i < curBatchLength; i++) {
            inputs[i].reset();
            outputs[i].reset();
        }

        raceIndex = -1;
        barrierAwait();

        for (int iter=0; iter < curBatchLength; iter++) {
            //noinspection StatementWithEmptyBody
            while(!allThreadsAt(iter));
            raceIndex = iter;
        }

        //noinspection StatementWithEmptyBody
        while(!allThreadsAt(-1));
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

        private final Racer<I, O> racer;

        private volatile int index;
        private volatile boolean stop;

        public RacerThread(Racer<I, O> racer) {
            this.racer = racer;
            setName("Race-Racer"+getName());
        }

        @Override
        public void run() {
            while (true) {
                index = -1;
                barrierAwait();

                if (stop) {
                    return;
                }

                final int curBatchLength = Race.this.curBatchLength;
                for (index=0; index<curBatchLength; index++) {
                    //noinspection StatementWithEmptyBody
                    while (index != raceIndex) ;
                    racer.go(inputs[index], outputs[index]);
                }
            }
        }

        public void stopRacer() {
            stop = true;
        }
    }

    private void stopRacerThreads() {
        for (RacerThread thread : racerThreads) {
            thread.stopRacer();
        }
        barrierAwait();
    }

    private void barrierAwait() {
        try {
            barrier.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (BrokenBarrierException e) {
            throw new RuntimeException();
        }
    }

    @SuppressWarnings("unchecked")
    private RacerThread[] createRacerThreads(Racer<I, O>[] racers) {
        final RacerThread[] threads = (RacerThread[]) Array.newInstance(RacerThread.class, racers.length);
        for (int i=0; i<racers.length; i++) {
            threads[i] = new RacerThread(racers[i]);
        }
        return threads;
    }

    @SuppressWarnings("unchecked")
    private static <T> T[] createAndFillArray(int length, Class<T> clazz) throws InstantiationException, IllegalAccessException {
        try {
            final T[] array = (T[]) Array.newInstance(clazz, length);
            for (int i = 0; i < length; i++) {
                array[i] = clazz.newInstance();
            }
            return array;
        } catch (Exception e) {
            throw new RuntimeException("failed to create array", e);
        }
    }

}
