package com.ipack.material

import android.content.res.Resources
import android.os.Bundle

object IpackContent {

    val ATTRIBUTION: CharSequence = "Attribution"
    val LABEL= "Material Icon"

    fun fillBundle(res: Resources, bundle: Bundle) {
        bundle.putInt("android", com.ipack.icons.R.drawable.android);
        bundle.putInt("file", com.ipack.icons.R.drawable.file);
    }
}
