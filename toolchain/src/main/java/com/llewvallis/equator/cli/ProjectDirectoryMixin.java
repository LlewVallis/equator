package com.llewvallis.equator.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import picocli.CommandLine;

public class ProjectDirectoryMixin {

    @CommandLine.Parameters(
            paramLabel = "project-directory",
            description = "Path to the Equator project (or the working directory by" + " default).",
            arity = "0..1")
    private Path path;

    private Path cached;

    public Path get() throws ExitCliException {
        if (cached == null) {
            cached = path == null ? Path.of("").toAbsolutePath() : path;

            if (!Files.isDirectory(cached)) {
                throw new ExitCliException("Project directory " + cached + " is not a directory");
            }
        }

        return cached;
    }
}
