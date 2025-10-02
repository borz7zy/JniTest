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

val jniSourceDir = layout.projectDirectory.dir("src/main/cpp").asFile
val jniBuildDir = layout.buildDirectory.dir("jni").get().asFile
val javaHome = System.getProperty("java.home")

application {
    mainClass.set("org.example.NativeLibKt")
    applicationDefaultJvmArgs = listOf(
        "-Djava.library.path=${jniBuildDir}"
    )
}

tasks.register<Exec>("cmakeConfigure") {
    workingDir(jniSourceDir)
    commandLine(
        "cmake", "-G\"Ninja\"",
        "-S", jniSourceDir,
        "-B", jniBuildDir,
        "-DCMAKE_BUILD_TYPE=Release"
    )
}

tasks.register<Exec>("cmakeBuild") {
    dependsOn("cmakeConfigure")
    workingDir(jniBuildDir)
    commandLine("cmake", "--build", ".")
}

tasks.withType<Jar> {
    dependsOn("cmakeBuild")
    from("${jniBuildDir}/library_exit") {
        into("native")
    }
}

tasks.named("classes") {
    dependsOn("cmakeBuild")
}

tasks.test{
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(sourceSets.main.get().output)

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })

    from("src/main/resources") {
        into("native")
    }
}