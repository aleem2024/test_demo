package com.aleem.jamaahreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.*

object ReminderScheduler {
    private val zone: ZoneId = ZoneId.of("Europe/London")

    private val daily = mapOf(
        1 to listOf("04:15","13:30","19:00","21:02","22:15"),
        2 to listOf("04:15","13:30","19:00","21:00","22:15"),
        3 to listOf("04:15","13:30","19:00","20:59","22:15"),
        4 to listOf("04:15","13:30","19:00","20:57","22:15"),
        5 to listOf("04:15","13:30","19:00","20:55","22:15"),
        6 to listOf("04:15","13:30","19:00","20:53","22:15"),
        7 to listOf("04:30","13:30","19:00","20:51","22:00"),
        8 to listOf("04:30","13:30","19:00","20:50","22:00"),
        9 to listOf("04:30","13:30","19:00","20:48","22:00"),
        10 to listOf("04:30","13:30","19:00","20:46","22:00"),
        11 to listOf("04:30","13:30","19:00","20:44","22:00"),
        12 to listOf("04:30","13:30","19:00","20:42","22:00"),
        13 to listOf("04:30","13:30","19:00","20:40","22:00"),
        14 to listOf("04:45","13:30","19:00","20:38","21:50"),
        15 to listOf("04:45","13:30","19:00","20:36","21:50"),
        16 to listOf("04:45","13:30","19:00","20:34","21:50"),
        17 to listOf("04:45","13:30","19:00","20:32","21:50"),
        18 to listOf("04:45","13:30","19:00","20:29","21:50"),
        19 to listOf("04:45","13:30","19:00","20:27","21:50"),
        20 to listOf("04:45","13:30","19:00","20:25","21:50"),
        21 to listOf("05:00","13:30","18:30","20:23","21:40"),
        22 to listOf("05:00","13:30","18:30","20:21","21:40"),
        23 to listOf("05:00","13:30","18:30","20:19","21:40"),
        24 to listOf("05:00","13:30","18:30","20:16","21:30"),
        25 to listOf("05:00","13:30","18:30","20:14","21:40"),
        26 to listOf("05:00","13:30","18:30","20:12","21:40"),
        27 to listOf("05:00","13:30","18:30","20:10","21:40"),
        28 to listOf("05:15","13:30","18:30","20:08","21:30"),
        29 to listOf("05:15","13:30","18:30","20:05","21:30"),
        30 to listOf("05:15","13:30","18:30","20:03","21:30")
    )

    private val prayerNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")

    data class Jumuah(val mosque: String, val jamaat: String, val time: String)

    private val jumuahs = listOf(
        Jumuah("Masjid Esa ibn Maryam", "Jamaat 1", "13:30"),
        Jumuah("Masjid Esa ibn Maryam", "Jamaat 2", "14:45"),
        Jumuah("Masjid Sulayman bin Dawud", "Jamaat 1", "13:30"),
        Jumuah("Masjid Sulayman bin Dawud", "Jamaat 2", "14:30"),
        Jumuah("Masjid Sulayman bin Dawud", "Jamaat 3", "15:30"),
        Jumuah("Hall Green Masjid - Wycombe Rd", "Jamaat 1", "13:30"),
        Jumuah("Hall Green Masjid - Wycombe Rd", "Jamaat 2", "14:15"),
        Jumuah("Ad-Duha Masjid", "Jamaat 1", "13:30"),
        Jumuah("Ad-Duha Masjid", "Jamaat 2", "14:30"),
        Jumuah("Ad-Duha Masjid", "Jamaat 3", "15:40")
    )

    fun scheduleAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = ZonedDateTime.now(zone)

        for (day in 1..30) {
            val date = LocalDate.of(2026, 9, day)
            val isFriday = date.dayOfWeek == DayOfWeek.FRIDAY
            val row = daily[day] ?: continue

            row.forEachIndexed { index, hhmm ->
                val prayer = prayerNames[index]
                if (isFriday && prayer == "Dhuhr") return@forEachIndexed

                val event = at(date, hhmm)
                listOf(15, 10, 5, 0).forEach { before ->
                    val alarmAt = event.minusMinutes(before.toLong())
                    if (alarmAt.isAfter(now)) {
                        val title = when (before) {
                            15 -> "PACK UP"
                            10 -> "LEAVE THE LAPTOP"
                            5 -> "LEAVE NOW"
                            else -> "JAMA'AH NOW"
                        }
                        val body = "$prayer • Jama'ah ${fmt(event)}"
                        schedule(am, context, requestCode(date, prayer, before), alarmAt, title, body, "")
                    }
                }
            }

            if (isFriday) {
                val groups = jumuahs.groupBy { it.time }.toSortedMap()
                val times = groups.keys.toList()
                times.forEachIndexed { idx, time ->
                    val event = at(date, time)
                    val alarmAt = event.minusMinutes(20)
                    if (alarmAt.isAfter(now)) {
                        val current = groups[time].orEmpty()
                        val currentText = current.joinToString("\n") { "• ${it.mosque} — ${it.jamaat} — ${fmt(event)}" }
                        val nextText = times.drop(idx + 1).take(3).joinToString("\n") { nextTime ->
                            val e = at(date, nextTime)
                            val mosques = groups[nextTime].orEmpty().joinToString(", ") { "${it.mosque} (${it.jamaat})" }
                            "• ${fmt(e)} — $mosques"
                        }
                        val detail = if (current.size > 1) {
                            "If you leave now, you can choose any of these mosques for the ${fmt(event)} Jamaat:"
                        } else {
                            "This Jamaat is at ${fmt(event)}."
                        }
                        schedule(am, context, 900000 + day * 100 + idx, alarmAt, "JUMU'AH — 20 MINUTES", currentText, detail + if (nextText.isNotBlank()) "\n\nNEXT AVAILABLE JAMAATS:\n$nextText" else "")
                    }
                }
            }
        }
    }

    private fun schedule(am: AlarmManager, context: Context, code: Int, whenAt: ZonedDateTime, title: String, body: String, extra: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
            putExtra("extra", extra)
            putExtra("code", code)
        }
        val pi = PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenAt.toInstant().toEpochMilli(), pi)
        } catch (_: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, whenAt.toInstant().toEpochMilli(), pi)
        }
    }

    private fun at(date: LocalDate, hhmm: String): ZonedDateTime {
        val parts = hhmm.split(":")
        return ZonedDateTime.of(date, LocalTime.of(parts[0].toInt(), parts[1].toInt()), zone)
    }

    private fun fmt(z: ZonedDateTime): String = z.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"))

    private fun requestCode(date: LocalDate, prayer: String, before: Int): Int =
        (date.dayOfMonth * 10000) + (prayer.hashCode().ushr(16) and 0x0fff) + before
}
