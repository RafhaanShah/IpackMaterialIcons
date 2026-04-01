package com.ipack.material

object IpackContent {

    fun getIcons(): List<IpackIcon> {
        val drawableClass = com.ipack.icons.R.drawable::class.java
        return drawableClass.fields.mapNotNull { field ->
            try {
                if (field.type == Int::class.javaPrimitiveType) {
                    val id = field.getInt(null)
                    val name = field.name
                    // We can use the field name as the icon name
                    IpackIcon(id, name, name)
                } else null
            } catch (e: Exception) {
                null
            }
        }.sortedBy { it.name.lowercase() }
    }
}
