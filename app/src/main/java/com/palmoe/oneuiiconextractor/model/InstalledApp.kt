package com.palmoe.oneuiiconextractor.model

import android.content.ComponentName

data class InstalledApp(
    val packageName: String,
    val label: String,
    val componentName: ComponentName?,
    val isSystemApp: Boolean
)
