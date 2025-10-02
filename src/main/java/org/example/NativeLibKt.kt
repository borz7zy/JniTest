package org.example

import java.io.File
import java.nio.file.Files

class NativeLibKt {

    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val lib = NativeLibKt()
            println(lib.stringFromNative())
        }

        init {
            val libName = when {
                System.getProperty("os.name").startsWith("Windows") -> "jnitest.dll"
                System.getProperty("os.name").startsWith("Mac") -> "jnitest.dylib"
                else -> "jnitest.so"
            }

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