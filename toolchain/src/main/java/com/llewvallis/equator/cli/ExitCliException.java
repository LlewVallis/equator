package com.llewvallis.equator.cli;

public class ExitCliException extends Exception {

    public final int exitCode;

    public ExitCliException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public ExitCliException(String message) {
        this(message, 1);
    }
}
