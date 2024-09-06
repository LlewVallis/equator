package com.llewvallis.equator.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.llewvallis.equator.MainModule;
import com.llewvallis.equator.lockfile.LockfileManager;
import java.io.IOException;
import java.io.PrintStream;

public class ServerModule extends AbstractModule {

    @Provides
    @Singleton
    private ServerRunner serverRunner(
            LockfileManager lockfileManager,
            @MainModule.Stdout PrintStream stdout,
            ConnectionHandler connectionHandler)
            throws IOException {
        return new ServerRunner(lockfileManager, stdout, connectionHandler);
    }

    @Provides
    @Singleton
    private ConnectionHandler connectionHandler() {
        return new ConnectionHandlerImpl();
    }
}
