package com.llewvallis.equator.lockfile;

import static com.llewvallis.equator.Constants.EQUATOR_DIRECTORY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Arrays;
import org.jspecify.annotations.Nullable;

public class LockfileManagerImpl implements LockfileManager {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Path path;

    public LockfileManagerImpl(Path projectDirectory) {
        path = projectDirectory.resolve(EQUATOR_DIRECTORY + "/.lockfile");
    }

    @Override
    public void write(LockfileData data) throws AlreadyRunningException {
        try {
            upsertContainingDirectory();

            try (var channel =
                    openAndLock(
                            StandardOpenOption.READ,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE)) {
                var existing = parse(channel);

                if (isServerStillRunning(existing)) {
                    throw new AlreadyRunningException();
                }

                channel.truncate(0);
                MAPPER.writeValue(Channels.newOutputStream(channel), data);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Error initializing lockfile", e);
        }
    }

    private void upsertContainingDirectory() throws IOException {
        var containingDirectory = path.getParent();
        Files.createDirectories(containingDirectory);
    }

    @Override
    public void clear() {
        try {
            try (var channel = openAndLock(StandardOpenOption.READ, StandardOpenOption.WRITE)) {
                var existing = parse(channel);
                if (existing != null && existing.pid() == ProcessHandle.current().pid()) {
                    channel.truncate(0);
                }
            }
        } catch (NoSuchFileException e) {
            // Ignored
        } catch (IOException e) {
            throw new UncheckedIOException("Error clearing lockfile", e);
        }
    }

    @Override
    public @Nullable LockfileData read() {
        try (var channel = openAndLock(StandardOpenOption.READ, StandardOpenOption.WRITE)) {
            var result = parse(channel);
            return isServerStillRunning(result) ? result : null;
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading lockfile", e);
        }
    }

    private @Nullable LockfileData parse(FileChannel channel) throws IOException {
        Preconditions.checkArgument(channel.position() == 0);

        try {
            var bytes = Channels.newInputStream(channel).readAllBytes();

            if (bytes.length == 0) {
                return null;
            }

            return MAPPER.readValue(bytes, LockfileData.class);
        } catch (JsonProcessingException e) {
            return null;
        } finally {
            channel.position(0);
        }
    }

    private FileChannel openAndLock(OpenOption... options) throws IOException {
        Preconditions.checkArgument(
                Arrays.asList(options).contains(StandardOpenOption.WRITE),
                "The WRITE option is required to lock a file");

        var channel = FileChannel.open(path, options);
        // Lock is released when the channel is closed
        var ignored = channel.lock();
        return channel;
    }

    private boolean isServerStillRunning(@Nullable LockfileData data) {
        return data != null && ProcessHandle.of(data.pid()).isPresent();
    }
}
