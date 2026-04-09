package com.palmoe.oneuiiconextractor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palmoe.oneuiiconextractor.ui.SettingsScreen
import com.palmoe.oneuiiconextractor.ui.theme.OneUIIconExtractorTheme

class SettingsActivity : ComponentActivity() {
    private val settingsStore by lazy { AppSettingsStore.get(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val samsungIconApiAvailable = SamsungIconResolver.isSamsungIconApiAvailable(packageManager)

        setContent {
            OneUIIconExtractorTheme {
                val settings = settingsStore.settings.collectAsStateWithLifecycle().value

                SettingsScreen(
                    settings = settings,
                    samsungIconApiAvailable = samsungIconApiAvailable,
                    onBack = ::finish,
                    onSelectIconSourceMode = settingsStore::updatePreferredIconSourceMode,
                    onSelectExportSize = settingsStore::updateExportSizePx
                )
            }
        }
    }
}
