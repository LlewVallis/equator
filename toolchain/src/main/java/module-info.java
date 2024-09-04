import org.jspecify.annotations.NullMarked;

@NullMarked
module toolchain.main {
    requires com.fasterxml.jackson.databind;
    requires com.google.common;
    requires com.google.guice;
    requires info.picocli;
    requires jakarta.inject;
    requires org.slf4j;
    requires org.jspecify;
    requires java.management;
}
