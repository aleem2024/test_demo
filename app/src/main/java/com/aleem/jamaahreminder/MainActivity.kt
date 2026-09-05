package com.aleem.jamaahreminder

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "Jamaah Reminder\nSeptember 2026"
            textSize = 28f
        }

        val info = TextView(this).apply {
            text = "All 5 daily prayers are scheduled from the September timetable. Friday Dhuhr is replaced with the 4-mosque / 10-Jamaat Jumu'ah reminders."
            textSize = 18f
        }

        val button = Button(this).apply {
            text = "ENABLE / REFRESH REMINDERS"
            setOnClickListener {
                requestNeededPermissions()
                ReminderScheduler.scheduleAll(this@MainActivity)
            }
        }

        layout.addView(title)
        layout.addView(info)
        layout.addView(button)
        setContentView(layout)

        requestNeededPermissions()
        ReminderScheduler.scheduleAll(this)
    }

    private fun requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        }

        if (Build.VERSION.SDK_INT >= 31) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                try {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                } catch (_: Exception) {}
            }
        }
    }
}
