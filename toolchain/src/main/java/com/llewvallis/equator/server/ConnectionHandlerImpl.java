package com.llewvallis.equator.server;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void handle(ClientConnection connection) {
        executor.execute(
                () -> {
                    try {
                        handleSync(connection);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

    private void handleSync(ClientConnection connection) throws IOException {
        connection.close();
    }
}
