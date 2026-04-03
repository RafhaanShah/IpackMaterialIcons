package com.ipack.material


data class IpackIcon(
    val name: String,
    val darkResId: Int,
    val lightResId: Int,
) {
    val darkResName: String = "${name}${IpackContent.DARK_SUFFIX}"
    val lightResName: String = "${name}${IpackContent.LIGHT_SUFFIX}"

    fun getResId(isDark: Boolean = false) = if (isDark) darkResId else lightResId
    fun getResName(isDark: Boolean = false) = if (isDark) darkResName else lightResName
}
