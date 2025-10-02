package org.example

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

            val inputStream = NativeLibKt::class.java.classLoader
                .getResourceAsStream("native/$libName")
                ?: throw RuntimeException("Cannot find native library $libName in JAR")

            val tempFile = createTempFile(libName, null)
            inputStream.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }

            System.load(tempFile.absolutePath)
        }
    }
    external fun stringFromNative(): String
}