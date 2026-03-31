#!/usr/bin/env bash
# Source before Gradle / sdkmanager:  source scripts/android_env.sh
# Sets JAVA_HOME, ANDROID_HOME, and PATH for this repo's bundled JDK + SDK.
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
export JAVA_HOME="$ROOT/jdk-17.0.10+7"
export ANDROID_HOME="$ROOT/android-sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$PATH"
