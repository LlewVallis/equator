package com.llewvallis.equator.lockfile;

import org.jspecify.annotations.Nullable;

public interface LockfileManager {

    record LockfileData(long pid, int port) {}

    class AlreadyRunningException extends Exception {}

    @Nullable LockfileData read();

    void write(LockfileData data) throws AlreadyRunningException;

    void clear();
}
