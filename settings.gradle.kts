include("agent")

listOf(
    "generic",
    "sonatype"
).forEach {
    include(it)
    findProject(":${it}")?.name = "agent-${it}"
}