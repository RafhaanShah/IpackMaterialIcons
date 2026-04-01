package com.ipack.material

object IpackKeys {
    const val ANDROID_RESOURCE_PREFIX: String = "android.resource://"
    const val ANDROID_RESOURCE_SCHEME: String = "android.resource"
    const val ICON_DOWNLOAD_URI_DIRECT: String = "http://ipack.dinglisch.net/download.html"
    const val ICON_DOWNLOAD_URI_MARKET: String = "market://search?q=ipack"
    private const val PACKAGE_NAME = "net.dinglisch.android.ipack"
    const val PREFIX: String = "ipack://"
    const val RECEIVER_NAME: String = "IpackReceiver"
    const val SCHEME: String = "ipack"
    const val SELECTOR_NAME: String = "IpackIconSelect"

    object Actions {
        const val ICON_SELECT: String = "net.dinglisch.android.ipack.actions.ICON_SELECT"
        const val NOTIFY: String = "net.dinglisch.android.ipack.actions.NOTIFY"
        const val NOTIFY_CANCEL: String = "net.dinglisch.android.ipack.actions.NOTIFY_CANCEL"
        private const val PREFIX = "net.dinglisch.android.ipack.actions."
        const val QUERY_ICONS: String = "net.dinglisch.android.ipack.actions.QUERY_ICONS"
        const val QUERY_PACKS: String = "net.dinglisch.android.ipack.actions.QUERY_PACKS"
    }

    object Extras {
        const val ALL_SAME_SIZE: String = "net.dinglisch.android.ipack.extras.ALL_SAME_SIZE"
        const val ATTRIBUTION: String = "net.dinglisch.android.ipack.extras.ATTRIBUTION"
        const val CELL_SIZE: String = "net.dinglisch.android.ipack.extras.CELL_SIZE"
        const val GRID_BACK_COLOUR: String = "net.dinglisch.android.ipack.extras.GRID_BACK_COLOUR"
        const val ICON_DISPLAY_SIZE: String = "net.dinglisch.android.ipack.extras.ICON_DISPLAY_SIZE"
        const val ICON_ID: String = "net.dinglisch.android.ipack.extras.ICON_ID"
        const val ICON_LABEL: String = "net.dinglisch.android.ipack.extras.ICON_LABEL"
        const val ICON_NAME: String = "net.dinglisch.android.ipack.extras.ICON_NAME"
        const val LABEL: String = "net.dinglisch.android.ipack.extras.LABEL"
        const val NOTIFICATION: String = "net.dinglisch.android.ipack.extras.NOTIFICATION"
        const val NOTIFICATION_ID: String = "net.dinglisch.android.ipack.extras.NOTIFICATION_ID"
        const val NOTIFICATION_PI: String = "net.dinglisch.android.ipack.extras.NOTIFICATION_PI"
        const val NOTIFICATION_TEXT: String = "net.dinglisch.android.ipack.extras.NOTIFICATION_TEXT"
        const val NOTIFICATION_TITLE: String =
            "net.dinglisch.android.ipack.extras.NOTIFICATION_TITLE"
        private const val PREFIX = "net.dinglisch.android.ipack.extras."
    }
}
