package ru.alepar.racer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Race<I, R> {

    private final int iter;
    private final int iterPerBatch;
    private final Class<I> inputClass;
    private final Class<R> resultClass;
    private final Racer<I, R>[] racers;
    private final boolean report;

    @SafeVarargs
    public Race(int iter, Class<I> inputClass, Class<R> resultClass, Racer<I, R>... racers) {
        this(iter, 10000, inputClass, resultClass, false, racers);
    }

    @SafeVarargs
    public Race(int iter, int iterPerBatch, Class<I> inputClass, Class<R> resultClass, boolean report, Racer<I, R>... racers) {
        this.iter = iter;
        this.iterPerBatch = iterPerBatch;
        this.inputClass = inputClass;
        this.resultClass = resultClass;
        this.report = report;
        this.racers = racers;
    }

    @SuppressWarnings("unchecked")
    public List<R> run() {
        try {
            final List<R> results = new ArrayList<>(iter);

            int iterLeft = iter;
            while (iterLeft > 0) {
                final int curIter = Math.min(iterPerBatch, iterLeft);
                final R[] curResults = new IterRace<>(curIter, inputClass, resultClass, racers).run();
                Collections.addAll(results, curResults);
                iterLeft -= curIter;
                if (report) {
                    System.out.print(String.format("%.1f%%\r", (1 - (double) iterLeft / iter) * 100));
                }
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException("failed to run batch", e);
        }
    }

}
