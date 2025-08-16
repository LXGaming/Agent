val asmVersion: String by project
val configVersion: String by project
val slf4jVersion: String by project

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