#!/bin/bash
##
# This is an environment setup file for the μg Build System
##

# General package info
PKG_NAME=NetworkLocation
TYPE=apk

# Flags for different packages that may be used [apk only]
USE_JGAPI=false
USE_MAPS=false
USE_SUPPORT=false

# Add or use packages not from those above [apk only]
EXTRA_INCLUDES="protobuf-micro.jar"
EXTRA_USES="protobuf-micro.jar:com.android.location.provider.jar:android.location.jar"

# Additional compile directory [api only]
EXTRA_COMPILE=""

# Script file to be called
BEFORE_BUILD_SCRIPT="prebuild.sh"
AFTER_BUILD_SCRIPT=""
