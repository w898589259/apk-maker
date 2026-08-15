#!/usr/bin/env bash
# =============================================================================
#  SecondScreenMVP — 一键构建脚本（macOS / Linux / Windows Git Bash）
#  前提：本机已安装 JDK 17+ 并配置好 Android SDK（设置 ANDROID_HOME）
#  用法：在终端进入本目录，执行  bash build_release.sh
#  产物：app/build/outputs/apk/debug/app-debug.apk
# =============================================================================
set -e
OS="$(uname -s)"
echo "==> 系统: $OS"

# --- 写入腾讯云 Maven 镜像 init 脚本（Gradle 从腾讯云拉取 AndroidX 依赖）---
INIT_DIR="$(pwd)/init.d"
mkdir -p "${INIT_DIR}"
cat > "${INIT_DIR}/tencent-mirror.init.gradle" <<'GRADLE'
allprojects {
    repositories {
        maven { url "https://mirrors.cloud.tencent.com/nexus/repository/maven-public/" }
        google()
        mavenCentral()
    }
}
GRADLE
echo "==> 已配置腾讯云 Maven 镜像 init 脚本"

# --- 检查环境 ---
if [ -z "${ANDROID_HOME}" ] && [ -z "${ANDROID_SDK_ROOT}" ]; then
  echo "⚠️  未检测到 ANDROID_HOME / ANDROID_SDK_ROOT"
  echo "    请先安装 Android SDK 并 export ANDROID_HOME=/你的/sdk/路径"
  echo "    需要：platforms;android-33 + build-tools;33.0.2 + platform-tools"
  exit 1
fi
echo "==> ANDROID_HOME=${ANDROID_HOME:-${ANDROID_SDK_ROOT}}"

# --- 用项目自带 gradlew 构建（wrapper 已从腾讯云镜像下 Gradle 7.6.4）---
echo "==> 开始构建 app-debug.apk ..."
chmod +x gradlew
./gradlew --init-script "${INIT_DIR}/tencent-mirror.init.gradle" assembleDebug

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "${APK}" ]; then
  echo ""
  echo "✅ 构建成功！APK 位置："
  echo "   $(pwd)/${APK}"
  echo "   文件大小：$(du -h "${APK}" | cut -f1)"
else
  echo "❌ 未找到 APK，构建可能失败，请检查上方日志"
  exit 1
fi
