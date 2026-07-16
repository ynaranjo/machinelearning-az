package com.kidsguard.app.data.db

import androidx.room.Entity

/** Segundos de uso de una app en un día concreto, por perfil. */
@Entity(
    tableName = "daily_usage",
    primaryKeys = ["date", "packageName", "profileId"]
)
data class DailyUsageEntity(
    val date: String, // yyyy-MM-dd
    val packageName: String,
    val profileId: Int,
    val seconds: Int
)
