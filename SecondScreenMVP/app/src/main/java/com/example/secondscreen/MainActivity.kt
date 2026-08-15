package com.example.secondscreen

import android.app.Activity
import android.app.PictureInPictureParams
import android.app.Presentation
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Rational
import android.view.Display
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var touchText: TextView
    private lateinit var btnCast: Button
    private lateinit var btnPip: Button

    private var externalDisplay: Display? = null
    private var currentPresentation: Presentation? = null

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) { refreshDisplays() }
        override fun onDisplayRemoved(displayId: Int) { refreshDisplays() }
        override fun onDisplayChanged(displayId: Int) { refreshDisplays() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        touchText = findViewById(R.id.touchText)
        btnCast = findViewById(R.id.btnCast)
        btnPip = findViewById(R.id.btnPip)

        btnCast.setOnClickListener { castToExternal() }
        btnPip.setOnClickListener { enterPip() }

        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        dm.registerDisplayListener(displayListener, null)
        refreshDisplays()
    }

    override fun onDestroy() {
        super.onDestroy()
        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        dm.unregisterDisplayListener(displayListener)
        currentPresentation?.dismiss()
    }

    private fun refreshDisplays() {
        val dm = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val displays = dm.displays
        val ext = displays.firstOrNull { it.displayId != Display.DEFAULT_DISPLAY }
        externalDisplay = ext

        if (ext == null) {
            statusText.text = "未检测到外接显示器（请用 Type-C 一线连便携屏）"
            touchText.text = "触摸能力：—"
            btnCast.isEnabled = false
            return
        }
        val metrics = DisplayMetrics()
        ext.getRealMetrics(metrics)
        statusText.text = "已检测到外接屏：ID=${ext.displayId}  ${metrics.widthPixels}x${metrics.heightPixels}"
        btnCast.isEnabled = true

        // 触摸能力三档检测
        val cap = TouchDetector.detect(this, ext.displayId)
        touchText.text = when (cap) {
            TouchDetector.Capability.NATIVE -> "触摸能力：NATIVE（副屏触摸可直接使用）"
            TouchDetector.Capability.INJECTABLE -> "触摸能力：INJECTABLE（需 Shizuku 注入关联）"
            TouchDetector.Capability.NONE -> "触摸能力：NONE（仅显示，手机端控制）"
        }
    }

    private fun castToExternal() {
        val disp = externalDisplay ?: return
        currentPresentation?.dismiss()
        val pres = object : Presentation(this, disp) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.presentation_player)
                window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
        try {
            pres.show()
            currentPresentation = pres
            Toast.makeText(this, "已向外接屏投放全屏窗口", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            Toast.makeText(this, "投放失败：${t.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } catch (t: Throwable) {
                Toast.makeText(this, "进入画中画失败：${t.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "系统版本低于 Android 8.0，不支持 PiP", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        btnCast.isEnabled = !isInPictureInPictureMode && externalDisplay != null
    }
}
