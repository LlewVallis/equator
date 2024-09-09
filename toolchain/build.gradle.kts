import net.ltgt.gradle.errorprone.errorprone
import okio.IOException

plugins {
    application
    id("com.diffplug.spotless") version "7.0.0.BETA2"
    id("net.ltgt.errorprone") version "4.0.1"
}

group = "com.llewvallis.equator"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

spotless {
    encoding("utf-8")

    java {
        googleJavaFormat().aosp().reflowLongStrings()
    }

    format("misc") {
        target("**/*.gradle.kts", "**/.gitignore", "**/.gitattributes")
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
}


application {
    mainClass.set("com.llewvallis.equator.cli.CliMain")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:33.3.0-jre")

    implementation("com.google.inject:guice:7.0.0")

    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.7")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    implementation("info.picocli:picocli:4.7.6")
    annotationProcessor("info.picocli:picocli-codegen:4.7.6")

    implementation("org.jspecify:jspecify:1.0.0")
    errorprone("com.google.errorprone:error_prone_core:2.31.0")
    errorprone("com.uber.nullaway:nullaway:0.11.2")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.assertj:assertj-core:3.26.3")
}

tasks.compileJava {
    options.compilerArgs.addAll(listOf(
        "-Werror",
        // For picocli
        "-Aproject=${project.group}/${project.name}",
    ))
}

tasks.withType<JavaCompile> {
    options.errorprone {
        disable("DefaultCharset", "CloseableProvides")

        error("NullAway")
        option("NullAway:AnnotatedPackages", "com.llewvallis.equator")
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    val dependencies = configurations.runtimeClasspath.get().map(::zipTree)
    from(dependencies)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest.attributes["Main-Class"] = application.mainClass.get()
}

tasks.register<PackageTask>("package") {
    group = "Distribution"
    description = "Packages the application with its own JRE"

    dependsOn(tasks.jar)

    outputName = "equator"
    javaHome = javaToolchains.launcherFor(java.toolchain).get().metadata.installationPath.asFile.path
    mainJar = tasks.jar.get().archiveFile.get()
    outputDir = layout.buildDirectory.get().dir("package")
}

tasks.build {
    dependsOn("package")
}

abstract class PackageTask : DefaultTask() {

    @get:Input
    lateinit var outputName: String

    @get:Input
    lateinit var javaHome: String

    @get:InputFile
    lateinit var mainJar: RegularFile

    @get:OutputDirectory
    lateinit var outputDir: Directory

    @TaskAction
    fun run() {
        cleanOutputDirectory()
        val commandLineArgs = buildCommandLineArgs()
        runJPackage(commandLineArgs)
    }

    private fun cleanOutputDirectory() {
        val existingDirectory = outputDir.asFile.resolve(outputName)
        if (existingDirectory.exists()) {
            existingDirectory.deleteRecursively()
        }
    }

    private fun buildCommandLineArgs(): List<String> {
        val jpackagePath = File(javaHome).resolve("bin/jpackage")
        val commandLineArgs = mutableListOf(jpackagePath.path)

        commandLineArgs.add("--name")
        commandLineArgs.add(outputName)

        commandLineArgs.add("--dest")
        commandLineArgs.add(outputDir.asFile.path)

        commandLineArgs.add("--type")
        commandLineArgs.add("app-image")

        commandLineArgs.add("--main-jar")
        commandLineArgs.add(mainJar.asFile.path)

        commandLineArgs.add("--input")
        commandLineArgs.add(mainJar.asFile.parent)

        return commandLineArgs
    }

    private fun runJPackage(commandLineArgs: List<String>) {
        val process = ProcessBuilder(commandLineArgs)
            .redirectOutput(ProcessBuilder.Redirect.DISCARD)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val stderr = process.errorReader().readText()
        val exitCode = process.waitFor()

        if (exitCode != 0) {
            System.err.println(stderr)
            throw IOException("jpackage command failed with code $exitCode")
        }
    }
}
