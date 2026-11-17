package org.umn.kulkaskis.data.remote

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.umn.kulkaskis.data.FridgeDatabase
import org.umn.kulkaskis.data.sampleItems

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SessionStore(application)
    private val auth = SupabaseAuth()
    private val prefs = application.getSharedPreferences("fridgecheck_prefs", Context.MODE_PRIVATE)

    private val _session = MutableStateFlow(store.load())
    val session: StateFlow<AuthSession?> = _session.asStateFlow()

    private val _displayName = MutableStateFlow(nameFor(_session.value))
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /** Nama milik akun ini: nama yang disimpan, "Guest" untuk tamu, atau bagian depan email. */
    private fun nameFor(session: AuthSession?): String {
        if (session == null) return ""
        val saved = prefs.getString("user_name_" + session.userId, null)
        if (!saved.isNullOrBlank()) return saved
        // Nama yang diisi saat Register (sebelum email dikonfirmasi) dipindahkan ke akun ini
        val pendingKey = "pending_name_" + session.email.lowercase()
        val pending = prefs.getString(pendingKey, null)
        if (!pending.isNullOrBlank()) {
            prefs.edit().putString("user_name_" + session.userId, pending).remove(pendingKey).apply()
            return pending
        }
        if (session.userId == GUEST_ID) return "Guest"
        return session.email.substringBefore("@").replaceFirstChar { it.uppercase() }
    }

    fun setDisplayName(name: String) {
        val clean = name.trim()
        val current = _session.value ?: return
        if (clean.isNotEmpty()) {
            prefs.edit().putString("user_name_" + current.userId, clean).apply()
            _displayName.value = clean
        }
    }

    fun signIn(email: String, password: String) = authenticate(email, password, register = false)

    fun signUp(email: String, password: String, fullName: String = "") {
        val clean = fullName.trim()
        if (clean.isNotEmpty()) {
            prefs.edit().putString("pending_name_" + email.trim().lowercase(), clean).apply()
        }
        authenticate(email, password, register = true)
    }

    /**
     * Mode demo: tanpa server. Mengisi database lokal dengan data contoh.
     * Item ditandai sudah tersinkron (dirty = false) dan SyncManager melewati sesi tamu,
     * jadi tidak ada yang dikirim ke Supabase.
     */
    fun continueAsGuest() {
        viewModelScope.launch {
            val app = getApplication<Application>()
            FridgeDatabase.getInstance(app).fridgeDao()
                .insertAll(sampleItems.map { it.copy(dirty = false) })
            val guest = AuthSession(
                accessToken = "guest",
                refreshToken = "",
                userId = GUEST_ID,
                email = "guest@kulkaskis.demo"
            )
            store.save(guest)
            _session.value = guest
            _displayName.value = nameFor(guest)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val wasGuest = _session.value?.userId == GUEST_ID
            if (!wasGuest) {
                // Usahakan kirim perubahan yang tertunda sebelum data lokal dihapus
                SyncManager(app).sync()
            } else {
                prefs.edit().remove("user_name_" + GUEST_ID).apply()
            }
            SyncScheduler.cancel(app)
            FridgeDatabase.getInstance(app).fridgeDao().clearAll()
            store.clear()
            _session.value = null
            _displayName.value = ""
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun authenticate(email: String, password: String, register: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            _message.value = null
            val result = if (register) auth.signUp(email, password) else auth.signIn(email, password)
            when (result) {
                is AuthResult.Success -> {
                    store.save(result.session)
                    _session.value = result.session
                    _displayName.value = nameFor(result.session)
                    SyncScheduler.request(getApplication<Application>())
                }
                AuthResult.NeedsConfirmation ->
                    _message.value = "Account created. Check your email to confirm it, then log in."
                is AuthResult.Error -> _message.value = result.message
            }
            _loading.value = false
        }
    }

    companion object {
        const val GUEST_ID = "guest"
    }
}