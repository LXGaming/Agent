plugins {
    `kotlin-dsl`
}

fun Provider<PluginDependency>.asDependency(): Provider<String> = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
}

dependencies {
    implementation(libs.plugins.gradleup.shadow.asDependency())
    implementation(libs.plugins.vanniktech.maven.publish.asDependency())
    // https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}