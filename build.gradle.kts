plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
}

version = "${rootProject.property("mod_version").toString()}-mc${rootProject.property("minecraft_version").toString()}"
group = rootProject.property("maven_group").toString()

base.archivesName.set("${rootProject.property("archives_base_name").toString()}-fabric")

// Sets which platforms native libraries from LWJGL we will use.
rootProject.setProperty("lwjgl_natives", Pair(System.getProperty("os.name")!!, System.getProperty("os.arch")!!).let { (name, arch) ->
    when {
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else if (arch.startsWith("ppc"))
                "natives-linux-ppc64le"
            else if (arch.startsWith("riscv"))
                "natives-linux-riscv64"
            else
                "natives-linux"
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) }     ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
        arrayOf("Windows").any { name.startsWith(it) }                ->
            if (arch.contains("64"))
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            else
                "natives-windows-x86"
        else                                                                            ->
            throw Error("Unrecognized or unsupported platform. Please set the \"lwjgl_natives\" property manually")
    }
})

loom {
    accessWidenerPath = file("src/main/resources/iridium.accesswidener")
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.

    maven("https://maven.parchmentmc.org") {
        name = "ParchmentMC"
    }

    maven("https://maven.isxander.dev/releases") {
        name = "Xander's Maven"
    }

    maven("https://maven.terraformersmc.com/") {
        name = "TerraformersMC"
    }

    maven("https://api.modrinth.com/maven")
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

    // YetAnotherConfigLib. Used for Iridium's custom settings screen.
    modImplementation("dev.isxander:yet-another-config-lib:${rootProject.property("yacl_version")}")

    // ModMenu
    modImplementation("com.terraformersmc:modmenu:${rootProject.property("modmenu_version")}")

    // Moonblast Renderer. The actual internal renderer implementation for Iridium.
    implementation(project(path = ":renderer", configuration = "namedElements"))
    include(project(path = ":renderer", configuration = "namedElements"))

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

    // Utility Libraries
    implementation("org.apache.maven:maven-artifact:${rootProject.property("maven_artifact_version")}")
    include("org.apache.maven:maven-artifact:${rootProject.property("maven_artifact_version")}")
}

tasks {
    processResources {
        val expandProperties = mapOf(
            "name" to rootProject.property("mod_name"),
            "version" to rootProject.property("mod_version"),
            "description" to rootProject.property("mod_description"),
            "id" to project.rootProject.property("mod_id"),
            "author" to rootProject.property("mod_author"),
            "icon_file" to rootProject.property("mod_icon_file"),
            "license" to rootProject.property("mod_license"),
            "minecraft_version" to rootProject.property("minecraft_version"),
            "fabric_api_version" to rootProject.property("fabric_api_version"),
            "fabric_loader_version" to rootProject.property("fabric_loader_version"),
            "yacl_version" to rootProject.property("yacl_version")
        )

        inputs.properties(expandProperties)

        filesMatching("fabric.mod.json") {
            expand(expandProperties)
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${base.archivesName.toString()}" }
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

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "${rootProject.property("archives_base_name")}-${project.name}"
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
