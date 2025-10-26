import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java")
    id("signing")
    id("com.gradleup.shadow") version "9.2.2" apply false
    id("com.vanniktech.maven.publish") version "0.34.0"
}

subprojects {
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "com.vanniktech.maven.publish")
    apply(plugin = "java-library")
    apply(plugin = "signing")

    val annotationsVersion: String by project
    val junitVersion: String by project

    group = "io.github.lxgaming"

    val compileJar: Configuration by configurations.creating

    configurations {
        implementation {
            extendsFrom(compileJar)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.jetbrains:annotations:${annotationsVersion}")
        testImplementation("org.junit.jupiter:junit-jupiter:${junitVersion}")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    mavenPublishing {
        publishToMavenCentral()
        signAllPublications()

        pom {
            name.set("Agent")
            url.set("https://github.com/LXGaming/Agent")
            developers {
                developer {
                    id.set("lxgaming")
                    name.set("LXGaming")
                }
            }
            issueManagement {
                system.set("GitHub Issues")
                url.set("https://github.com/LXGaming/Agent/issues")
            }
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            scm {
                connection.set("scm:git:https://github.com/LXGaming/Agent.git")
                developerConnection.set("scm:git:https://github.com/LXGaming/Agent.git")
                url.set("https://github.com/LXGaming/Agent")
            }
        }
    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
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
}

mavenPublishing {
    configure(JavaLibrary(
        javadocJar = JavadocJar.None(),
        sourcesJar = false
    ))
}

tasks.jar {
    enabled = false
}