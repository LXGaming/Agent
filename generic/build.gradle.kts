plugins {
    id("agent.common")
    id("agent.publish")
    id("agent.shadow")
}

base {
    archivesName = "agent-generic"
}

dependencies {
    api(project(path = ":agent"))
    compileJar(project(path = ":agent")) {
        exclude(group = "org.jetbrains", module = "annotations")
    }
    compileJar(libs.slf4j.simple)
}

mavenPublishing {
    pom {
        description.set("Agent targeting Generic products")
    }
}

tasks.compileJava {
    dependsOn(":agent:build")
}

tasks.jar {
    manifest {
        attributes(
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Implementation-Title" to "Agent Generic",
            "Premain-Class" to "io.github.lxgaming.agent.Main"
        )
    }
}