package com.kidsguard.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import com.kidsguard.app.R
import com.kidsguard.app.model.AppInfo

object AppRepository {

    /** Todas las apps con icono en el launcher, excluyendo a KidsGuard. */
    fun getLaunchableApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, 0)
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map {
                AppInfo(
                    packageName = it.activityInfo.packageName,
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm),
                    category = categoryLabel(context, it.activityInfo.applicationInfo)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** Solo las apps de la lista blanca, para el launcher infantil. */
    fun getAllowedApps(context: Context, allowed: Set<String>): List<AppInfo> =
        getLaunchableApps(context).filter { it.packageName in allowed }

    /** Categoría declarada por la propia app (disponible desde API 26). */
    private fun categoryLabel(context: Context, app: ApplicationInfo): String =
        when (app.category) {
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
