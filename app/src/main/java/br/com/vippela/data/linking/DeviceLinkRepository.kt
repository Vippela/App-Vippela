package br.com.vippela.data.linking

import br.com.vippela.blocking.AppBlockService
import br.com.vippela.data.linking.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class DeviceLinkRepository(private val store: LinkStore, private val scope: String) {
    private val api = NetworkModule.api(store.server)
    private val key = store.key(scope)

    suspend fun generate(memberKey: String, memberName: String, ownerName: String) =
        api.generateLink(key, GenerateLinkRequest(memberKey, memberName, ownerName)).also {
            store.savePending(scope, it)
        }

    suspend fun confirm(code: String) =
        api.confirmLink(key, ConfirmLinkRequest(code, store.deviceId(scope))).also {
            store.save(scope, it)
        }

    private suspend fun withReport(link: DeviceLinkResponse): DeviceLinkResponse {
        val cacheKey = store.server + scope + link.id
        val cached = reports[cacheKey]
        val ttl = if (cached?.second?.permission == true) 60000 else 5000
        if (cached != null && System.currentTimeMillis() - cached.first < ttl) return link.copy(report = cached.second)
        return try {
            val report = api.report(link.id, key)
            if (reports.size > 100) reports.clear()
            reports[cacheKey] = System.currentTimeMillis() to report
            link.copy(report = report)
        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
        catch (e: Exception) { link.copy(report = cached?.second, reportError = reportFailure(e)) }
    }
    suspend fun list() = api.links(key).map { withReport(it) }

    companion object {
        private val reports = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, UsageReport>>()
        private val uploads = java.util.concurrent.ConcurrentHashMap<String, Pair<Long, Boolean>>()
        private val telemetryScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.IO)
        private val reportMutex = kotlinx.coroutines.sync.Mutex()
    }

    suspend fun status(id: String) = withReport(api.status(id, key))

    suspend fun rule(id: String, packageName: String, blocked: Boolean) =
        withReport(api.rule(id, key, RuleRequest(packageName, blocked)))

    private fun collectReport(cached: DeviceLinkResponse, apps: List<LinkedApp>) {
        val cacheKey = store.server + scope + cached.id
        val now = System.currentTimeMillis()
        val granted = br.com.vippela.data.usage.UsageCollector.permitted(store.context)
        val uploaded = uploads[cacheKey]
        if (uploaded != null && uploaded.second == granted && now - uploaded.first < 60000) return
        if (!reportMutex.tryLock()) return
        telemetryScope.launch {
            try {
                if (store.activeScope != scope) return@launch
                val report = br.com.vippela.data.usage.UsageCollector(store.context, scope).collect(apps)
                store.save(scope, (store.cached(scope) ?: cached).copy(report = report, reportError = "Relatório salvo neste aparelho. Aguardando envio ao responsável."))
                if (store.activeScope != scope) return@launch
                api.uploadReport(cached.id, key, report)
                uploads[cacheKey] = now to report.permission
                store.save(scope, (store.cached(scope) ?: cached).copy(reportError = null))
            } catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) {
                store.save(scope, (store.cached(scope) ?: cached).copy(reportError = reportFailure(e)))
            } finally { reportMutex.unlock() }
        }
    }

    suspend fun sync(): DeviceLinkResponse? = withContext(Dispatchers.IO) {
        val cached = store.cached(scope) ?: return@withContext null
        val enabled = AppBlockService.connected && store.activeScope == scope
        val apps = store.installedApps()
        try {
            val result = api.sync(cached.id, key, SyncRequest(apps, cached.revision, enabled))
            val latest = store.cached(scope) ?: cached
            // Confirma uma revisão apenas depois de salvá-la no aparelho.
            store.save(scope, result.copy(report = latest.report, reportError = latest.reportError))
        } finally { collectReport(cached, apps) }
        store.cached(scope)
    }
}

internal fun reportFailure(error: Exception): String = when ((error as? retrofit2.HttpException)?.code()) {
    404, 405 -> "O servidor ainda não oferece relatórios. Atualize e reinicie o backend."
    400, 413 -> "O servidor recusou o relatório. Confira se o backend está atualizado."
    401, 403 -> "O servidor não autorizou o relatório deste vínculo."
    else -> "Não foi possível sincronizar as estatísticas. Confira o servidor e a conexão; tentaremos novamente."
}
