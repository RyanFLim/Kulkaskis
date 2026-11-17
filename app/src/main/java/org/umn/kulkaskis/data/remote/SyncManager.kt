package org.umn.kulkaskis.data.remote

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.umn.kulkaskis.data.FridgeDatabase
import org.umn.kulkaskis.data.FridgeItem
import kotlin.coroutines.cancellation.CancellationException

class SyncManager(context: Context) {
    private val dao = FridgeDatabase.getInstance(context).fridgeDao()
    private val store = SessionStore(context)
    private val auth = SupabaseAuth()
    private val api = SupabaseDb()

    /** true = selesai (atau tidak ada yang perlu disinkronkan), false = gagal dan perlu dicoba lagi. */
    suspend fun sync(): Boolean = withContext(Dispatchers.IO) {
        val session = store.load() ?: return@withContext true
        if (session.userId == AuthViewModel.GUEST_ID) return@withContext true
        if (session.userId == "guest") return@withContext true
        try {
            try {
                syncWith(session.accessToken)
            } catch (e: UnauthorizedException) {
                val result = auth.refresh(session.refreshToken)
                if (result is AuthResult.Success) {
                    store.save(result.session)
                    syncWith(result.session.accessToken)
                } else {
                    return@withContext false
                }
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("SyncManager", "Sync failed", e)
            false
        }
    }

    private suspend fun syncWith(token: String) {
        // 1. Kirim perubahan lokal
        val dirty = dao.getDirty()
        api.upsert(token, dirty)
        dirty.forEach { dao.markClean(it.id, it.updatedAt) }

        // 2. Ambil data dari server, yang paling baru menang
        val remote = api.fetchAll(token)
        val toApply = mutableListOf<FridgeItem>()
        for (r in remote) {
            val local = dao.getById(r.id)
            if (local == null) {
                if (!r.deleted) toApply.add(r)
            } else if (!local.dirty && r.updatedAt > local.updatedAt) {
                toApply.add(r)
            }
        }
        dao.insertAll(toApply)

        // 3. Buang item yang sudah dihapus dan sudah tersinkron
        dao.purgeDeleted()
    }
}