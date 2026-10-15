val asmVersion = project.property("asmVersion") as String
val configVersion = project.property("configVersion") as String
val slf4jVersion = project.property("slf4jVersion") as String

base {
    archivesName = "agent"
}

dependencies {
    api("com.typesafe:config:${configVersion}")
    api("org.ow2.asm:asm-tree:${asmVersion}")
    api("org.slf4j:slf4j-api:${slf4jVersion}")
    testImplementation("org.slf4j:slf4j-simple:${slf4jVersion}")
}

mavenPublishing {
    pom {
        description.set("Java Agent Framework")
    }
}

tasks.processResources {
    from("../LICENSE") {
        into("META-INF")
        rename { "${it}-Agent" }
    }

    exclude("agent.conf")
}

tasks.shadowJar {
    enabled = false
}