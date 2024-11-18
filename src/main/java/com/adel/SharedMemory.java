package com.adel;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class SharedMemory {
    public static MappedByteBuffer createSharedMemory(final String path,
                                                      final long size) {
        try (final FileChannel fc = (FileChannel) Files.newByteChannel(new File(path).toPath(),
                EnumSet.of(StandardOpenOption.CREATE,
                        StandardOpenOption.SPARSE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.READ))) {
            return fc.map(FileChannel.MapMode.READ_WRITE, 0, size);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getBufferAddress(final MappedByteBuffer shm) {
        try {
            Class<?> clz = shm.getClass();
            final Method maddr = clz.getMethod("address");
            maddr.setAccessible(true);

            final Long addr = (Long) maddr.invoke(shm);
            if (null == addr) {
                throw new RuntimeException("Unable to retrieve buffer's address");
            }

            return addr;
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
