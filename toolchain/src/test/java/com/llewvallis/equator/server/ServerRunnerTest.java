package com.llewvallis.equator.server;

import static org.assertj.core.api.Assertions.*;

import com.google.common.io.Closer;
import com.llewvallis.equator.lockfile.FakeLockfileManager;
import com.llewvallis.equator.lockfile.LockfileManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@Execution(ExecutionMode.CONCURRENT)
@Timeout(value = 1)
class ServerRunnerTest {

    private static class TestException extends RuntimeException {}

    private static class ThrowingConnectionHandler implements ConnectionHandler {

        @Override
        public void handle(ClientConnection connection) {
            try {
                connection.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            throw new TestException();
        }
    }

    private LockfileManager lockfileManager;
    private ByteArrayOutputStream stdoutBytes;
    private ServerRunner runner;
    private Closer closer;

    @BeforeEach
    void setUp() throws IOException {
        lockfileManager = new FakeLockfileManager();

        stdoutBytes = new ByteArrayOutputStream();
        var stdout = new PrintStream(stdoutBytes);

        var connectionHandler = new ThrowingConnectionHandler();
        runner = new ServerRunner(lockfileManager, stdout, connectionHandler);

        var clientSocket = new Socket("localhost", runner.getPort());

        closer = Closer.create();
        closer.register(clientSocket);
        closer.register(runner);
    }

    @AfterEach
    void tearDown() throws IOException {
        closer.close();
    }

    @Test
    void writesLockfile() {
        assertThatThrownBy(() -> runner.run()).isInstanceOf(TestException.class);

        var expected =
                new LockfileManager.LockfileData(ProcessHandle.current().pid(), runner.getPort());
        assertThat(lockfileManager.read()).isEqualTo(expected);
    }

    @Test
    void clearsLockfileOnClose() throws IOException {
        assertThatThrownBy(() -> runner.run()).isInstanceOf(TestException.class);
        closer.close();

        assertThat(lockfileManager.read()).isNull();
    }

    @Test
    void printsPortToStdout() {
        assertThatThrownBy(() -> runner.run()).isInstanceOf(TestException.class);

        var expected = Integer.toString(runner.getPort());
        assertThat(stdoutBytes.toByteArray()).asString().isEqualTo(expected);
    }
}
