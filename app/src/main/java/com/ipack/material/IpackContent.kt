package com.ipack.material

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.util.Log

object IpackContent {

    const val ALL_SAME_SIZE: Boolean = true
    const val ATTRIBUTION: String = "https://materialdesignicons.com/"
    const val LABEL: String = "Material Design Icons"

    const val DEFAULT_CELL_SIZE: Int = 72
    const val DEFAULT_ICON_SIZE: Int = 48
    const val DEFAULT_ICON_EXPORT_SIZE: Int = 128
    const val DEFAULT_GRID_BACK_COLOUR: Int = 0 // 0 indicates use theme default
    const val HEX_FORMAT: String = "%08X"
    const val PNG_MIME_TYPE: String = "image/png"
    const val PNG_EXTENSION: String = ".png"
    const val DRAWABLE_RESOURCE_TYPE: String = "drawable"
    const val LIGHT_SUFFIX = "_light"
    const val DARK_SUFFIX = "_dark"

    private val tag = IpackContent::class.simpleName

    fun getIcons(context: Context): List<IpackIcon> {
        val drawableClass = com.ipack.icons.R.drawable::class.java
        Log.d(tag, "getIcons: ${drawableClass.name}")
        val icons = mutableMapOf<String, IpackIcon>()
        drawableClass.fields.mapNotNull { field ->
            try {
                if (field.type == Int::class.javaPrimitiveType) {
                    val name = field.name.removeSuffix(LIGHT_SUFFIX).removeSuffix(DARK_SUFFIX)
                    icons.putIfAbsent(name, getIcon(context, name))
                }
            } catch (e: Exception) {
                Log.e(tag, "getIcons: ${field.name}", e)
                null
            }
        }

        Log.d(tag, "getIcons result:${icons.size}")
        return icons.values.sortedBy { it.name.lowercase() }
    }

    @SuppressLint("DiscouragedApi")
    fun getIcon(context: Context, name: String): IpackIcon {
        val packageName = context.packageName
        val darkResName = "${name}$LIGHT_SUFFIX"
        val lightResName = "${name}${DARK_SUFFIX}"
        val darkResId =
            context.resources.getIdentifier(darkResName, DRAWABLE_RESOURCE_TYPE, packageName)
        val lightResId =
            context.resources.getIdentifier(lightResName, DRAWABLE_RESOURCE_TYPE, packageName)
        return IpackIcon(
            name = name,
            darkResId = darkResId,
            lightResId = lightResId,
        )
    }

    fun isDarkMode(context: Context): Boolean =
        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            else -> false
        }
}
