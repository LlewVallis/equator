package com.llewvallis.equator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.llewvallis.equator.server.ServerModule;
import com.llewvallis.equator.server.ServerRunner;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

public class Main {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static List<Module> modules(Args args, PrintStream stdout) {
        return List.of(new MainModule(args, stdout), new ServerModule());
    }

    public record Args(Path projectDirectory) {

        public String serialize() {
            try {
                var json = MAPPER.writeValueAsString(this);
                return Base64.getUrlEncoder().encodeToString(json.getBytes());
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }

        public static Args deserialize(String encoded) {
            try {
                var json = Base64.getUrlDecoder().decode(encoded);
                return MAPPER.readValue(json, Args.class);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public static void main(String[] cliArgs) {
        var stdout = isolateStdout();
        var args = parseArgs(cliArgs);
        var injector = Guice.createInjector(modules(args, stdout));

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

    private static Args parseArgs(String[] args) {
        Preconditions.checkArgument(
                args.length == 1, "Expected exactly one argument, found %s", args.length);
        return Args.deserialize(args[0]);
    }
}
