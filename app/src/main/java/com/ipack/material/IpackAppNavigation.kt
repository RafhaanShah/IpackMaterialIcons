package com.ipack.material

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun IpackAppNavigation(
    intentAction: String? = null,
    onResult: (Int, Intent?) -> Unit = { _, _ -> }
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SelectDestination(intentAction = intentAction),
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        enterTransition = { fadeIn(animationSpec = tween(400)) },
        exitTransition = { fadeOut(animationSpec = tween(400)) },
        popEnterTransition = { fadeIn(animationSpec = tween(400)) },
        popExitTransition = { fadeOut(animationSpec = tween(400)) }
    ) {
        composable<SelectDestination> {
            IpackIconSelectRoute(
                onNavigateToExport = { icon ->
                    navController.navigate(ExportDestination(iconId = icon.id))
                },
                onResult = onResult,
            )
        }
        composable<ExportDestination> {
            IpackIconExportRoute(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
