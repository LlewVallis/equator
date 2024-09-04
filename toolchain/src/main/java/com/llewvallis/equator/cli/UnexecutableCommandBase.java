package com.llewvallis.equator.cli;

import picocli.CommandLine;

public abstract class UnexecutableCommandBase {

    @SuppressWarnings("unused")
    @CommandLine.Mixin
    private HelpMixin help;

    private static class HelpMixin {

        @SuppressWarnings("unused")
        @CommandLine.Option(
                names = {"-h", "--help"},
                usageHelp = true,
                description = "Display usage information.")
        private boolean helpRequested;
    }
}
