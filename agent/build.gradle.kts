plugins {
    id("agent.common")
    id("agent.publish")
    id("agent.resources")
}

base {
    archivesName = "agent"
}

dependencies {
    api(libs.asm.tree)
    api(libs.slf4j.api)
    api(libs.typesafe.config)
    testImplementation(libs.slf4j.simple)
}

mavenPublishing {
    pom {
        description.set("Java Agent Framework")
    }
}