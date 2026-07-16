package com.kidsguard.app.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable,
    /** Categoría del sistema (Juegos, Vídeo…) ya traducida, o null. */
    val category: String? = null
)
