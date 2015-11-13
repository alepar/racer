package ru.alepar.racer.examples;

import ru.alepar.racer.Race;
import ru.alepar.racer.Resettable;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Arrays;

public class DoubleWordWriteWithStoreStore {

    private static final Unsafe unsafe = getUnsafeInstance();

    public static void main(String[] args) throws Exception {
        final int[] results = new int[4];
        final Race<Input, Result> race = new Race<>(Config.ITERATIONS_TOTAL, Config.ITERATIONS_PER_BATCH, Input.class, Result.class, true,
            (result) -> {
                results[result.r] ++;
            },
            (input, result) -> {
                unsafe.putInt(null, input.addrValue, 1);
                unsafe.putInt(null, input.addrKey, 1);
            },
            (input, result) -> {
                int k = unsafe.getInt(null, input.addrKey);
                int v = unsafe.getInt(null, input.addrValue);

                result.r = (k << 1) + (v);
            }
        );
        final long start = System.nanoTime();
        race.run();
        final long end = System.nanoTime();
        System.out.println(String.format("taken %.2fs", (end-start)/1_000_000/1000.0));
        System.out.println(Arrays.toString(results));
    }

    public static class Input implements Resettable {

        private final long address = unsafe.allocateMemory(16);

        private final long addrKey = address;
        private final long addrValue = address + 8;

        @Override
        public void reset() {
            unsafe.putInt(null, addrKey, 0);
            unsafe.putInt(null, addrValue, 0);
        }
    }

    public static class Result implements Resettable {

        int r;

        @Override
        public void reset() {
            r = 0;
        }

    }

    private static Unsafe getUnsafeInstance() {
        try {
            final Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeField.setAccessible(true);
            return (Unsafe) unsafeField.get(Unsafe.class);
        } catch (Exception e) {
            throw new RuntimeException("failed to get sun.misc.Unsafe#theUnsafe", e);
        }
    }

}
