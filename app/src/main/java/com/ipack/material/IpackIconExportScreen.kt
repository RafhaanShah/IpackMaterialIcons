package com.ipack.material

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.ipack.material.ui.theme.IpackMaterialIconsTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.Serializable

@Serializable
data class ExportDestination(val iconId: Int)

@Composable
fun IpackIconExportRoute(
    onBackClick: () -> Unit,
    viewModel: IpackIconExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ExportEvent.Success -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT)
                    .show()

                is ExportEvent.Error -> Toast.makeText(context, event.message, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png")
    ) { uri ->
        uri?.let { viewModel.exportIcon(context, it) }
    }

    IpackIconExportScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onShowDialog = viewModel::setShowDialog,
        onUpdateColor = viewModel::updateColor,
        onUpdateHexCode = viewModel::updateHexCode,
        onUpdateFileName = viewModel::updateFileName,
        onUpdateIconSize = viewModel::updateIconSize,
        onExportConfirm = { fileName ->
            val finalFileName =
                if (fileName.lowercase().endsWith(".png")) fileName else "$fileName.png"
            exportLauncher.launch(finalFileName)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpackIconExportScreen(
    uiState: IpackIconExportUiState,
    onBackClick: () -> Unit,
    onShowDialog: (Boolean) -> Unit,
    onUpdateColor: (Color) -> Unit,
    onUpdateHexCode: (String) -> Unit,
    onUpdateFileName: (String) -> Unit,
    onUpdateIconSize: (String) -> Unit,
    onExportConfirm: (String) -> Unit,
) {
    BackHandler(onBack = onBackClick)

    val controller = rememberColorPickerController()

    LaunchedEffect(uiState.selectedColor) {
        controller.selectByColor(uiState.selectedColor, fromUser = false)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Export Icon") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = com.ipack.icons.R.drawable.arrow_left),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { onShowDialog(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                enabled = !uiState.isLoading
            ) {
                Text(if (uiState.isLoading) "Exporting..." else "Export")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isLightColor = uiState.selectedColor.luminance() > 0.5f
            val targetColor1 = if (isLightColor) Color(0xFF121212) else Color.White
            val targetColor2 = if (isLightColor) Color(0xFF242424) else Color.LightGray

            val checkerboardColor1 by animateColorAsState(
                targetValue = targetColor1,
                animationSpec = tween(durationMillis = 500),
                label = "checkerboardColor1"
            )
            val checkerboardColor2 by animateColorAsState(
                targetValue = targetColor2,
                animationSpec = tween(durationMillis = 500),
                label = "checkerboardColor2"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .checkerboard(
                        color1 = checkerboardColor1,
                        color2 = checkerboardColor2
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = uiState.icon.id),
                    contentDescription = uiState.icon.name,
                    modifier = Modifier.size(128.dp),
                    colorFilter = ColorFilter.tint(uiState.selectedColor)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    controller = controller,
                    onColorChanged = { colorEnvelope ->
                        if (colorEnvelope.fromUser) {
                            onUpdateColor(colorEnvelope.color)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AlphaSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller,
                )

                Spacer(modifier = Modifier.height(8.dp))

                BrightnessSlider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(35.dp),
                    controller = controller,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Hex Code",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = uiState.hexCode,
                        onValueChange = onUpdateHexCode,
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("#") },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        singleLine = true
                    )
                }
            }
        }

        if (uiState.showDialog) {
            AlertDialog(
                onDismissRequest = { onShowDialog(false) },
                title = { Text("Export Details") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = uiState.fileName,
                            onValueChange = onUpdateFileName,
                            label = { Text("Filename") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = uiState.iconSizeInput,
                            onValueChange = onUpdateIconSize,
                            label = { Text("Icon Size (px)") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        onShowDialog(false)
                        onExportConfirm(uiState.fileName)
                    }) {
                        Text("Export")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onShowDialog(false) }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

fun Modifier.checkerboard(
    squareSize: Dp = 8.dp,
    color1: Color = Color.White,
    color2: Color = Color.LightGray
): Modifier = drawBehind {
    val sizePx = squareSize.toPx()
    val columns = (size.width / sizePx).toInt() + 1
    val rows = (size.height / sizePx).toInt() + 1

    for (row in 0 until rows) {
        for (col in 0 until columns) {
            val color = if ((row + col) % 2 == 0) color1 else color2
            drawRect(
                color = color,
                topLeft = Offset(col * sizePx, row * sizePx),
                size = androidx.compose.ui.geometry.Size(sizePx, sizePx)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IpackIconExportScreenPreview() {
    val mockIcon = IpackIcon(
        id = android.R.drawable.ic_menu_gallery,
        name = "Gallery Icon",
        resourceName = "ic_menu_gallery"
    )
    IpackMaterialIconsTheme {
        IpackIconExportScreen(
            uiState = IpackIconExportUiState(
                icon = mockIcon,
                fileName = "Gallery Icon",
                selectedColor = Color.White
            ),
            onBackClick = {},
            onShowDialog = {},
            onUpdateColor = {},
            onUpdateHexCode = {},
            onUpdateFileName = {},
            onUpdateIconSize = {},
            onExportConfirm = {}
        )
    }
}
