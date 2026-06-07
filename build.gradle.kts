plugins {
	id("net.fabricmc.fabric-loom-remap")
	`maven-publish`
}

version = "${providers.gradleProperty("mod_version").get()}-mc${providers.gradleProperty("minecraft_version").get()}"
group = providers.gradleProperty("maven_group").get()

base.archivesName.set("${providers.gradleProperty("archives_base_name").get()}-fabric")

val ffm by sourceSets.creating {
	compileClasspath += sourceSets.main.get().output
	runtimeClasspath += sourceSets.main.get().output
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	mappings("net.fabricmc:yarn:${providers.gradleProperty("mappings_version").get()}:v2")
	modImplementation("net.fabricmc:fabric-loader:${providers.gradleProperty("fabric_loader_version").get()}")

	// Fabric API. This is technically optional, but you probably want it anyway.
	modImplementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")

	// JUnit
	testImplementation(platform("org.junit:junit-bom:${providers.gradleProperty("junit_version").get()}"))
	testImplementation("org.junit.jupiter:junit-jupiter")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// FFM source set sees the main output
	"ffmImplementation"(sourceSets.main.get().output)
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
	when (name) {
		"compileFfmJava" -> options.release = 22
		else -> options.release = 17
	}
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.test {
	useJUnitPlatform()

	// Expose sun.misc.Unsafe to test code (needed by ByteBufferNativeBuffer tests).
	jvmArgs("--add-opens", "java.base/sun.misc=ALL-UNNAMED")

	// Point to the native library output location (populated after CMake build).
	jvmArgs("-Djava.library.path=${layout.buildDirectory.get()}/native/alignment")

	// FFM test class must be on the test classpath.
	testClassesDirs = files(sourceSets.test.get().output.classesDirs, ffm.output.classesDirs)
	classpath = files(sourceSets.test.get().runtimeClasspath, ffm.output)
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from(ffm.output)
	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

// configure the maven publication
publishing {
	publications {
		register<MavenPublication>("mavenJava") {
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
