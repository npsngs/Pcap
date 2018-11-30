LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := NativePortService
LOCAL_SRC_FILES := native_portservice.c

LOCAL_LDLIBS += -llog	#增加这行代码  -l<log库文件>
include $(BUILD_SHARED_LIBRARY)