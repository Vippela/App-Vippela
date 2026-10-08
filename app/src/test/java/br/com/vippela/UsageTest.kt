package br.com.vippela

import br.com.vippela.data.usage.addUsage
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class UsageTest {
    @Test fun splitsForegroundSessionAcrossMidnightWithoutLosingTime() {
        val buckets = mutableMapOf<String, Long>()
        val start = Instant.parse("2026-10-01T23:58:00Z").toEpochMilli()
        addUsage(buckets, "com.example.app", start, start + 300000, ZoneId.of("UTC"))
        assertEquals(120000L, buckets["2026-10-01|23|com.example.app"])
        assertEquals(180000L, buckets["2026-10-02|0|com.example.app"])
        assertEquals(300000L, buckets.values.sum())
    }
    @Test fun repeatedHourOnDaylightSavingDoesNotLoseOrDuplicateDuration() {
        val buckets = mutableMapOf<String, Long>()
        val start = Instant.parse("2026-11-01T05:30:00Z").toEpochMilli()
        addUsage(buckets, "com.example.app", start, start + 7200000, ZoneId.of("America/New_York"))
        assertEquals(7200000L, buckets.values.sum())
        assertEquals(5400000L, buckets["2026-11-01|1|com.example.app"])
    }
    @Test fun backwardsClockCannotProduceNegativeUsage() {
        val buckets = mutableMapOf<String, Long>()
        addUsage(buckets, "com.example.app", 2000, 1000, ZoneId.of("UTC"))
        assertTrue(buckets.isEmpty())
    }
}
