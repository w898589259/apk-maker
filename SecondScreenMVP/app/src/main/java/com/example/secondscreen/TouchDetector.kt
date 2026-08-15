package com.example.secondscreen

import android.content.Context
import android.os.Build

/**
 * 外接屏触摸能力三档检测：
 *  NATIVE      —— 系统已把触摸输入设备关联到外接 Display（三星 DeX/定制 ROM）
 *  INJECTABLE  —— 识别到外置屏但触摸未关联，可尝试 Shizuku 注入 port-association
 *  NONE        —— 无触摸回传通道（如部分一线连方案/镜像场景）
 */
object TouchDetector {

    enum class Capability { NATIVE, INJECTABLE, NONE }

    fun detect(context: Context, externalDisplayId: Int): Capability {
        // 通过 dumpsys input 解析：查找关联到目标 displayId 的 touch 设备
        val dumpsys = runCatching { exec("dumpsys input") }.getOrDefault("")
        val hasTouchOnDisplay = dumpsys.contains("displayId=$externalDisplayId") &&
                (dumpsys.contains("TOUCH") || dumpsys.contains("touch"))

        if (hasTouchOnDisplay) return Capability.NATIVE

        // 未原生关联，但存在外置屏 → 可尝试注入（需 Shizuku/root）
        if (externalDisplayId != -1) return Capability.INJECTABLE
        return Capability.NONE
    }

    /** 尝试通过 Shizuku/root 执行命令建立 port-association（第二档用）。 */
    fun tryInjectPortAssociation(location: String, port: Int): Boolean {
        return runCatching {
            val code = exec("service call inputflinger 42 i32 $port s16 $location").trim()
            code.isNotEmpty()
        }.getOrDefault(false)
    }

    private fun exec(cmd: String): String =
        Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd)).run {
            use { it.inputStream.bufferedReader().readText() }
        }
}
