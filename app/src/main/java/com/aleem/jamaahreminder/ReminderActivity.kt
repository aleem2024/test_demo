package com.aleem.jamaahreminder

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ReminderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        val title = intent.getStringExtra("title") ?: "Jamaah Reminder"
        val body = intent.getStringExtra("body") ?: ""
        val extra = intent.getStringExtra("extra") ?: ""

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 64, 48, 64)
            setBackgroundColor(Color.rgb(176, 0, 0))
        }

        val titleView = TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 40f
            gravity = Gravity.CENTER
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val bodyView = TextView(this).apply {
            text = body
            setTextColor(Color.WHITE)
            textSize = 24f
            gravity = Gravity.CENTER
            setPadding(0, 32, 0, 24)
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

        val extraView = TextView(this).apply {
            text = extra
            setTextColor(Color.WHITE)
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 30)
        }

        val dismiss = Button(this).apply {
            text = "DISMISS"
            textSize = 18f
            setOnClickListener { finish() }
        }

        root.addView(titleView)
        root.addView(bodyView)
        if (extra.isNotBlank()) root.addView(extraView)
        root.addView(dismiss)

        val scroll = ScrollView(this)
        scroll.addView(root, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))

        setContentView(scroll)
    }
}
