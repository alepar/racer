package ru.alepar.direct;

import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("UnusedDeclaration") // come on, this is public api
public class DirectUnsafe {

    private final Object[] helper = new Object[1];

    private final Unsafe unsafe;
    private final long helperAddress;
    private final long objectArrayOffset;
    private final boolean nativeByteOrder;

    public DirectUnsafe(Unsafe unsafe) {
        this.unsafe = unsafe;

        if (unsafe.arrayIndexScale(Object[].class) != 8) {
            throw new RuntimeException("only 64bit jvm with -XX:-UseCompressedOops is supported");
        }

        objectArrayOffset = (long) unsafe.arrayBaseOffset(Object[].class);
        helperAddress = getAddress(helper);
        nativeByteOrder = systemByteOrder() == ByteOrder.BIG_ENDIAN;
    }

    public long getAddress(Object obj) {
        helper[0] = obj;
        return unsafe.getLong(helper, objectArrayOffset);
    }

    public void putLongOrdered(DirectBuffer buffer, long offset, long value) {
        unsafe.putOrderedLong(helper, buffer.address()+offset-helperAddress, nativeByteOrder ? value : Long.reverseBytes(value));
    }

    public void putLongVolatile(DirectBuffer buffer, long offset, long value) {
        unsafe.putLongVolatile(helper, buffer.address() + offset - helperAddress, nativeByteOrder ? value : Long.reverseBytes(value));
    }

    public boolean compareAndSwapLong(DirectBuffer buffer, long offset, long expect, long update) {
        return unsafe.compareAndSwapLong(helper, buffer.address() + offset - helperAddress,
                nativeByteOrder ? expect : Long.reverse(expect),
                nativeByteOrder ? update : Long.reverse(update)
        );
    }

    public void putIntOrdered(DirectBuffer buffer, long offset, int value) {
        unsafe.putOrderedInt(helper, buffer.address() + offset - helperAddress, nativeByteOrder ? value : Integer.reverseBytes(value));
    }

    public void putIntVolatile(DirectBuffer buffer, long offset, int value) {
        unsafe.putIntVolatile(helper, buffer.address() + offset - helperAddress, nativeByteOrder ? value : Integer.reverseBytes(value));
    }

    public boolean compareAndSwapInt(DirectBuffer buffer, long offset, int expect, int update) {
        return unsafe.compareAndSwapInt(helper, buffer.address() + offset - helperAddress,
                nativeByteOrder ? expect : Integer.reverse(expect),
                nativeByteOrder ? update : Integer.reverse(update)
        );
    }

    public static DirectUnsafe getInstance() {
        return new DirectUnsafe(getUnsafeInstance());
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

    private ByteOrder systemByteOrder() {
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
        final DirectUnsafe dunsafe = DirectUnsafe.getInstance();

        final ByteBuffer buf = ByteBuffer.allocateDirect(8);
        System.out.println(buf.duplicate().getLong());
        dunsafe.compareAndSwapLong((DirectBuffer) buf, 0, 0, -1);
        System.out.println(buf.getLong());
    }
}
