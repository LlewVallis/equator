package com.llewvallis.equator.cli;

import com.llewvallis.equator.lockfile.LockfileManagerImpl;
import picocli.CommandLine;

@CommandLine.Command(name = "kill", description = "Kill a running language daemon.")
public class DaemonKillCommand extends CommandBase {

    @CommandLine.Mixin private ProjectDirectoryMixin projectDirectory;

    @CommandLine.Option(
            names = {"-f", "--force"},
            description = "Whether to force kill the process.")
    private boolean force;

    @Override
    protected void run() throws ExitCliException {
        var lockfile = new LockfileManagerImpl(projectDirectory.get()).read();

        if (lockfile == null) {
            return;
        }

        var processOpt = ProcessHandle.of(lockfile.pid());

        if (processOpt.isPresent()) {
            var process = processOpt.get();

            var requested = force ? process.destroyForcibly() : process.destroy();
            if (requested) {
                System.out.println("Killed " + lockfile.pid());
            } else {
                throw new ExitCliException("Could not kill " + lockfile.pid());
            }
        } else {
            System.out.println("Not running");
        }
    }
}
