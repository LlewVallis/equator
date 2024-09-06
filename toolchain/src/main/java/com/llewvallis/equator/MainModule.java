package com.llewvallis.equator;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.llewvallis.equator.lockfile.LockfileManager;
import com.llewvallis.equator.lockfile.LockfileManagerImpl;
import jakarta.inject.Singleton;
import java.io.PrintStream;
import java.nio.file.Path;

public class MainModule extends AbstractModule {

    private final Path projectDirectory;
    private final PrintStream stdout;

    public MainModule(Main.Args args, PrintStream stdout) {
        this.projectDirectory = args.projectDirectory();
        this.stdout = stdout;
    }

    public @interface ProjectDirectory {}

    public @interface Stdout {}

    @ProjectDirectory
    @Provides
    private Path projectDirectory() {
        return projectDirectory;
    }

    @Stdout
    @Provides
    private PrintStream stdout() {
        return stdout;
    }

    @Provides
    @Singleton
    private LockfileManager lockfileManager(@ProjectDirectory Path projectDirectory) {
        return new LockfileManagerImpl(projectDirectory);
    }
}
