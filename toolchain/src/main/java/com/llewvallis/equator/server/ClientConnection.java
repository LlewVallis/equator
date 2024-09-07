package com.llewvallis.equator.server;

import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;

public interface ClientConnection extends Closeable {

    InputStream in();

    OutputStream out();

    @Override
    void close();
}
