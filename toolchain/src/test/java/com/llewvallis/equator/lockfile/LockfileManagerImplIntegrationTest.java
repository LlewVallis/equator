package com.llewvallis.equator.lockfile;

import static org.assertj.core.api.Assertions.assertThat;

import com.llewvallis.equator.TempDirectoryIntegrationTestBase;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Execution(ExecutionMode.CONCURRENT)
@Timeout(value = 1, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class LockfileManagerImplIntegrationTest extends TempDirectoryIntegrationTestBase {

    // We are assuming there is no process with this PID
    private static final LockfileManager.LockfileData DATA =
            new LockfileManager.LockfileData(1234567890, 0);

    static {
        assertThat(ProcessHandle.of(DATA.pid())).isEmpty();
    }

    private Path lockfile;
    private LockfileManager lockfileManager;

    @BeforeEach
    void setUp() {
        lockfile = tempDir.resolve(".equator/.lockfile");
        lockfileManager = new LockfileManagerImpl(tempDir);
    }

    private void writeLockfile(String content) throws IOException {
        Files.createDirectories(lockfile.getParent());
        Files.writeString(lockfile, content);
    }

    @Test
    void readingMissingLockfileReturnsNull() {
        assertThat(lockfileManager.read()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "This is not JSON!", "{\"pid\": \"Invalid PID\"}"})
    void readingInvalidLockfileReturnsNull(String content) throws IOException {
        writeLockfile(content);
        assertThat(lockfileManager.read()).isNull();
    }

    @Test
    void readingLockfileForDeadProcessReturnsNull() throws LockfileManager.AlreadyRunningException {
        lockfileManager.write(DATA);
        assertThat(lockfileManager.read()).isNull();
    }

    @Test
    void readingLockfileForLiveProcessReturnsData() throws LockfileManager.AlreadyRunningException {
        var currentPid = ProcessHandle.current().pid();
        var data = new LockfileManager.LockfileData(currentPid, 0);

        lockfileManager.write(data);
        assertThat(lockfileManager.read()).isEqualTo(data);
    }

    @Test
    void writingLockfileCreatesFile() throws LockfileManager.AlreadyRunningException {
        lockfileManager.write(DATA);
        assertThat(Files.isRegularFile(lockfile)).isTrue();
    }

    @Test
    void clearingWithoutLockfileSucceeds() {
        lockfileManager.clear();
    }

    @Test
    void clearingExistingLockfileDoesNotDeleteIt() throws LockfileManager.AlreadyRunningException {
        lockfileManager.write(DATA);
        lockfileManager.clear();
        assertThat(Files.isRegularFile(lockfile)).isTrue();
    }
}
