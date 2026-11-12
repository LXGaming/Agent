plugins {
    id("java-library")
    id("com.gradleup.shadow")
}

val compileJar = configurations.create("compileJar")

configurations {
    implementation {
        extendsFrom(compileJar)
    }
}

tasks.jar {
    dependsOn(tasks.shadowJar)
}

tasks.shadowJar {
    configurations = listOf(compileJar)
    duplicatesStrategy = DuplicatesStrategy.WARN

    exclude("META-INF/maven/**")
    exclude("META-INF/LICENSE.txt")

    filesMatching("simplelogger.properties") {
        filter {
            it.replace("org.slf4j.", "io.github.lxgaming.agent.lib.slf4j.")
        }
    }

    mergeServiceFiles()

    relocate("com.typesafe.config", "io.github.lxgaming.agent.lib.config")
    relocate("org.objectweb.asm", "io.github.lxgaming.agent.lib.asm")
    relocate("org.slf4j", "io.github.lxgaming.agent.lib.slf4j")
}