package org.umn.kulkaskis.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FridgeItem::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FridgeDatabase : RoomDatabase() {
    abstract fun fridgeDao(): FridgeDao

    companion object {
        @Volatile
        private var INSTANCE: FridgeDatabase? = null

        fun getInstance(context: Context): FridgeDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FridgeDatabase::class.java,
                    "kulkaskis.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}