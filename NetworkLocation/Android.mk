LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := com.android.location.provider

LOCAL_PACKAGE_NAME := NetworkLocation
LOCAL_CERTIFICATE := platform
LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)
