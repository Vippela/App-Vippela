package br.com.vippela.data.linking

import br.com.vippela.blocking.AppBlockService
import br.com.vippela.data.linking.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    suspend fun list() = api.links(key)

    suspend fun status(id: String) = api.status(id, key)

    suspend fun rule(id: String, packageName: String, blocked: Boolean) =
        api.rule(id, key, RuleRequest(packageName, blocked))

    suspend fun sync(): DeviceLinkResponse? =
        withContext(Dispatchers.IO) {
            val cached = store.cached(scope) ?: return@withContext null
            val enabled = AppBlockService.connected && store.activeScope == scope
            val apps = store.installedApps()
            val result = api.sync(cached.id, key, SyncRequest(apps, cached.revision, enabled))
            // Só confirma uma revisão depois que ela está salva no aparelho.
            store.save(scope, result)
            store.cached(scope) ?: result
        }
}
