package com.llewvallis.equator.cli;

import picocli.CommandLine;

// TODO: Support the --version flag
// TODO: Add tests for the CLI interface
@CommandLine.Command(
        name = "equator",
        subcommands = {DaemonCommand.class, CompletionsCommand.class})
public class CliMain extends UnexecutableCommandBase {

    public static void main(String[] args) {
        var exitCode =
                new CommandLine(new CliMain())
                        .setExecutionExceptionHandler(
                                (ex, commandLine, parse) -> {
                                    if (ex instanceof ExitCliException exitException) {
                                        System.err.println(ex.getMessage());
                                        return exitException.exitCode;
                                    }

                                    throw ex;
                                })
                        .execute(args);

        System.exit(exitCode);
    }
}
