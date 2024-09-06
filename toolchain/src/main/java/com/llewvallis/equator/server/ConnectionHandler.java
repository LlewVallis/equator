package com.llewvallis.equator.server;

public interface ConnectionHandler {

    void handle(ClientConnection connection);
}
