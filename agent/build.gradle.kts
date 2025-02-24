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

publishing {
    publications {
        named<MavenPublication>("maven") {
            from(components["java"])
            pom {
                description = "Java Agent Framework"
            }
        }
    }
}

tasks.processResources {
    from("../LICENSE")
    rename("LICENSE", "LICENSE-Agent")
    exclude("simplelogger.properties")
}

tasks.shadowJar {
    enabled = false
}