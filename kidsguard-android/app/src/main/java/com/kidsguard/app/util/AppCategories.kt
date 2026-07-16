package com.kidsguard.app.util

import android.content.Context
import android.content.pm.ApplicationInfo
import com.kidsguard.app.R

/**
 * Categorías del sistema declaradas por cada app (API 26+), usadas para
 * agrupar la selección de apps y para los límites por categoría.
 */
object AppCategories {

    /** Todas las categorías configurables, con «otras» al final. */
    val ALL = listOf(
        ApplicationInfo.CATEGORY_GAME,
        ApplicationInfo.CATEGORY_AUDIO,
        ApplicationInfo.CATEGORY_VIDEO,
        ApplicationInfo.CATEGORY_IMAGE,
        ApplicationInfo.CATEGORY_SOCIAL,
        ApplicationInfo.CATEGORY_NEWS,
        ApplicationInfo.CATEGORY_MAPS,
        ApplicationInfo.CATEGORY_PRODUCTIVITY,
        ApplicationInfo.CATEGORY_UNDEFINED
    )

    private val cache = mutableMapOf<String, Int>()

    /** Categoría de un paquete, con caché (se consulta en cada bloqueo). */
    fun of(context: Context, packageName: String): Int =
        synchronized(cache) {
            cache.getOrPut(packageName) {
                runCatching {
                    context.packageManager.getApplicationInfo(packageName, 0).category
                }.getOrDefault(ApplicationInfo.CATEGORY_UNDEFINED)
            }
        }

    fun label(context: Context, category: Int): String = when (category) {
        ApplicationInfo.CATEGORY_GAME -> context.getString(R.string.category_games)
        ApplicationInfo.CATEGORY_AUDIO -> context.getString(R.string.category_audio)
        ApplicationInfo.CATEGORY_VIDEO -> context.getString(R.string.category_video)
        ApplicationInfo.CATEGORY_IMAGE -> context.getString(R.string.category_image)
        ApplicationInfo.CATEGORY_SOCIAL -> context.getString(R.string.category_social)
        ApplicationInfo.CATEGORY_NEWS -> context.getString(R.string.category_news)
        ApplicationInfo.CATEGORY_MAPS -> context.getString(R.string.category_maps)
        ApplicationInfo.CATEGORY_PRODUCTIVITY ->
            context.getString(R.string.category_productivity)
        else -> context.getString(R.string.category_other)
    }
}
