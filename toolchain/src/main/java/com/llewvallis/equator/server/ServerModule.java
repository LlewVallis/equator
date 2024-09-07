package com.llewvallis.equator.server;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.llewvallis.equator.MainModule;
import com.llewvallis.equator.lockfile.LockfileManager;
import com.llewvallis.equator.properties.PropertyOverrides;
import java.io.IOException;
import java.io.PrintStream;

public class ServerModule extends AbstractModule {

    @Provides
    @Singleton
    private ServerRunner serverRunner(
            PropertyOverrides overrides,
            LockfileManager lockfileManager,
            @MainModule.Stdout PrintStream stdout,
            ConnectionHandler connectionHandler)
            throws IOException {
        return new ServerRunner(overrides, lockfileManager, stdout, connectionHandler);
    }

    @Provides
    @Singleton
    private ConnectionHandler connectionHandler(PropertyOverrides overrides) {
        return new ConnectionHandlerImpl(overrides);
    }
}
