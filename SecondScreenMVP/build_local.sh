#!/bin/bash
# 本地一键构建脚本（需已安装 JDK 17 + Android SDK，且设置 ANDROID_HOME）
set -e
: "${ANDROID_HOME:?请先设置 ANDROID_HOME 指向 Android SDK 目录}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "==> 使用 Gradle Wrapper 构建 debug APK ..."
./gradlew assembleDebug --no-daemon "$@"

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK" ]; then
  echo "==> 构建成功：$APK"
  cp "$APK" "SecondScreenMVP-debug.apk"
  echo "==> 已复制为 SecondScreenMVP-debug.apk"
else
  echo "==> 未找到 APK，构建可能失败"
  exit 1
fi
