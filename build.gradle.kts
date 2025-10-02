import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.2.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// === Build type ===
val buildType: String = if (project.hasProperty("buildType")) {
    project.property("buildType").toString()
} else "debug"

// === Kotlin compiler options ===
val kotlinDebugOptions = listOf<String>()
val kotlinReleaseOptions = listOf("-Xopt-in=kotlin.ExperimentalStdlibApi")

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        freeCompilerArgs.set(if (buildType.lowercase() == "release") kotlinReleaseOptions else kotlinDebugOptions)
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

// === JNI / C++ build ===
val jniBuildType = "RelWithDebInfo"
val jniSourceDir = layout.projectDirectory.dir("src/main/cpp").asFile
val jniBuildDir = layout.buildDirectory.dir("jni").get().asFile
val nativeRunDir = layout.buildDirectory.dir("run/native").get().asFile

// === offBuildLibs ===
val offBuildLibs = project.findProperty("offBuildLibs")?.toString()?.toBoolean() ?: false

if (!offBuildLibs) {
    tasks.register<Exec>("cmakeConfigure") {
        workingDir(jniSourceDir)
        commandLine(
            "cmake",
            "-G", "Ninja",
            "-S", jniSourceDir.absolutePath,
            "-B", jniBuildDir.absolutePath,
            "-DCMAKE_BUILD_TYPE=$jniBuildType"
        )
    }

    tasks.register<Exec>("cmakeBuild") {
        dependsOn("cmakeConfigure")
        workingDir(jniBuildDir)
        commandLine("cmake", "--build", ".")
    }

    tasks.register<Copy>("copyNativeLibs") {
        dependsOn("cmakeBuild")
        from("${jniBuildDir}/library_exit")
        into(nativeRunDir)
    }

    tasks.named<JavaExec>("run") {
        dependsOn("copyNativeLibs")
        jvmArgs = listOf("-Djava.library.path=${nativeRunDir.absolutePath}")
    }
} else {
    tasks.named<JavaExec>("run") {
        doFirst {
            println("Running with offBuildLibs=true, using native libs from resources")
        }
    }
}

// === JAR tasks ===
tasks.jar {
    manifest { attributes["Main-Class"] = application.mainClass.get() }
    if (offBuildLibs) {
        from("src/main/resources/native") { into("native") }
    } else {
        dependsOn("cmakeBuild")
        from("${jniBuildDir}/library_exit") { into("native") }
    }
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest { attributes["Main-Class"] = application.mainClass.get() }

    from(sourceSets.main.get().output)
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    if (offBuildLibs) {
        from("src/main/resources/native") { into("native") }
    } else {
        dependsOn("cmakeBuild")
        from("${jniBuildDir}/library_exit") { into("native") }
    }
}

application {
    mainClass.set("org.example.NativeLibKt")
}
