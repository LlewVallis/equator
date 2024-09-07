package com.llewvallis.equator.server;

import static com.llewvallis.equator.server.ConservativeBufferedInputStream.BUFFER_SIZE;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.primitives.Bytes;
import com.llewvallis.equator.properties.PropertyOverrides;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConservativeBufferedInputStreamTest {

    private static final byte[] RANDOM_BYTES = new byte[100];

    static {
        new Random(0).nextBytes(RANDOM_BYTES);
    }

    private PropertyOverrides overrides;

    @BeforeEach
    void setUp() {
        overrides = new PropertyOverrides();
        // Small buffer size means edge cases are more likely to appear
        overrides.setOverride(BUFFER_SIZE, 10);
    }

    private InputStream createWithBytes(byte[] bytes) {
        return new ConservativeBufferedInputStream(overrides, new ByteArrayInputStream(bytes));
    }

    @Test
    void correctBytesCanBeRead() throws IOException {
        var stream = createWithBytes(RANDOM_BYTES);
        assertThat(stream.readAllBytes()).isEqualTo(RANDOM_BYTES);
    }

    @Test
    void correctBytesCanBeReadOneAtATime() throws IOException {
        var stream = createWithBytes(RANDOM_BYTES);

        var received = new ArrayList<Byte>();
        for (int next; (next = stream.read()) != -1; ) {
            received.add((byte) next);
        }

        assertThat(Bytes.toArray(received)).isEqualTo(RANDOM_BYTES);
    }

    @Test
    void correctBytesCanBeReadWithASmallBuffer() throws IOException {
        var stream = createWithBytes(RANDOM_BYTES);

        var received = new ArrayList<Byte>();
        while (true) {
            var buffer = new byte[3];
            var read = stream.read(buffer);
            if (read == -1) {
                break;
            }

            var readBytes = Arrays.copyOf(buffer, read);
            received.addAll(Bytes.asList(readBytes));
        }

        assertThat(Bytes.toArray(received)).isEqualTo(RANDOM_BYTES);
    }

    @Test
    void readDoesNotBlockWhenAByteIsAvailable() throws IOException {
        var in = new PipedInputStream();
        var out = new PipedOutputStream(in);
        var stream = new ConservativeBufferedInputStream(overrides, in);

        out.write(1);
        out.write(2);
        out.write(3);

        assertThat(stream.read()).isEqualTo(1);
        assertThat(stream.read()).isEqualTo(2);
        assertThat(stream.read()).isEqualTo(3);
    }

    @Test
    void readReturnsNegativeOneWhenNoMoreDataIsAvailable() throws IOException {
        var stream = createWithBytes(new byte[0]);
        assertThat(stream.read()).isEqualTo(-1);
    }

    @Test
    void availableReturnsRemainingNumberOfBytes() throws IOException {
        var stream = createWithBytes(RANDOM_BYTES);

        for (var i = 0; i < RANDOM_BYTES.length; i++) {
            assertThat(stream.available()).isEqualTo(RANDOM_BYTES.length - i);
            stream.read();
        }

        assertThat(stream.available()).isEqualTo(0);
    }
}
