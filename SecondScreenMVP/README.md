# SecondScreenMVP —— 一线连·扩展播放 MVP

把"扩展播放/画中画"与"一线连触摸便携屏"打通的通用视频扩展播放工具雏形。

## 功能
- 自动检测 Type-C 一线连外接显示器（`DisplayManager`）
- 外接屏触摸能力**三档检测**（NATIVE / INJECTABLE / NONE，基于 `dumpsys input` 解析）
- 通过 `Presentation` 向外接屏投放**独立全屏窗口**（副屏触摸可直达）
- 主屏进入画中画（PiP），实现"便携屏全屏 + 手机独立操作"
- Shizuku/root 端口关联注入接口（第二档触摸能力用）

## 快速出包

### 方式 A：GitHub Actions 自动构建（★推荐，完全不用装任何软件）
适合"我只想要一个能装的 APK"——全程在网页上点几下，几分钟出包。
1. **注册/登录 GitHub**（免费）：https://github.com/signup
2. **新建仓库**：点右上角 "+" → New repository → 名字随便填（如 `secondscreen-apk`）→ 选 **Public** → Create repository
3. **上传本项目的所有文件**：在新建的仓库页点 "uploading an existing file" → 把本目录（`SecondScreenMVP/`）下**全部文件和文件夹**拖进去 → 点 "Commit changes"
4. **一键出包**：点仓库顶部 **Actions** 标签 → 选左侧 **Build Debug APK** → 点右侧 **Run workflow** → 再点绿色的 **Run workflow** 按钮
5. **下载 APK**：等约 3-5 分钟状态变绿 ✅ → 点进该次运行 → 页面底部 **Artifacts** 区下载 `app-debug-apk`（解压即得 `app-debug.apk`）
6. **安装到手机**：把 `app-debug.apk` 传到手机（微信/数据线均可），在手机文件管理器里点击安装即可（需开启"允许安装未知来源应用"）

> 提示：GitHub Actions 的 runner 网络可正常访问 Gradle/Android 官方源，因此能稳定编译出包。

### 方式 A2：本机一键脚本（需电脑装了 JDK 17 + Android SDK）
若你本机已配好 Android 开发环境（设置好 `ANDROID_HOME`，并安装 platforms;android-33 + build-tools;33.0.2），执行：
```
bash build_release.sh
```
脚本会调用项目自带 gradlew（已从腾讯云镜像下载 Gradle 7.6.4）、配置腾讯云 Maven 镜像拉取 AndroidX 依赖，最终产出 `app/build/outputs/apk/debug/app-debug.apk`。

### 方式 B：本地 Android Studio / 命令行构建
- 要求：JDK 17、Android SDK（compileSdk 33 / build-tools 33.0.2 / platform-tools）
- 设置 `ANDROID_HOME` 后，命令行执行：
  ```
  ./build_local.sh
  ```
  成功后在项目根目录得到 `SecondScreenMVP-debug.apk`
- 或在 Android Studio Hedgehog(2023.1.1)+ 中打开本项目，菜单 **Build → Build APK(s)**

## 使用流程
1. 手机开启开发者选项 → 无线调试，在 App 内授权 Shizuku（如需第二档触摸注入）
2. 打开 App → 点「向外接屏全屏投放」→ 选择视频源（或把正在播放的视频切到画中画）
3. 用 Type-C 线一线连便携屏，App 通过 DisplayManager 自动识别外接显示器
4. 视频全屏输出到便携屏，手机端可独立操作

## 技术栈
Kotlin + Jetpack（AppCompat / Material / ConstraintLayout）+ Presentation API + PictureInPicture + Shizuku 端口关联。
Gradle 7.6.4 / AGP 7.4.2 / compileSdk 33 / minSdk 26。

## 说明
当前为 MVP 演示占位播放器（副屏显示可交互的播放控制 UI，验证投放与触摸链路）。
拿到可运行 APK 并在"手机 + 触摸便携屏"实机验证后，把 `PlayerPresentation` 里的占位 UI
替换为 `VideoView` / ExoPlayer `PlayerView`，即可升级为完整视频投屏工具。
