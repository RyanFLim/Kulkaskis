package org.umn.kulkaskis.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FridgeDao {
    @Query("SELECT * FROM fridge_items WHERE deleted = 0 ORDER BY expiry ASC")
    fun observeAll(): Flow<List<FridgeItem>>

    @Query("SELECT * FROM fridge_items WHERE dirty = 1")
    suspend fun getDirty(): List<FridgeItem>

    @Query("SELECT * FROM fridge_items WHERE id = :id")
    suspend fun getById(id: String): FridgeItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FridgeItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<FridgeItem>)

    @Query("UPDATE fridge_items SET deleted = 1, dirty = 1, updatedAt = :now WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)

    @Query("UPDATE fridge_items SET dirty = 0 WHERE id = :id AND updatedAt = :updatedAt")
    suspend fun markClean(id: String, updatedAt: Long)

    @Query("DELETE FROM fridge_items WHERE deleted = 1 AND dirty = 0")
    suspend fun purgeDeleted()

    @Query("DELETE FROM fridge_items")
    suspend fun clearAll()
}