#include <jni.h>
#include <stdio.h>
#include <android/log.h>

jint Java_com_grumpycat_pcaplib_port_PortService_parseUidByPort(JNIEnv *env, jclass type_, jint port,
                                                           jint type) {
    jint uid = parseUid(port, "/proc/net/tcp");
    if(uid > 0){
        return uid;
    }

    uid = parseUid(port, "/proc/net/tcp6");

    if(uid > 0){
        return uid;
    }

    uid = parseUid(port, "/proc/net/udp");

    if(uid > 0){
        return uid;
    }

    uid = parseUid(port, "/proc/net/udp6");
    if(uid > 0) {
        return uid;
    }

    return 0;
}

jint parseUid(jint port, const char*fileName){
    FILE *fp = fopen(fileName, "r");
    if (fp == NULL) {
        return 0;
    }

    char buffer[512];
    jint curPort;
    jint uid;
    fgets(buffer, sizeof(buffer), fp);
    while (fgets(buffer, sizeof(buffer), fp) != NULL) {
        int matched = sscanf(buffer, "%*s%*X:%X%*s%*s%*s%*s%*s%u", &curPort, &uid);
        if(matched == 2 && curPort == port){
            return uid;
        }
    }

    fclose(fp);
    return 0;
}