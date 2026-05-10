import org.gradle.jvm.tasks.Jar
import java.io.ByteArrayOutputStream

plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("org.openjfx.javafxplugin") version "0.1.0"
    application
}

group = "com.productbasket"
version = "1.0.0"

repositories {
    mavenCentral()
}

javafx {
    version = "17.0.9"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.github.librepdf:openpdf:1.3.35")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

application {
    mainClass.set("com.productbasket.MainKt")
    applicationDefaultJvmArgs = listOf(
        "--add-opens", "javafx.controls/javafx.scene.control=ALL-UNNAMED",
        "--add-opens", "javafx.graphics/javafx.scene=ALL-UNNAMED"
    )
}

tasks.test {
    useJUnitPlatform()
}

// Fat JAR for distribution
tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "com.productbasket.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    with(tasks.jar.get())
}

kotlin {
    jvmToolchain(17)
}
