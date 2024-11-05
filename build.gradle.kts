import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java")
    id("maven-publish")
    id("signing")
    id("com.gradleup.shadow") version "8.3.4" apply false
}

subprojects {
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
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

        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.base.archivesName.get()
                version = project.version.toString()
                pom {
                    name = "Agent"
                    url = "https://github.com/LXGaming/Agent"
                    developers {
                        developer {
                            id = "lxgaming"
                            name = "LXGaming"
                        }
                    }
                    issueManagement {
                        system = "GitHub Issues"
                        url = "https://github.com/LXGaming/Agent/issues"
                    }
                    licenses {
                        license {
                            name = "The Apache License, Version 2.0"
                            url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                        }
                    }
                    scm {
                        connection = "scm:git:https://github.com/LXGaming/Agent.git"
                        developerConnection = "scm:git:https://github.com/LXGaming/Agent.git"
                        url = "https://github.com/LXGaming/Agent"
                    }
                }
            }
        }
        repositories {
            if (project.hasProperty("sonatypeUsername") && project.hasProperty("sonatypePassword")) {
                maven {
                    name = "sonatype"
                    url = if (project.version.toString().contains("-SNAPSHOT")) {
                        uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
                    } else {
                        uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                    }

                    credentials {
                        username = project.property("sonatypeUsername").toString()
                        password = project.property("sonatypePassword").toString()
                    }
                }
            }
        }
    }

    signing {
        if (project.hasProperty("signingKey") && project.hasProperty("signingPassword")) {
            useInMemoryPgpKeys(
                project.property("signingKey").toString(),
                project.property("signingPassword").toString()
            )
        }

        sign(publishing.publications["maven"])
    }

    tasks.jar {
        manifest {
            attributes(
                "Implementation-Title" to "Agent",
                "Implementation-Vendor" to "LX_Gaming",
                "Implementation-Version" to project.version.toString(),
                "Specification-Title" to "Agent",
                "Specification-Vendor" to "LX_Gaming",
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

    tasks.processResources {
        val excludedProjects = listOf(
            "agent"
        )

        outputs.upToDateWhen { false }
        if (!excludedProjects.contains(project.name)) {
            dependsOn("copyResources")
            finalizedBy("mergeResources")
        }
    }

    tasks.test {
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
        }

        useJUnitPlatform()
    }

    tasks.register<Copy>("copyResources") {
        from(project(":agent").file("src/main/resources/simplelogger.properties"))
        filter {
            it.replace("org.slf4j", "io.github.lxgaming.agent.lib.slf4j")
        }
        into(layout.buildDirectory.dir("resources/main"))
    }

    tasks.register("mergeResources") {
        dependsOn(tasks.processResources)

        doLast {
            val inputFiles = listOf(
                project(":agent"),
                project
            ).map {
                it.file("src/main/resources/agent.conf")
            }

            val outputFile = layout.buildDirectory.file("resources/main/agent.conf").get().asFile
            outputFile.parentFile.mkdirs()

            outputFile.writer().use { writer ->
                for ((index, inputFile) in inputFiles.withIndex()) {
                    if (index != 0) {
                        writer.appendLine()
                    }

                    inputFile.reader().use { reader ->
                        reader.copyTo(writer)
                    }
                }
            }
        }
    }
}

tasks.jar {
    enabled = false
}