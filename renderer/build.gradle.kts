plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("java")
}

version = "${rootProject.property("mod_version").toString()}-mc${rootProject.property("minecraft_version").toString()}"
group = project.property("maven_group").toString()

base.archivesName.set("${project.property("archives_base_name").toString()}-fabric")

repositories {
   maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }
    mavenCentral()
}

dependencies {
    // To change the versions, see the gradle.properties file
    minecraft("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${rootProject.property("minecraft_version")}:${rootProject.property("mappings_version")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.property("fabric_api_version")}")

    // LWJGL and Vulkan. Thanks to Minecraft, LWJGL's core is already present, so we don't need to include it here.
    implementation("org.lwjgl:lwjgl-shaderc:${rootProject.property("lwjgl_version")}")
    implementation("org.lwjgl:lwjgl-spvc:${rootProject.property("lwjgl_version")}")
    implementation("org.lwjgl:lwjgl-vma:${rootProject.property("lwjgl_version")}")
    implementation("org.lwjgl:lwjgl-vulkan:${rootProject.property("lwjgl_version")}")

    runtimeOnly("org.lwjgl:lwjgl::${rootProject.property("lwjgl_natives")}")
    runtimeOnly("org.lwjgl:lwjgl-shaderc::${rootProject.property("lwjgl_natives")}")
    runtimeOnly("org.lwjgl:lwjgl-spvc::${rootProject.property("lwjgl_natives")}")
    runtimeOnly("org.lwjgl:lwjgl-vma::${rootProject.property("lwjgl_natives")}")

    if (rootProject.property("lwjgl_natives") == "natives-macos" || rootProject.property("lwjgl_natives") == "natives-macos-arm64")
        runtimeOnly("org.lwjgl:lwjgl-vulkan::${rootProject.property("lwjgl_natives")}")
}

tasks {
    processResources {
        val expandProperties = mapOf(
            "name" to project.property("mod_name"),
            "version" to project.property("mod_version"),
            "description" to project.property("mod_description"),
            "id" to project.property("mod_id"),
            "author" to rootProject.property("mod_author"),
            "icon_file" to project.property("mod_icon_file"),
            "license" to rootProject.property("mod_license"),
            "minecraft_version" to rootProject.property("minecraft_version"),
            "fabric_api_version" to rootProject.property("fabric_api_version"),
            "fabric_loader_version" to rootProject.property("fabric_loader_version")
        )

        inputs.properties(expandProperties)

        filesMatching("fabric.mod.json") {
            expand(expandProperties)
        }
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release = 21
}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}