package com.llewvallis.equator.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.llewvallis.equator.properties.Property;
import com.llewvallis.equator.properties.PropertyOverrides;
import java.io.IOException;
import java.io.InputStream;

/**
 * Like {@link java.io.BufferedInputStream}, but with the guarantee that a read operation will only
 * ever block if it absolutely must (i.e. if it is required by the API contract).
 */
public class ConservativeBufferedInputStream extends InputStream {

    @VisibleForTesting
    static final Property<Integer> BUFFER_SIZE = Property.integer("socketInputBufferSize", 1024);

    private final InputStream wrapped;

    private final byte[] buffer;
    private int bufferOffset = 0;
    private int bufferedBytes = 0;

    public ConservativeBufferedInputStream(PropertyOverrides overrides, InputStream wrapped) {
        this.wrapped = wrapped;
        buffer = new byte[BUFFER_SIZE.get(overrides)];
    }

    @Override
    public int read() throws IOException {
        if (bufferedBytes == 0) {
            fill();

            if (bufferedBytes == 0) {
                return -1;
            }
        }

        var result = buffer[bufferOffset] & 0xFF;
        bufferOffset++;
        bufferedBytes--;
        return result;
    }

    @Override
    public int read(byte[] dest) throws IOException {
        return read(dest, 0, dest.length);
    }

    @Override
    public int read(byte[] dest, int destOffset, int destCapacity) throws IOException {
        var bytesReadSoFar = 0;

        // While more capacity is available, we will keep reading until we would block. If we
        // haven't read any bytes yet, then we will block anyway
        while (destCapacity > 0 && (available() > 0 || bytesReadSoFar == 0)) {
            if (bufferedBytes == 0) {
                fill();

                if (bufferedBytes == 0) {
                    return bytesReadSoFar == 0 ? -1 : bytesReadSoFar;
                }
            }

            var count = Math.min(destCapacity, bufferedBytes);
            System.arraycopy(buffer, bufferOffset, dest, destOffset, count);
            bytesReadSoFar += count;
            bufferOffset += count;
            bufferedBytes -= count;
            destOffset += count;
            destCapacity -= count;
        }

        return bytesReadSoFar;
    }

    private void fill() throws IOException {
        Preconditions.checkState(bufferedBytes == 0);
        bufferOffset = 0;

        var requested = Math.min(Math.max(wrapped.available(), 1), buffer.length);
        var actual = wrapped.read(buffer, 0, requested);

        if (actual != -1) {
            bufferedBytes = actual;
        }
    }

    @Override
    public int available() throws IOException {
        return bufferedBytes + wrapped.available();
    }

    @Override
    public void close() throws IOException {
        wrapped.close();
    }
}
