package com.palmoe.oneuiiconextractor

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class IconSourceMode(val storageValue: String) {
    Samsung("samsung"),
    Android("android");

    companion object {
        fun fromStorage(value: String?): IconSourceMode {
            return entries.firstOrNull { it.storageValue == value } ?: Samsung
        }
    }
}

data class AppSettings(
    val preferredIconSourceMode: IconSourceMode = IconSourceMode.Samsung,
    val exportSizePx: Int = AppSettingsStore.DEFAULT_EXPORT_SIZE_PX
)

class AppSettingsStore private constructor(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_ICON_SOURCE_MODE || key == KEY_EXPORT_SIZE_PX) {
            _settings.value = readSettings()
        }
    }

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    fun updatePreferredIconSourceMode(iconSourceMode: IconSourceMode) {
        preferences.edit()
            .putString(KEY_ICON_SOURCE_MODE, iconSourceMode.storageValue)
            .apply()
    }

    fun updateExportSizePx(sizePx: Int) {
        preferences.edit()
            .putInt(KEY_EXPORT_SIZE_PX, sanitizeExportSize(sizePx))
            .apply()
    }

    private fun readSettings(): AppSettings {
        return AppSettings(
            preferredIconSourceMode = IconSourceMode.fromStorage(
                preferences.getString(KEY_ICON_SOURCE_MODE, IconSourceMode.Samsung.storageValue)
            ),
            exportSizePx = sanitizeExportSize(
                preferences.getInt(KEY_EXPORT_SIZE_PX, DEFAULT_EXPORT_SIZE_PX)
            )
        )
    }

    private fun sanitizeExportSize(sizePx: Int): Int {
        return supportedExportSizes.firstOrNull { it == sizePx } ?: DEFAULT_EXPORT_SIZE_PX
    }

    companion object {
        const val DEFAULT_EXPORT_SIZE_PX = 512
        val supportedExportSizes = listOf(512, 1024, 2048)

        private const val PREFERENCES_NAME = "oneui_icon_extractor_settings"
        private const val KEY_ICON_SOURCE_MODE = "icon_source_mode"
        private const val KEY_EXPORT_SIZE_PX = "export_size_px"

        @Volatile
        private var instance: AppSettingsStore? = null

        fun get(context: Context): AppSettingsStore {
            return instance ?: synchronized(this) {
                instance ?: AppSettingsStore(context).also { instance = it }
            }
        }
    }
}
