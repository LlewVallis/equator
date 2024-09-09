import net.ltgt.gradle.errorprone.errorprone

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
    manifest.attributes["Main-Class"] = application.mainClass.get()
}

val jrePath = layout.buildDirectory.dir("jre")
val jreTask = tasks.register<Exec>("jre") {
    outputs.dir(jrePath)

    val javaHome = javaToolchains.launcherFor(java.toolchain).get().metadata.installationPath.asFile
    val jlinkPath = javaHome.resolve("bin/jlink").path

    val jdkModules = listOf("java.base", "java.management")

    doFirst {
        // jlink complains if the directory already exists
        jrePath.get().asFile.deleteRecursively()
    }

    commandLine(
        jlinkPath,
        "--strip-debug", "--no-man-pages", "--no-header-files",
        "--add-modules", jdkModules.joinToString(","),
        "--output", jrePath.get().asFile.path,
        "--launcher", "test=java.base/java.util.regex.PrintPattern"
    )
}

val packageTask = tasks.register<Copy>("package") {
    group = "Distribution"
    description = "Creates a distributable package, including a JRE"

    val output = layout.buildDirectory.dir("package/equator")

    doFirst {
        output.get().asFile.deleteRecursively()
    }

    into(output)

    dependsOn(tasks.jar)
    from(tasks.jar.get().archiveFile) {
        into("jars")
    }
    rename("toolchain-(.+).jar", "equator.jar")

    from(file("src/main/package"))

    dependsOn(configurations.runtimeClasspath)
    from(configurations.runtimeClasspath.get()) {
        into("jars")
    }

    dependsOn(jreTask)
    from(jrePath) {
        into("jre")
    }
}

tasks.build {
    dependsOn(packageTask)
}
