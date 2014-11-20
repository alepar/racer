package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Racer;

import java.util.HashSet;

public class DoubleCheckedLocking {

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static void main(String[] args) throws Exception {
        final Race<Input, Result> race = new Race<>(10000000, Input.class, Result.class,
                new Racer<Input, Result>() {
                    @Override
                    public void go(Input input, Result result) {
                        if (input.singleton == null) {
                            synchronized (input) {
                                if (input.singleton == null) {
                                    input.singleton = create();
                                }
                            }
                        }
                        result.o1 = input.singleton;
                    }
                },
                new Racer<Input, Result>() {
                    @Override
                    public void go(Input input, Result result) {
                        if (input.singleton == null) {
                            synchronized (input) {
                                if (input.singleton == null) {
                                    input.singleton = create();
                                }
                            }
                        }
                        result.o2 = input.singleton;
                    }
                },
                new Racer<Input, Result>() {
                    @Override
                    public void go(Input input, Result result) {
                        if (input.singleton == null) {
                            synchronized (input) {
                                if (input.singleton == null) {
                                    input.singleton = create();
                                }
                            }
                        }
                        result.o3 = input.singleton;
                    }
                },
                new Racer<Input, Result>() {
                    @Override
                    public void go(Input input, Result result) {
                        if (input.singleton == null) {
                            synchronized (input) {
                                if (input.singleton == null) {
                                    input.singleton = create();
                                }
                            }
                        }
                        result.o4 = input.singleton;
                    }
                }
        );
        final long start = System.currentTimeMillis();
        race.run();
        final long end = System.currentTimeMillis();
        System.out.println(String.format("taken %.2fs", (end-start)/1000.0));
    }

    public static Object create() {
        return new Object();
    }

    public static class Input {
        volatile Object singleton;
    }

    public static class Result {

        Object o1, o2, o3, o4;

        @Override
        public boolean equals(Object o) {
            return this == o || !(o == null || getClass() != o.getClass()) && hashCode() == o.hashCode();
        }

        @Override
        public int hashCode() {
            return new HashSet<Object>() {{
                add(o1); add(o2); add(o3); add(o4);
            }}.size();
        }

        @Override
        public String toString() {
            return String.format("" + hashCode());
        }
    }

}
