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
static int access=3;


static long hook(JNIEnv *env, jclass type, jobject srcMethodObj, jobject destMethodObj,
                 jobject backupMethodObj) {
    void *mSrc = (void *) env->FromReflectedMethod(srcMethodObj);
    void *mNew_ = (void *) env->FromReflectedMethod(destMethodObj);
    size_t *mInvoker = (size_t *) env->FromReflectedMethod(backupMethodObj);
    memcpy(mInvoker, mSrc, MethodStruct.methodSize);
    *(mInvoker + access) = *(mInvoker + access) | 0x0002;
    memcpy(mSrc, mNew_, MethodStruct.methodSize);
    return (size_t) mInvoker;
}


static jobject restore(JNIEnv *env, jclass type, jobject srcMethod, jlong methodPtr) {
    int* backupMethod = reinterpret_cast<int*>(methodPtr);
    size_t* artMethodSrc = reinterpret_cast<size_t*>(env -> FromReflectedMethod(srcMethod));
    memcpy(artMethodSrc, backupMethod, MethodStruct.methodSize);
    *(artMethodSrc+access)=*(artMethodSrc+access)|0x0002;

    return srcMethod;
}

static void init(JNIEnv* env, jclass clazz, jclass cls,jstring name,jstring sig, jboolean isstatic) {
    jmethodID m;
    const char* name_=env->GetStringUTFChars(name,0);;
    const char* sig_=env->GetStringUTFChars(sig,0);
    if(isstatic) {
        m=env->GetStaticMethodID(cls, name_, sig_);
    } else{
        m=env->GetMethodID(cls, name_, sig_);
    }

    if(env->ExceptionCheck()){
        env->ExceptionClear();
    }
    return;
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
        },
        {
                "init",
                "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/String;Z)V",
                (void*)init
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
