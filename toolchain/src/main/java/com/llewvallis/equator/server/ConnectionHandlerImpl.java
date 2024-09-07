package com.llewvallis.equator.server;

import com.llewvallis.equator.properties.Property;
import com.llewvallis.equator.properties.PropertyOverrides;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class ConnectionHandlerImpl implements ConnectionHandler {

    private static final Property<Integer> MAX_CONNECTIONS =
            Property.integer("maxConnections", 256);

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final Semaphore semaphore;

    public ConnectionHandlerImpl(PropertyOverrides overrides) {
        semaphore = new Semaphore(MAX_CONNECTIONS.get(overrides));
    }

    @Override
    public void handle(ClientConnection connection) {
        if (!semaphore.tryAcquire()) {
            connection.close();
            return;
        }

        executor.execute(
                () -> {
                    try {
                        handleSync(connection);
                    } finally {
                        semaphore.release();
                    }
                });
    }

    private void handleSync(ClientConnection connection) {
        connection.close();
    }
}
