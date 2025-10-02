package org.example

import java.io.File

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

            val nativeDir = File(System.getProperty("user.dir"), "build/run/native")
            val libFile = File(nativeDir, libName)
            if (!libFile.exists()) {
                throw RuntimeException("Cannot find native library $libName in $nativeDir")
            }
            System.load(libFile.absolutePath)
        }
    }
    external fun stringFromNative(): String
}