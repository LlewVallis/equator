package com.llewvallis.equator.cli;

import com.llewvallis.equator.Main;
import com.llewvallis.equator.lockfile.LockfileManagerImpl;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import picocli.CommandLine;

// TODO: Support named pipes (Windows) and socket files (Unix)
@CommandLine.Command(
        name = "daemon",
        description = "Start the language server daemon.",
        subcommands = DaemonKillCommand.class)
public class DaemonCommand extends CommandBase {

    @CommandLine.Mixin private ProjectDirectoryMixin projectDirectory;

    @Override
    protected void run() throws ExitCliException {
        var port = upsertServer();
        System.out.println("Available on port " + port);
    }

    private int upsertServer() throws ExitCliException {
        var existingServer = new LockfileManagerImpl(projectDirectory.get()).read();
        if (existingServer != null) {
            return existingServer.port();
        }

        try {
            return startServer();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to start daemon", e);
        }
    }

    private int startServer() throws IOException, ExitCliException {
        var javaCommand = ProcessHandle.current().info().command().orElse("java");
        var classPath = ManagementFactory.getRuntimeMXBean().getClassPath();
        var mainClass = Main.class.getName();
        var args = new Main.Args(projectDirectory.get()).serialize();

        var process =
                new ProcessBuilder(javaCommand, "-cp", classPath, mainClass, args)
                        .directory(projectDirectory.get().toFile())
                        .redirectError(ProcessBuilder.Redirect.INHERIT)
                        .start();

        var inputBytes = process.getInputStream().readAllBytes();
        return Integer.parseInt(new String(inputBytes));
    }
}
