package com.llewvallis.equator.cli;

import java.util.concurrent.Callable;

public abstract class CommandBase extends UnexecutableCommandBase implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        run();
        return 0;
    }

    protected abstract void run() throws Exception;
}
