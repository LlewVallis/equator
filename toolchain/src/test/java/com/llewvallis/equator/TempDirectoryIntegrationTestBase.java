package com.llewvallis.equator;

import com.google.common.io.Closer;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class TempDirectoryIntegrationTestBase {

    private final Closer deleteTempDir = Closer.create();

    protected Path tempDir;

    @BeforeEach
    void createTempDir() throws IOException {
        tempDir = Files.createTempDirectory("equator-integration-test");
        deleteTempDir.register(
                () -> MoreFiles.deleteRecursively(tempDir, RecursiveDeleteOption.ALLOW_INSECURE));

        Runtime.getRuntime()
                .addShutdownHook(
                        Thread.ofVirtual()
                                .unstarted(
                                        () -> {
                                            try {
                                                deleteTempDir.close();
                                            } catch (IOException e) {
                                                throw new UncheckedIOException(e);
                                            }
                                        }));
    }

    @AfterEach
    void deleteTempDir() throws IOException {
        deleteTempDir.close();
    }
}
