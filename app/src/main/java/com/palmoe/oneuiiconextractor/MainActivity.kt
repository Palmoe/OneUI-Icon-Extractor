package com.palmoe.oneuiiconextractor

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmoe.oneuiiconextractor.ui.MainScreen
import com.palmoe.oneuiiconextractor.ui.theme.OneUIIconExtractorTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OneUIIconExtractorTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val context = LocalContext.current
                val exportLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { uri: Uri? ->
                    if (uri == null) {
                        viewModel.onExportPickerDismissed()
                    } else {
                        viewModel.exportSelectedIcons(uri)
                    }
                }
                val openExportPicker = remember(exportLauncher) {
                    { exportLauncher.launch(null) }
                }
                val openSettings = remember(context) {
                    {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }
                }

                MainScreen(
                    uiState = uiState,
                    previewIconLoader = { app, sizePx ->
                        SamsungIconResolver.loadBitmap(
                            context = context,
                            app = app,
                            sizePx = sizePx,
                            useCache = true,
                            iconSourceMode = uiState.effectiveIconSourceMode
                        )
                    },
                    onToggleThirdPartyApps = viewModel::toggleThirdPartyApps,
                    onToggleSystemApps = viewModel::toggleSystemApps,
                    onSearchQueryChange = viewModel::updateSearchQuery,
                    onToggleApp = viewModel::toggleSelection,
                    onToggleVisibleSelection = viewModel::toggleVisibleSelection,
                    onOpenSettings = openSettings,
                    onExportClick = openExportPicker,
                    onMessageShown = viewModel::clearMessage
                )
            }
        }
    }
}
