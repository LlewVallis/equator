package com.llewvallis.equator.cli;

import picocli.AutoComplete;
import picocli.CommandLine;

@CommandLine.Command(
        name = "completions",
        description = "Print a Bash/ZSH shell completion script.")
public class CompletionsCommand extends CommandBase {

    @CommandLine.Spec private CommandLine.Model.CommandSpec spec;

    @Override
    protected void run() {
        var script = AutoComplete.bash(spec.root().name(), spec.root().commandLine());
        // We bring our own newline to avoid CRLF, which can cause issues in scripts
        System.out.print(script + "\n");
    }
}
