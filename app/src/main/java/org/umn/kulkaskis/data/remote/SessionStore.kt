package org.umn.kulkaskis.data.remote

import android.content.Context

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("kulkaskis_session", Context.MODE_PRIVATE)

    fun save(session: AuthSession) {
        prefs.edit()
            .putString("access", session.accessToken)
            .putString("refresh", session.refreshToken)
            .putString("user_id", session.userId)
            .putString("email", session.email)
            .apply()
    }

    fun load(): AuthSession? {
        val access = prefs.getString("access", null) ?: return null
        return AuthSession(
            accessToken = access,
            refreshToken = prefs.getString("refresh", "").orEmpty(),
            userId = prefs.getString("user_id", "").orEmpty(),
            email = prefs.getString("email", "").orEmpty()
        )
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}