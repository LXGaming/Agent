val slf4jVersion: String by project

base {
    archivesName = "agent-sonatype"
}

dependencies {
    api(project(path = ":agent"))
    compileJar(project(path = ":agent")) {
        exclude(module = "annotations")
    }
    compileJar("org.slf4j:slf4j-simple:${slf4jVersion}")
}

publishing {
    publications {
        named<MavenPublication>("maven") {
            from(components["java"])
            pom {
                description = "Agent targeting Sonatype products"
            }
        }
    }
}

tasks.compileJava {
    dependsOn(":agent:build")
}

tasks.jar {
    dependsOn(tasks.shadowJar)
    manifest {
        attributes(
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Implementation-Title" to "Agent Sonatype",
            "Premain-Class" to "io.github.lxgaming.agent.Main",
        )
    }

    exclude("simplelogger.properties")
}

tasks.shadowJar {
    configurations = listOf(project.configurations.compileJar.get())

    exclude("META-INF/maven/**")
    exclude("META-INF/versions/**")
    exclude("META-INF/LICENSE.txt")
    exclude("module-info.class")

    mergeServiceFiles()

    relocate("com.typesafe.config", "io.github.lxgaming.agent.lib.config")
    relocate("org.objectweb.asm", "io.github.lxgaming.agent.lib.asm")
    relocate("org.slf4j", "io.github.lxgaming.agent.lib.slf4j")
}