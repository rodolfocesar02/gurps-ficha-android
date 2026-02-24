package com.gurps.ficha.data.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FichaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FichaDatabase : RoomDatabase() {
    abstract fun fichaDao(): FichaDao

    companion object {
        @Volatile
        private var INSTANCE: FichaDatabase? = null

        fun getInstance(context: Context): FichaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    FichaDatabase::class.java,
                    "gurps_fichas.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
