package com.llewvallis.equator;

import static com.llewvallis.equator.Constants.JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.llewvallis.equator.lockfile.LockfileManager;
import com.llewvallis.equator.lockfile.LockfileManagerImpl;
import com.llewvallis.equator.server.ServerRunner;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Base64;

public class MainModule extends AbstractModule {

    private final Path projectDirectory;
    private final PrintStream stdout;

    public MainModule(Args args, PrintStream stdout) {
        this.projectDirectory = args.projectDirectory;
        this.stdout = stdout;
    }

    public record Args(Path projectDirectory) {

        public String serialize() {
            try {
                var json = JSON.writeValueAsString(this);
                return Base64.getUrlEncoder().encodeToString(json.getBytes());
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }

        public static Args deserialize(String encoded) {
            try {
                var json = Base64.getUrlDecoder().decode(encoded);
                return JSON.readValue(json, Args.class);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public @interface ProjectDirectory {}

    public @interface Stdout {}

    @ProjectDirectory
    @Provides
    private Path projectDirectory() {
        return projectDirectory;
    }

    @Singleton
    @Stdout
    @Provides
    @SuppressWarnings("CloseableProvides")
    private PrintStream stdout() {
        return stdout;
    }

    @Singleton
    @Provides
    private LockfileManager lockfileManager(@ProjectDirectory Path projectDirectory) {
        return new LockfileManagerImpl(projectDirectory);
    }

    public static void main(String[] cliArgs) throws JsonProcessingException {
        var stdout = isolateStdout();
        var args = parseArgs(cliArgs);
        var injector = Guice.createInjector(new MainModule(args, stdout));

        try (var serverRunner = injector.getBinding(ServerRunner.class).getProvider().get()) {
            serverRunner.run();
        } catch (IOException e) {
            throw new UncheckedIOException("Error running server", e);
        }
    }

    // We want to be very intentional with what we print to stdout
    private static PrintStream isolateStdout() {
        var stdout = System.out;
        System.setOut(System.err);
        return stdout;
    }

    private static Args parseArgs(String[] args) throws JsonProcessingException {
        Preconditions.checkArgument(
                args.length == 1, "Expected exactly one argument, found %s", args.length);
        return Args.deserialize(args[0]);
    }
}
