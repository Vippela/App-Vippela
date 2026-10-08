package br.com.vippela

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import br.com.vippela.data.linking.model.LinkedApp
import br.com.vippela.data.usage.UsageCollector
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class UsageCollectorTest {
    @Test fun consentPrecedesCollectionAndRepeatedPollsDoNotDoubleCount() {
        val context = RuntimeEnvironment.getApplication()
        val ops = shadowOf(context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
        fun permission(mode: Int) = ops.setMode(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName, mode)
        val manager = shadowOf(context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager)
        val apps = listOf(LinkedApp("com.example.video", "Vídeo"))
        val collector = UsageCollector(context, "test-child")
        val start = java.time.Instant.parse("2026-10-01T12:00:00Z").toEpochMilli()
        permission(AppOpsManager.MODE_IGNORED)
        assertFalse(collector.collect(apps, start).permission)
        permission(AppOpsManager.MODE_ALLOWED)
        assertTrue(collector.collect(apps, start).buckets.isEmpty())
        manager.addEvent(apps[0].packageName, start + 1000, UsageEvents.Event.MOVE_TO_FOREGROUND)
        assertEquals(9000L, collector.collect(apps, start + 10000).buckets.values.sum())
        assertEquals(19000L, collector.collect(apps, start + 20000).buckets.values.sum())
        manager.addEvent(apps[0].packageName, start + 31000, UsageEvents.Event.MOVE_TO_BACKGROUND)
        assertEquals(30000L, collector.collect(apps, start + 40000).buckets.values.sum())
        assertEquals(30000L, UsageCollector(context, "test-child").collect(apps, start + 50000).buckets.values.sum())
        assertTrue(UsageCollector(context, "different-child").collect(apps, start + 50000).buckets.isEmpty())
        permission(AppOpsManager.MODE_IGNORED)
        assertTrue(collector.collect(apps, start + 60000).buckets.isEmpty())
        permission(AppOpsManager.MODE_ALLOWED)
        assertTrue(collector.collect(apps, start + 70000).buckets.isEmpty())
    }
}
