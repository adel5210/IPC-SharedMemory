package com.adel;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class SpinLock {
    private static final Unsafe unsafe;
    private final long addr;


    static {
        try {
            final Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public SpinLock(long addr) {
        this.addr = addr;
    }

    public boolean tryLock(final long maxWait) {
        final long deadline = System.currentTimeMillis() + maxWait;
        while (System.currentTimeMillis() < deadline) {
            if (unsafe.compareAndSwapInt(null, addr, 0, 1)) {
                return true;
            }
        }
        return false;
    }

    public void unlock() {
        unsafe.putInt(addr, 0);
    }
}
