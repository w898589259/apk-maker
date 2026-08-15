package com.example.secondscreen

import android.app.Activity
import android.app.Presentation
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

/**
 * 在外接显示器（便携屏）上以独立窗口形式展示的"播放器"界面。
 * 通过 Presentation API 把内容渲染到外接 Display，实现"便携屏全屏、手机独立操作"。
 * 若系统已将该触摸输入设备关联到外接 Display（Android 10+ port-association），
 * 副屏上的按钮触摸会直接投递到本 Presentation 窗口。
 */
class PlayerPresentation(context: Context, display: Display) : Presentation(context, display) {

    private var playing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.presentation_player)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnPlayPause = findViewById<Button>(R.id.btnPlayPause)
        val btnClose = findViewById<Button>(R.id.btnClose)

        btnPlayPause.setOnClickListener {
            playing = !playing
            tvStatus.text = if (playing) "▶ 正在播放（副屏全屏）" else "⏸ 已暂停"
            btnPlayPause.text = if (playing) "暂停" else "播放"
        }
        btnClose.setOnClickListener {
            // 关闭副屏投放，回到主屏
            (context as? Activity)?.finish()
            dismiss()
        }
        tvStatus.text = "▶ 正在播放（副屏全屏）"
    }
}

/**
 * 承载 Presentation 的 Activity：在手机主屏上只是一个"控制器"壳，
 * 真正画面通过 PlayerPresentation 输出到外接显示器。
 */
class ExternalPlayerActivity : Activity() {

    private var presentation: PlayerPresentation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 主屏端：仅一个极简控制器界面（实际投屏画面在副屏 Presentation）
        setContentView(R.layout.activity_external_launcher)

        findViewById<Button>(R.id.btnStopCast).setOnClickListener { finish() }

        val displayId = intent.getIntExtra("displayId", -1)
        if (displayId == -1) {
            Toast.makeText(this, "未指定外接显示器", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val display = getDisplayById(displayId)
        if (display == null) {
            Toast.makeText(this, "找不到外接显示器 ID=$displayId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        presentation = PlayerPresentation(this, display).also { it.show() }
    }

    private fun getDisplayById(id: Int): Display? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            display?.display ?: null
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.takeIf { it.displayId == id }
        }
    }

    override fun onDestroy() {
        presentation?.dismiss()
        presentation = null
        super.onDestroy()
    }
}
