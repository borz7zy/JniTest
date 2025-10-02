#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_org_example_NativeLibKt_stringFromNative(JNIEnv* env, jobject){
    return env->NewStringUTF("Hello from JNI + CMake!");
}