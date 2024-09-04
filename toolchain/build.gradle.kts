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
    // For picocli
    options.compilerArgs.add("-Aproject=${project.group}/${project.name}")
}

tasks.withType<JavaCompile> {
    options.errorprone {
        disable("DefaultCharset")
        error("NullAway")
        option("NullAway:AnnotatedPackages", "com.llewvallis.equator")
    }
}

tasks.test {
    useJUnitPlatform()
}
