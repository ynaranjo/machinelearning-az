package com.kidsguard.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DailyUsageEntity::class], version = 1, exportSchema = false)
abstract class KidsGuardDatabase : RoomDatabase() {

    abstract fun usageDao(): UsageDao

    companion object {
        @Volatile
        private var instance: KidsGuardDatabase? = null

        fun get(context: Context): KidsGuardDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    KidsGuardDatabase::class.java,
                    "kidsguard.db"
                ).build().also { instance = it }
            }
    }
}
