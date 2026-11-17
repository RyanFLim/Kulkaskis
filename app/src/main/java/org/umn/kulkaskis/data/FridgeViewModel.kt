package org.umn.kulkaskis.data

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.umn.kulkaskis.data.remote.SyncScheduler

class FridgeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = FridgeDatabase.getInstance(application).fridgeDao()
    private val prefs = application.getSharedPreferences("kulkaskis_prefs", Context.MODE_PRIVATE)

    val items: StateFlow<List<FridgeItem>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _userName = MutableStateFlow(prefs.getString("user_name", "Ryan") ?: "Ryan")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun setUserName(name: String) {
        val clean = name.trim()
        if (clean.isNotEmpty()) {
            prefs.edit().putString("user_name", clean).apply()
            _userName.value = clean
        }
    }

    fun addItem(item: FridgeItem) = save(item)

    fun updateItem(updated: FridgeItem) = save(updated)

    fun deleteItem(id: String) {
        viewModelScope.launch {
            dao.markDeleted(id, System.currentTimeMillis())
            SyncScheduler.request(getApplication<Application>())
        }
    }

    private fun save(item: FridgeItem) {
        viewModelScope.launch {
            dao.insert(
                item.copy(
                    updatedAt = System.currentTimeMillis(),
                    dirty = true,
                    deleted = false
                )
            )
            SyncScheduler.request(getApplication<Application>())
        }
    }
}