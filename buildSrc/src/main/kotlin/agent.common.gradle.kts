import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java-library")
}

val libs = the<LibrariesForLibs>()

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.jetbrains.annotations)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform)
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.compileJava {
    options.compilerArgs.add("-Xlint:-options")
}

tasks.compileTestJava {
    sourceCompatibility = JavaVersion.VERSION_25.toString()
    targetCompatibility = JavaVersion.VERSION_25.toString()
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to "Agent",
            "Implementation-Vendor" to "Alex Thomson",
            "Implementation-Version" to project.version.toString(),
            "Specification-Title" to "Agent",
            "Specification-Vendor" to "Alex Thomson",
            "Specification-Version" to "1"
        )
    }
}

tasks.javadoc {
    isFailOnError = false
    options {
        this as CoreJavadocOptions

        addStringOption("Xdoclint:none", "-quiet")
    }
}

tasks.test {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }

    useJUnitPlatform()
}