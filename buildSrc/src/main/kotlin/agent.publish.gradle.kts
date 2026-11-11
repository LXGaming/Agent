plugins {
    id("signing")
    id("com.vanniktech.maven.publish")
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
    val signingKey = project.findProperty("signingKey") as String?
    val signingPassword = project.findProperty("signingPassword") as String?
    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
    }
}