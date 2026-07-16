package com.kidsguard.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface UsageDao {

    @Query(
        "SELECT * FROM daily_usage WHERE profileId = :profileId AND date >= :fromDate"
    )
    fun usageSince(profileId: Int, fromDate: String): List<DailyUsageEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertIgnore(entity: DailyUsageEntity): Long

    @Query(
        "UPDATE daily_usage SET seconds = seconds + :seconds " +
            "WHERE date = :date AND packageName = :packageName AND profileId = :profileId"
    )
    fun increment(date: String, packageName: String, profileId: Int, seconds: Int): Int

    /** Suma segundos al acumulado del día, creando la fila si no existe. */
    @Transaction
    fun addSeconds(date: String, packageName: String, profileId: Int, seconds: Int) {
        val inserted = insertIgnore(DailyUsageEntity(date, packageName, profileId, seconds))
        if (inserted == -1L) {
            increment(date, packageName, profileId, seconds)
        }
    }

    @Query("DELETE FROM daily_usage WHERE date < :beforeDate")
    fun prune(beforeDate: String)
}
