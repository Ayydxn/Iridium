architectury {
    val enabled_platforms: String by rootProject
    common(enabled_platforms.split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/${project(":common").property("mod_id")}.accesswidener"))
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")

    // Remove the next line if you don't want to depend on the API
    modApi("dev.architectury:architectury:${rootProject.property("architectury_version")}")

    // Can't always rely on 'common' builds for the latest versions of YACL being available.
    // So, we just use the Fabric version.
    modImplementation("dev.isxander:yet-another-config-lib:${rootProject.property("yacl_version")}-fabric")

    // LWJGL and Vulkan. Thanks to Minecraft, LWJGL's core is already present, so we don't need to include it here.
    implementation("org.lwjgl:lwjgl-shaderc:${rootProject.property("lwjgl_version")}")
    implementation("org.lwjgl:lwjgl-vma:${rootProject.property("lwjgl_version")}")
    implementation("org.lwjgl:lwjgl-vulkan:${rootProject.property("lwjgl_version")}")
    runtimeOnly("org.lwjgl:lwjgl::${rootProject.property("lwjgl_natives")}")
    runtimeOnly("org.lwjgl:lwjgl-shaderc::${rootProject.property("lwjgl_natives")}")
    runtimeOnly("org.lwjgl:lwjgl-vma::${rootProject.property("lwjgl_natives")}")
    if (rootProject.property("lwjgl_natives") == "natives-macos" || rootProject.property("lwjgl_natives") == "natives-macos-arm64")
        runtimeOnly("org.lwjgl:lwjgl-vulkan::${rootProject.property("lwjgl_natives")}")

    // Maven Artifact
    implementation("org.apache.maven:maven-artifact:${rootProject.property("maven_artifact_version")}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = rootProject.property("archives_base_name").toString()
            from(components.getByName<AdhocComponentWithVariants>("java"))
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
