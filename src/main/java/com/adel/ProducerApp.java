package com.adel;

import java.io.File;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Random;

/**
 * Hello world!
 */
public class ProducerApp {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA1");
        digest.digest(new byte[256]);
        final byte[] dummy = digest.digest();
        final int hashLen = dummy.length;

        final MappedByteBuffer shm = SharedMemory.createSharedMemory("ipc_shared.dat", 64 * 1024 + hashLen);
        final Random rnd = new Random();

        final long start = System.currentTimeMillis();
        long iter = 0;
        final int capacity = shm.capacity();
        System.out.println("Starting producer iters ....");

        while (System.currentTimeMillis() - start < 30_000) {
            for (int i = 0; i < capacity - hashLen; i++) {
                final byte val = (byte) (rnd.nextInt(256) & 0x00ff);
                digest.update(val);
                shm.put(i, val);
            }

            final byte[] hash = digest.digest();
            shm.position(capacity - hashLen);
            shm.put(hash);
            iter++;
        }

        System.out.printf("%d iterations run\n", iter);
        System.out.println("<Enter> to exit");
        System.console().readLine();
    }


}
