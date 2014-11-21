package ru.alepar.direct;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

@SuppressWarnings("UnusedDeclaration") // come on, this is public api
public class AtomicDirectBuffer {

    private static final Unsafe unsafe = getUnsafeInstance();
    private static final boolean nativeByteOrder = systemByteOrder() == ByteOrder.BIG_ENDIAN;

    private final long bufAddress;

    public AtomicDirectBuffer(DirectBuffer buffer) {
        bufAddress = buffer.address();
    }


    public void putLongOrdered(long offset, long value) {
        unsafe.putOrderedLong(null, bufAddress + offset, nativeByteOrder ? value : Long.reverseBytes(value));
    }

    public void putLongVolatile(long offset, long value) {
        unsafe.putLongVolatile(null, bufAddress + offset, nativeByteOrder ? value : Long.reverseBytes(value));
    }

    public boolean compareAndSwapLong(long offset, long expect, long update) {
        return unsafe.compareAndSwapLong(null, bufAddress + offset,
                nativeByteOrder ? expect : Long.reverseBytes(expect),
                nativeByteOrder ? update : Long.reverseBytes(update)
        );
    }

    public void putIntOrdered(long offset, int value) {
        unsafe.putOrderedInt(null, bufAddress + offset, nativeByteOrder ? value : Integer.reverseBytes(value));
    }

    public void putIntVolatile(long offset, int value) {
        unsafe.putIntVolatile(null, bufAddress + offset, nativeByteOrder ? value : Integer.reverseBytes(value));
    }

    public boolean compareAndSwapInt(long offset, int expect, int update) {
        return unsafe.compareAndSwapInt(null, bufAddress + offset,
                nativeByteOrder ? expect : Integer.reverseBytes(expect),
                nativeByteOrder ? update : Integer.reverseBytes(update)
        );
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

    private static ByteOrder systemByteOrder() {
        try {
            final Class<?> nioBitsClass = Class.forName("java.nio.Bits");
            final Field byteOrderField = nioBitsClass.getDeclaredField("byteOrder");
            byteOrderField.setAccessible(true);
            return (ByteOrder) byteOrderField.get(nioBitsClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to get java.nio.Bits#byteOrder", e);
        }
    }

    public static void main(String[] args) {
        final ByteBuffer buf = ByteBuffer.allocateDirect(8);
        final AtomicDirectBuffer atomic = new AtomicDirectBuffer((DirectBuffer) buf);


        for(int i=0; i<100; i++) System.gc();

        System.out.println(buf.duplicate().getLong());
//        atomic.putLongVolatile(0, 3);
        System.out.println(atomic.compareAndSwapLong(0, 0, 2));

        System.out.println(buf.duplicate().getLong());
        final byte[] bytes = new byte[8];
        buf.get(bytes);
        System.out.println(Arrays.toString(bytes));
    }
}
