package com.adel;

import java.nio.MappedByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Hello world!
 */
public class ProducerAppWithSpinLock {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA1");
        digest.digest(new byte[256]);
        final byte[] dummy = digest.digest();
        final int hashLen = dummy.length;

        final MappedByteBuffer shm = SharedMemory.createSharedMemory("ipc_shared.dat", 64 * 1024 + hashLen);
        shm.putInt(0, 0);
        final long addr = SharedMemory.getBufferAddress(shm);
        System.out.printf("Buffer address: 0x%08x\n", addr);

        final Random rnd = new Random();

        final long start = System.currentTimeMillis();
        long iter = 0;
        final int capacity = shm.capacity();
        System.out.println("Starting producer iters ....");
        final SpinLock lock = new SpinLock(addr);

        while (System.currentTimeMillis() - start < 30_000) {

            if (!lock.tryLock(5_000)) {
                throw new RuntimeException("Unable to acquire lock");
            }

            try {
                for (int i = 4; i < capacity - hashLen; i++) {
                    final byte val = (byte) (rnd.nextInt(256) & 0x00ff);
                    digest.update(val);
                    shm.put(i, val);
                }

                final byte[] hash = digest.digest();
                shm.position(capacity - hashLen);
                shm.put(hash);
                iter++;
            } finally {
                lock.unlock();
            }
        }

        System.out.printf("%d iterations run\n", iter);
        System.out.println("<Enter> to exit");
        System.console().readLine();
    }


}
