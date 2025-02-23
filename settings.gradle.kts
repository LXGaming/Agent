include("agent")

listOf(
    "generic"
).forEach {
    include(it)
    findProject(":${it}")?.name = "agent-${it}"
}