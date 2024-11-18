package com.adel;

import java.nio.MappedByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ConsumerApp {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA1");
        digest.digest(new byte[256]);
        final byte[] dummy = digest.digest();
        final int hashLen = dummy.length;

        final MappedByteBuffer shm = SharedMemory.createSharedMemory("ipc_shared.dat", 64 * 1024 + hashLen);

        final long start = System.currentTimeMillis();
        long iter = 0;
        final int capacity = shm.capacity();
        System.out.println("Starting consumer iters ....");

        long matchCount = 0;
        long mismatchCount = 0;
        byte[] expectedHash = new byte[hashLen];

        while (System.currentTimeMillis() - start < 30_000) {
            for (int i = 0; i < capacity - hashLen; i++) {
                final byte val = shm.get(i);
                digest.update(val);
            }

            final byte[] hash = digest.digest();
            shm.position(capacity - hashLen);
            shm.get(expectedHash);

            if (Arrays.equals(hash, expectedHash)) {
                matchCount++;
            } else {
                mismatchCount++;
            }

            iter++;
        }

        System.out.printf("%d iterations run. matches=%d, mismatches=%d\n", iter, matchCount, mismatchCount);
        System.out.println("<Enter> to exit");
        System.console().readLine();
    }
}
