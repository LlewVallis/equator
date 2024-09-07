package com.llewvallis.equator.server;

import com.google.common.io.Closer;
import com.llewvallis.equator.properties.PropertyOverrides;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientConnectionImpl implements ClientConnection {

    private static final Logger LOG = LoggerFactory.getLogger(ClientConnectionImpl.class);

    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    public ClientConnectionImpl(PropertyOverrides overrides, Socket socket) throws IOException {
        this.socket = socket;
        in = new ConservativeBufferedInputStream(overrides, socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public InputStream in() {
        return in;
    }

    @Override
    public OutputStream out() {
        return out;
    }

    @Override
    public void close() {
        try (var closer = Closer.create()) {
            closer.register(socket);
            closer.register(in);
            closer.register(out);
        } catch (IOException e) {
            LOG.warn("Failed to close connection", e);
        }
    }
}
