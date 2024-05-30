import groovy.lang.Closure
import io.github.pacifistmc.forgix.plugin.ForgixMergeExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI

plugins {
    java
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.6-SNAPSHOT" apply false
    id("org.ajoberstar.grgit") version "5.2.2"
    id("io.github.pacifistmc.forgix") version "1.2.6"
}

// Sets which platforms native libraries from LWJGL we will use.
rootProject.setProperty("lwjgl_natives", Pair(
        System.getProperty("os.name")!!,
        System.getProperty("os.arch")!!
).let { (name, arch) ->
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

architectury {
    minecraft = rootProject.property("minecraft_version").toString()
}

@Suppress("UNCHECKED_CAST")
configure<ForgixMergeExtension> {
    group = "${rootProject.property("maven_group")}.iridium"
    mergedJarName = "iridium-${getIridiumVersionString()}.jar"
    outputDir = "build/forgix"

    fabric(closureOf<ForgixMergeExtension.FabricContainer> {
        jarLocation = "build/libs/iridium-${getIridiumVersionString()}-fabric.jar"
    } as Closure<ForgixMergeExtension.FabricContainer>)

    custom(closureOf<ForgixMergeExtension.CustomContainer> {
        projectName = "neoforge"
        jarLocation = "build/libs/iridium-${getIridiumVersionString()}-neoforge.jar"
    } as Closure<ForgixMergeExtension.CustomContainer>)
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")
    loom.silentMojangMappingsLicense()

    dependencies {
        "minecraft"("com.mojang:minecraft:${rootProject.property("minecraft_version")}")
        "mappings"("org.quiltmc:quilt-mappings:${rootProject.property("minecraft_version")}+build.${rootProject.property("quilt_mappings_version")}:intermediary-v2")
    }

    tasks.processResources {
        val expandProps = mapOf(
                "name" to rootProject.property("mod_name"),
                "version" to getIridiumVersionString(),
                "description" to rootProject.property("mod_description"),
                "id" to rootProject.property("mod_id"),
                "author" to rootProject.property("mod_author"),
                "icon_path" to rootProject.property("icon_path"),
                "issue_tracker_url" to rootProject.property("issue_tracker_url"),
                "license" to rootProject.property("license"),
                "minecraft_version" to rootProject.property("minecraft_version"),
                "minecraft_version_range" to rootProject.property("minecraft_version_range"),
                "architectury_version" to rootProject.property("architectury_version"),
                "architectury_version_range" to rootProject.property("architectury_version_range"),
                "fabric_loader_version" to rootProject.property("fabric_loader_version"),
                "fabric_version" to rootProject.property("fabric_api_version"),
                "neoforge_version" to rootProject.property("neoforge_version"),
                "neoforge_version_range" to rootProject.property("neoforge_version_range"),
                "yacl_version" to rootProject.property("yacl_version")
        )

        inputs.properties(expandProps)

        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "META-INF/mods.toml", "*.mixins.json")) {
            expand(expandProps)
        }
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    base.archivesName.set(rootProject.property("archives_base_name").toString())
    version = getIridiumVersionString()
    group = rootProject.property("maven_group").toString()

    repositories {
        // Add repositories to retrieve artifacts from in here.
        // You should only use this when depending on other mods because
        // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
        // See https://docs.gradle.org/current/userguide/declaring_repositories.html
        // for more information about repositories.

        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
        }

        maven("https://maven.quiltmc.org/repository/release") {
            name = "Quilt"
        }

        maven("https://maven.isxander.dev/releases") {
            name = "Xander's Maven"
        }

        maven("https://maven.flashyreese.me/releases") {
            name = "FlashyReese's Maven"
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = 17
    }

    java {
        withSourcesJar()
    }
}

fun getIridiumVersionString(): String {
    val stringBuilder = StringBuilder()
    val isADistributionBuild = project.hasProperty("build.distribution")
    val buildID = System.getenv("GITHUB_RUN_NUMBER")

    stringBuilder.append("${rootProject.property("mod_version")}-mc${rootProject.property("minecraft_version")}")

    if (!isADistributionBuild) {
        stringBuilder.append("+snapshot")

        if (buildID != null) {
            stringBuilder.append("-build.${buildID}")
        } else {
            stringBuilder.append("-local")
        }
    }

    return stringBuilder.toString()
}
