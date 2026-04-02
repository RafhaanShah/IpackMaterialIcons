package com.ipack.material

import android.util.Log

object IpackContent {

    const val ALL_SAME_SIZE: Boolean = true
    const val ATTRIBUTION: String = "https://materialdesignicons.com/"
    const val LABEL: String = "Material Design Icons"

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
