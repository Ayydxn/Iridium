plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating // Don't use shadow from the shadow plugin because we don't want IDEA to index this.
val developmentNeoForge: Configuration = configurations.getByName("developmentNeoForge")

configurations {
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    developmentNeoForge.extendsFrom(configurations["common"])
}

dependencies {
    neoForge("net.neoforged:neoforge:${rootProject.property("neoforge_version")}")

    modApi("dev.architectury:architectury-neoforge:${rootProject.property("architectury_version")}")

    runtimeOnly("dev.isxander:yet-another-config-lib:${rootProject.property("yacl_version")}-neoforge") {
        isTransitive = false
    }
    include("dev.isxander:yet-another-config-lib:${rootProject.property("yacl_version")}-neoforge")

    // LWJGL and Vulkan. Thanks to Minecraft, LWJGL's core is already present, so we don't need to include it here.
    implementation("org.lwjgl:lwjgl-shaderc:${rootProject.property("lwjgl_version")}")
    implementation("org.lwjgl:lwjgl-vma:${rootProject.property("lwjgl_version")}")
    implementation("org.lwjgl:lwjgl-vulkan:${rootProject.property("lwjgl_version")}")

    include("org.lwjgl:lwjgl-shaderc:${rootProject.property("lwjgl_version")}")
    include("org.lwjgl:lwjgl-vma:${rootProject.property("lwjgl_version")}")
    include("org.lwjgl:lwjgl-vulkan:${rootProject.property("lwjgl_version")}")

    forgeRuntimeLibrary("org.lwjgl:lwjgl-shaderc:${rootProject.property("lwjgl_version")}")
    forgeRuntimeLibrary("org.lwjgl:lwjgl-vma:${rootProject.property("lwjgl_version")}")
    forgeRuntimeLibrary("org.lwjgl:lwjgl-vulkan:${rootProject.property("lwjgl_version")}")
    
    runtimeOnly("org.lwjgl:lwjgl::${rootProject.property("lwjgl_natives")}")
    runtimeOnly("org.lwjgl:lwjgl-shaderc::${rootProject.property("lwjgl_natives")}")
    runtimeOnly("org.lwjgl:lwjgl-vma::${rootProject.property("lwjgl_natives")}")
    if (rootProject.property("lwjgl_natives") == "natives-macos" || rootProject.property("lwjgl_natives") == "natives-macos-arm64")
        runtimeOnly("org.lwjgl:lwjgl-vulkan::${rootProject.property("lwjgl_natives")}")

    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionNeoForge")) { isTransitive = false }
}

val javaComponent = components.getByName<AdhocComponentWithVariants>("java")
javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
    skip()
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("fabric.mod.json")
        exclude("architectury.common.json")

        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
        archiveClassifier.set("neoforge")
    }

    jar {
        archiveClassifier.set("dev")
    }

    sourcesJar {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenForge") {
            artifactId = "${rootProject.property("archives_base_name")}-${project.name}"
            from(javaComponent)
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
    }
}
