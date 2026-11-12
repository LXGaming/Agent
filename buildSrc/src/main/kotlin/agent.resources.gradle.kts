plugins {
    id("java")
}

tasks.jar {
    exclude("agent.conf")
    exclude("simplelogger.properties")
}

tasks.processResources {
    val parentProject = project(":agent")
    if (parentProject == project) {
        return@processResources
    }

    val parentResources = parentProject.sourceSets.main.get().resources
    from(parentResources) {
        include("simplelogger.properties")
    }

    val parentAgentConf = parentResources.matching {
        include("agent.conf")
    }.singleFile
    inputs.file(parentAgentConf)

    doLast {
        val agentConf = destinationDir.resolve("agent.conf")
        agentConf.writeText(parentAgentConf.readText() + "\n" + agentConf.readText())
    }
}