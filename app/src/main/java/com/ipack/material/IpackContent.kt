package com.ipack.material

import android.content.Context
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

    private val tag = IpackContent::class.simpleName

    fun getIcons(): List<IpackIcon> {
        val drawableClass = com.ipack.icons.R.drawable::class.java
        return drawableClass.fields.mapNotNull { field ->
            try {
                if (field.type == Int::class.javaPrimitiveType) {
                    val id = field.getInt(null)
                    val name = field.name
                    IpackIcon(id = id, name = name, resourceName = name)
                } else null
            } catch (e: Exception) {
                Log.e(tag, "getIcons ${field.name}", e)
                null
            }
        }.sortedBy { it.name.lowercase() }
    }

    fun getIcon(context: Context, id: Int): IpackIcon {
        val name = context.resources.getResourceEntryName(id)
        return IpackIcon(id = id, name = name, resourceName = name)
    }
}
