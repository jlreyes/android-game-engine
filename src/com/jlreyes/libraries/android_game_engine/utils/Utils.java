package com.jlreyes.libraries.android_game_engine.utils;

import com.jlreyes.libraries.android_game_engine.utils.math.function.Function1;

import java.nio.*;

public class Utils {
    public static final int FLOAT_BYTES = 4;
    public static final int UNSIGNED_BYTE_MAX_VALUE = 255;
    public static final int UNSIGNED_SHORT_MAX_VALUE = 65535;

    public static enum Priority {LOW, NORMAL, HIGH, ABSOLUTE}

    public static enum Order {LESS, EQUAL, GREATER}

    public static enum Direction {NONE, UP, DOWN, LEFT, RIGHT}

    /**
     * Compares p1 to p2.
     * @return LESS, EQUAL, or GREATER
     */
    public static Order comparePriority(Priority p1, Priority p2) {
        if (p1 == p2) return Order.EQUAL;
        if (p1 == Priority.LOW) return Order.LESS;
        if (p1 == Priority.NORMAL) {
            if (p2 == Priority.LOW) return Order.GREATER;
            else return Order.LESS;
        }
        if (p1 == Priority.HIGH) {
            if (p2 == Priority.LOW) return Order.GREATER;
            if (p2 == Priority.NORMAL) return Order.GREATER;
            else return Order.LESS;
        }
        if (p1 == Priority.ABSOLUTE) return Order.GREATER;        /* This should never happen */
        throw new RuntimeException("Wut?");
    }

    public static Object LinearSearch(Function1<Object, Boolean> equalFunction,
                                      Object[] array) {
        for (Object o : array)
            if (equalFunction.run(o) == true) return o;
        return null;
    }

    /**
     * Given an array of bytes, combines bytes[offset] through
     * bytes[offset + 8] into a long. bytes should be in the given order
     *
     * @param bytes
     * @param offset
     * @param order
     * @return
     */
    public static long LongFromByteArray(byte[] bytes, int offset, ByteOrder order) {
        if (bytes.length - offset < 8)
            throw new RuntimeException("Not enough bytes given.");
        ByteBuffer b = ByteBuffer.allocateDirect(8).order(order);
        b.put(bytes, offset, 8);
        b.position(0);
        LongBuffer l = b.asLongBuffer();
        return l.get();
    }

    /**
     * Given an array of bytes, combines bytes[offset] through
     * bytes[offset + 4] into an int. bytes should be in the given order
     *
     * @param bytes
     * @param offset
     * @param order
     * @return
     */
    public static int IntFromByteArray(byte[] bytes, int offset, ByteOrder order) {
        if (bytes.length - offset < 4)
            throw new RuntimeException("Not enough bytes given.");
        ByteBuffer b = ByteBuffer.allocateDirect(4).order(order);
        b.put(bytes, offset, 4);
        b.position(0);
        IntBuffer i = b.asIntBuffer();
        //Log.i("Utils", "Position:" + i.position() + " Limit:" + i.limit());
        return i.get();
    }

    /**
     * Given an array of bytes, combines bytes[offset] through
     * bytes[offset + 2] into a char. bytes should be in the given order
     *
     * @param bytes
     * @param offset
     * @param order
     * @return
     */
    public static char CharFromByteArray(byte[] bytes, int offset, ByteOrder order) {
        if (bytes.length - offset < 2)
            throw new RuntimeException("Not enough bytes given.");
        ByteBuffer b = ByteBuffer.allocateDirect(2).order(order);
        b.put(bytes, offset, 2);
        b.position(0);
        CharBuffer c = b.asCharBuffer();
        return c.get();
    }

    /**
     * Given a long, returns an array of bytes representing that long in
     * the given order
     *
     * @param l
     * @return
     */
    public static byte[] LongToByteArray(long l, ByteOrder order) {
        int numBytes = 8;
        byte[] bytes = new byte[numBytes];
        ByteBuffer b = ByteBuffer.wrap(bytes).order(order);
        b.putLong(l);
        return b.array();
    }

    /**
     * Given a int, returns an array of bytes representing that int in
     * the given order
     *
     * @param i
     * @return
     */
    public static byte[] IntToByteArray(int i, ByteOrder order) {
        int numBytes = 4;
        byte[] bytes = new byte[numBytes];
        ByteBuffer b = ByteBuffer.wrap(bytes).order(order);
        b.putInt(i);
        return b.array();
    }

    /**
     * Given a char, returns an array of bytes representing that char in the
     * given order
     *
     * @param c
     * @return
     */
    public static byte[] CharToByteArray(char c, ByteOrder order) {
        int numBytes = 2;
        byte[] bytes = new byte[numBytes];
        ByteBuffer b = ByteBuffer.wrap(bytes).order(order);
        b.putChar(c);
        return b.array();
    }
}
