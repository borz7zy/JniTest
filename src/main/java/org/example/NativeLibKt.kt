package org.example

import java.io.File
import java.nio.file.Files

class NativeLibKt {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val lib = NativeLibKt()
            println(lib.stringFromNative())
        }

        init {
            val os = System.getProperty("os.name").lowercase().let {
                when {
                    it.contains("windows") -> "windows"
                    it.contains("mac") || it.contains("darwin") -> "macos"
                    it.contains("linux") -> "ubuntu"
                    else -> throw RuntimeException("Unsupported OS: $it")
                }
            }

            val arch = System.getProperty("os.arch").lowercase().let {
                when {
                    it.contains("amd64") || it.contains("x86_64") -> "x86_64"
                    it.contains("x86") || it.contains("i386") || it.contains("i686") -> "x86"
                    it.contains("aarch64") || it.contains("arm64") -> "arm64"
                    it.contains("arm") -> "arm32"
                    else -> throw RuntimeException("Unsupported architecture: $it")
                }
            }

            val ext = when (os) {
                "windows" -> "dll"
                "macos" -> "dylib"
                "ubuntu" -> "so"
                else -> throw RuntimeException("Unsupported OS: $os")
            }

            val libName = "jnitest_${os}_${arch}.$ext"

            val libFile = File(System.getProperty("user.dir"), "build/run/native/$libName")
            if (libFile.exists()) {
                System.load(libFile.absolutePath)
            }else{
                val tempDir = Files.createTempDirectory("jni_libs").toFile()
                tempDir.deleteOnExit()
                val tempLib = File(tempDir, libName)
                val inputStream = NativeLibKt::class.java.getResourceAsStream("/native/$libName")
                    ?: throw RuntimeException("Cannot find native library $libName in JAR resources")
                inputStream.use { input -> tempLib.outputStream().use { output -> input.copyTo(output) } }
                System.load(tempLib.absolutePath)
            }
        }
    }

    external fun stringFromNative(): String
}
