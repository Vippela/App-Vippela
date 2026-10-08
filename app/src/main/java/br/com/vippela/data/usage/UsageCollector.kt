package br.com.vippela.data.usage

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Process
import android.util.Base64
import br.com.vippela.data.linking.model.LinkedApp
import br.com.vippela.data.linking.model.UsageReport
import com.google.gson.Gson
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZoneId

internal fun addUsage(buckets: MutableMap<String, Long>, pkg: String, start: Long, end: Long, zone: ZoneId) {
    var cursor = start
    while (cursor < end) {
        val local = Instant.ofEpochMilli(cursor).atZone(zone)
        val next = minOf(end, local.withMinute(0).withSecond(0).withNano(0).plusHours(1).toInstant().toEpochMilli())
        val key = "${local.toLocalDate()}|${local.hour}|$pkg"
        buckets[key] = (buckets[key] ?: 0) + next - cursor
        cursor = next
    }
}

class UsageCollector(private val context: Context, private val account: String) {
    companion object {
        private val lock = Any()
        fun permitted(context: Context): Boolean =
            (context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
                .checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
    }
    private val prefs = context.getSharedPreferences("usage_$account", Context.MODE_PRIVATE)
    private val gson = Gson()
    private class History {
        var since = 0L
        var cursor = 0L
        var active: String? = null
        var started = 0L
        var zone = ""
        var buckets: MutableMap<String, Long> = mutableMapOf()
    }
    fun collect(apps: List<LinkedApp>, now: Long = System.currentTimeMillis()): UsageReport = synchronized(lock) {
        val zone = ZoneId.systemDefault()
        val icons = mutableMapOf<String, String>()
        var iconSize = 0
        apps.forEach { app ->
            runCatching {
                val drawable = context.packageManager.getApplicationIcon(app.packageName)
                val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
                drawable.setBounds(0, 0, 48, 48)
                drawable.draw(Canvas(bitmap))
                val out = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                bitmap.recycle()
                Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            }.getOrNull()?.takeIf { it.length <= 12000 && iconSize + it.length <= 1500000 }?.let {
                icons[app.packageName] = it
                iconSize += it.length
            }
        }
        if (!permitted(context)) {
            prefs.edit().clear().apply()
            return@synchronized UsageReport(false, now, now, zone.id, icons = icons)
        }
        var history = runCatching { gson.fromJson(prefs.getString("history", null), History::class.java) }.getOrNull() ?: History()
        if (history.since == 0L || history.zone != zone.id || history.cursor > now) {
            history = History().apply { since = now; cursor = now; this.zone = zone.id }
        }
        // Longas interrupções podem ultrapassar a retenção de eventos do Android.
        if (now - history.cursor > 86400000L) {
            history.active = null
            history.cursor = now
        }
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val events = if (now - history.cursor > 1) manager.queryEvents(history.cursor + 1, now) else null
        val eligible = apps.map { it.packageName }.toSet()
        val event = UsageEvents.Event()
        fun finish(at: Long) {
            history.active?.let { addUsage(history.buckets, it, history.started, at.coerceAtLeast(history.started), zone) }
            history.active = null
        }
        while (events != null && events.hasNextEvent()) {
            events.getNextEvent(event)
            when (event.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    if (history.active != event.packageName) {
                        finish(event.timeStamp)
                        if (event.packageName in eligible) {
                            history.active = event.packageName
                            history.started = event.timeStamp
                        }
                    }
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> if (history.active == event.packageName) finish(event.timeStamp)
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> finish(event.timeStamp)
                26 -> { history.active = null } // DEVICE_SHUTDOWN
            }
        }
        if (events != null) history.cursor = now
        val earliest = Instant.ofEpochMilli(now).atZone(zone).toLocalDate().minusDays(29).toString()
        history.buckets.keys.removeAll { it.substringBefore('|') < earliest }
        prefs.edit().putString("history", gson.toJson(history)).apply()
        val snapshot = history.buckets.toMutableMap()
        if (events != null) history.active?.let { addUsage(snapshot, it, history.started, now, zone) }
        snapshot.keys.removeAll { it.substringBefore('|') < earliest }
        val since = maxOf(history.since, Instant.ofEpochMilli(now).atZone(zone).toLocalDate().minusDays(29).atStartOfDay(zone).toInstant().toEpochMilli())
        UsageReport(true, now, since, zone.id, snapshot, icons)
    }
}
