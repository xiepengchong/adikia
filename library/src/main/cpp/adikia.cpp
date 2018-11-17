#include <jni.h>
#include <string.h>
#include "log.h"
#include <time.h>
#include <stdlib.h>

static const char* ADIKIA_INIT_JAVA = "com/adikia/library/AdikiaEntry";

static struct {
    jmethodID m1;
    jmethodID m2;
    size_t methodSize;
} MethodStruct;


static long hook(JNIEnv *env, jclass type, jobject srcMethodObj, jobject destMethodObj, jobject backupMethodObj) {
    void* srcMethod = reinterpret_cast<void*>(env -> FromReflectedMethod(srcMethodObj));
    void* destMethod = reinterpret_cast<void*>(env -> FromReflectedMethod(destMethodObj));
    void* backupMethod = reinterpret_cast<void*>(env -> FromReflectedMethod(backupMethodObj));
    memcpy(backupMethod, srcMethod, MethodStruct.methodSize);
    memcpy(srcMethod, destMethod, MethodStruct.methodSize);
    return reinterpret_cast<long>(backupMethod);
}


static jobject restore(JNIEnv *env, jclass type, jobject srcMethod, jlong methodPtr) {
    int* backupMethod = reinterpret_cast<int*>(methodPtr);
    void* artMethodSrc = reinterpret_cast<void*>(env -> FromReflectedMethod(srcMethod));
    memcpy(artMethodSrc, backupMethod, MethodStruct.methodSize);
    return srcMethod;
}


static JNINativeMethod gMethods[] = {
        {
                "hook",
                "(Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)J",
                (void*)hook
        },
        {
                "restore",
                "(Ljava/lang/reflect/Method;J)Ljava/lang/reflect/Method;",
                (void*)restore
        }
};

extern "C"
JNIEXPORT jint JNICALL
JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_FALSE;
    }
    jclass classEvaluateUtil = env->FindClass(ADIKIA_INIT_JAVA);
    if(env -> RegisterNatives(classEvaluateUtil, gMethods, sizeof(gMethods)/ sizeof(gMethods[0])) < 0) {
        return JNI_FALSE;
    }
    MethodStruct.m1 = env -> GetStaticMethodID(classEvaluateUtil, "m1", "()V");
    MethodStruct.m2 = env -> GetStaticMethodID(classEvaluateUtil, "m2", "()V");
    MethodStruct.methodSize = reinterpret_cast<size_t>(MethodStruct.m2) - reinterpret_cast<size_t>(MethodStruct.m1);
    return JNI_VERSION_1_4;
}
