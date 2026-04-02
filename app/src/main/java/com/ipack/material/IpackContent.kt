package com.ipack.material

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri

object IpackContent {

    const val ALL_SAME_SIZE: Boolean = true
    const val ATTRIBUTION: String = "https://materialdesignicons.com/"
    const val LABEL: String = "Material Design Icons"

    const val DEFAULT_CELL_SIZE: Int = 72
    const val DEFAULT_ICON_SIZE: Int = 48
    const val DEFAULT_GRID_BACK_COLOUR: Int = 0 // 0 indicates use theme default

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
}
