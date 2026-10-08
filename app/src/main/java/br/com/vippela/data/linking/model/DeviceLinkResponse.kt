package br.com.vippela.data.linking.model

data class DeviceLinkResponse(
    val id: String,
    val memberKey: String,
    val memberName: String,
    val ownerName: String,
    val linkedDeviceId: String?,
    val status: String,
    val revision: Long,
    val appliedRevision: Long,
    val protectionEnabled: Boolean,
    val lastSeenAt: String?,
    val apps: List<LinkedApp>,
    val blockedPackages: Set<String>,
    val report: UsageReport? = null,
    val reportError: String? = null,
)

data class LinkedApp(val packageName: String, val label: String)

data class GenerateLinkRequest(
    val memberKey: String,
    val memberName: String,
    val ownerName: String,
)

data class RuleRequest(val packageName: String, val blocked: Boolean)

data class SyncRequest(
    val apps: List<LinkedApp>,
    val appliedRevision: Long,
    val protectionEnabled: Boolean,
)

data class UsageReport(
    val permission: Boolean = false,
    val collectedAt: Long = 0,
    val since: Long = 0,
    val zone: String = "UTC",
    val buckets: Map<String, Long> = emptyMap(),
    val icons: Map<String, String> = emptyMap(),
)
