package com.llewvallis.equator.lockfile;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class FakeLockfileManager implements LockfileManager {

    @Nullable private LockfileData data;

    @Override
    public @Nullable LockfileData read() {
        return data;
    }

    @Override
    public void write(LockfileData data) throws AlreadyRunningException {
        if (this.data != null) {
            throw new AlreadyRunningException();
        }

        this.data = data;
    }

    @Override
    public void clear() {
        data = null;
    }
}
