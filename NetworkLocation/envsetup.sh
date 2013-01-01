#!/bin/bash
##
# This is an environment setup file for the μg Build System
##

# General package info
PKG_NAME=NetworkLocation
TYPE=apk

# Flags for different packages that may be used [apk only]
USE_JGAPI=false
USE_MAPS=true
USE_SUPPORT=false

# Add or use packages not from those above [apk only]
EXTRA_INCLUDES=""
EXTRA_USES="com.android.location.provider.jar:android.location.jar"

# Additional compile directory [api only]
EXTRA_COMPILE=""

# Script file to be called
BEFORE_BUILD_SCRIPT=""
AFTER_BUILD_SCRIPT=""
