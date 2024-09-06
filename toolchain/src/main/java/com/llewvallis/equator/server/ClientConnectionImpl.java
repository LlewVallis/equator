package com.llewvallis.equator.server;

import java.io.IOException;
import java.net.Socket;

public class ClientConnectionImpl implements ClientConnection {

    private final Socket socket;

    public ClientConnectionImpl(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
