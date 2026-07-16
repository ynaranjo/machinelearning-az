package com.kidsguard.app.data

import android.content.Context
import android.content.Intent
import com.kidsguard.app.model.AppInfo
import com.kidsguard.app.util.AppCategories

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
                    category = AppCategories.label(
                        context, it.activityInfo.applicationInfo.category
                    )
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /** Solo las apps de la lista blanca, para el launcher infantil. */
    fun getAllowedApps(context: Context, allowed: Set<String>): List<AppInfo> =
        getLaunchableApps(context).filter { it.packageName in allowed }
}
