package com.llewvallis.equator.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Closer;
import com.llewvallis.equator.MainModule;
import com.llewvallis.equator.lockfile.LockfileManager;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerRunner implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ServerRunner.class);

    private final LockfileManager lockfileManager;
    private final PrintStream stdout;
    private final ConnectionHandler connectionHandler;
    private final ServerSocket serverSocket;

    public ServerRunner(
            LockfileManager lockfileManager,
            @MainModule.Stdout PrintStream stdout,
            ConnectionHandler connectionHandler)
            throws IOException {
        this.lockfileManager = lockfileManager;
        this.stdout = stdout;
        this.connectionHandler = connectionHandler;
        serverSocket = new ServerSocket(0);
    }

    public void run() throws IOException {
        writeLockfile();
        writePortToStdout();

        while (true) {
            var socket = serverSocket.accept();
            connectionHandler.handle(new ClientConnectionImpl(socket));
        }
    }

    private void writeLockfile() {
        var pid = ProcessHandle.current().pid();
        var port = serverSocket.getLocalPort();

        try {
            lockfileManager.write(new LockfileManager.LockfileData(pid, port));
        } catch (LockfileManager.AlreadyRunningException e) {
            LOG.error("Server is already running");
            System.exit(1);
        }
    }

    private void writePortToStdout() {
        // We print the port to stdout to communicate which port we are binding on
        stdout.print(serverSocket.getLocalPort());
        stdout.close();
    }

    @VisibleForTesting
    int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    public void close() throws IOException {
        try (var closer = Closer.create()) {
            closer.register(serverSocket);
            closer.register(lockfileManager::clear);
        }
    }
}
