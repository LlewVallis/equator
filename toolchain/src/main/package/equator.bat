@echo off
set DIR=%~dp0
"%DIR%\jre\bin\java" "@%DIR%\java-opts" -cp "%DIR%\jars\*" com.llewvallis.equator.cli.CliMain %*