package com.palmoe.oneuiiconextractor

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.LruCache
import com.palmoe.oneuiiconextractor.model.InstalledApp
import java.lang.reflect.Method

object SamsungIconResolver {
    private val previewCache = object : LruCache<String, Bitmap>(12 * 1024) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    @Volatile
    private var cachedActivityMethod: Method? = null

    @Volatile
    private var cachedApplicationMethod: Method? = null

    @Volatile
    private var didResolveMethods = false

    private val iconTrayModes = intArrayOf(1, 0)

    fun isSamsungIconApiAvailable(packageManager: PackageManager): Boolean {
        ensureMethodsResolved(packageManager)
        return cachedActivityMethod != null || cachedApplicationMethod != null
    }

    fun loadBitmap(
        context: Context,
        app: InstalledApp,
        sizePx: Int,
        useCache: Boolean,
        iconSourceMode: IconSourceMode
    ): Bitmap? {
        val cacheKey = buildString {
            append(app.packageName)
            append(':')
            append(app.componentName?.className.orEmpty())
            append(':')
            append(sizePx)
            append(':')
            append(iconSourceMode.storageValue)
        }
        if (useCache) {
            synchronized(previewCache) {
                previewCache.get(cacheKey)?.let { return it }
            }
        }

        val drawable = loadDrawable(
            context = context,
            app = app,
            iconSourceMode = iconSourceMode
        ) ?: return null
        val bitmap = drawable.toBitmap(sizePx) ?: return null
        if (useCache) {
            synchronized(previewCache) {
                previewCache.put(cacheKey, bitmap)
            }
        }
        return bitmap
    }

    private fun loadDrawable(
        context: Context,
        app: InstalledApp,
        iconSourceMode: IconSourceMode
    ): Drawable? {
        val packageManager = context.packageManager
        ensureMethodsResolved(packageManager)

        if (iconSourceMode == IconSourceMode.Samsung) {
            invokeActivityIcon(packageManager, app.componentName)?.let { return it }
            invokeApplicationIcon(packageManager, app.packageName)?.let { return it }
        }

        return try {
            app.componentName?.let(packageManager::getActivityIcon)
                ?: packageManager.getApplicationIcon(app.packageName)
        } catch (_: Exception) {
            null
        }
    }

    private fun ensureMethodsResolved(packageManager: PackageManager) {
        if (didResolveMethods) {
            return
        }

        synchronized(this) {
            if (didResolveMethods) {
                return
            }

            cachedActivityMethod = findMethod(
                target = packageManager,
                name = "semGetActivityIconForIconTray",
                firstParameterType = ComponentName::class.java
            )
            cachedApplicationMethod = findMethod(
                target = packageManager,
                name = "semGetApplicationIconForIconTray",
                firstParameterType = String::class.java
            )
            didResolveMethods = true
        }
    }

    private fun findMethod(target: Any, name: String, firstParameterType: Class<*>): Method? {
        var currentClass: Class<*>? = target.javaClass
        while (currentClass != null) {
            val method = currentClass.declaredMethods.firstOrNull { candidate ->
                val parameterTypes = candidate.parameterTypes
                candidate.name == name &&
                    parameterTypes.size == 2 &&
                    parameterTypes[0] == firstParameterType &&
                    (parameterTypes[1] == Int::class.javaPrimitiveType || parameterTypes[1] == Int::class.javaObjectType)
            }

            if (method != null) {
                method.isAccessible = true
                return method
            }

            currentClass = currentClass.superclass
        }

        return null
    }

    private fun invokeActivityIcon(
        packageManager: PackageManager,
        componentName: ComponentName?
    ): Drawable? {
        val method = cachedActivityMethod ?: return null
        val component = componentName ?: return null
        for (mode in iconTrayModes) {
            val drawable = runCatching {
                method.invoke(packageManager, component, mode) as? Drawable
            }.getOrNull()
            if (drawable != null) {
                return drawable
            }
        }
        return null
    }

    private fun invokeApplicationIcon(
        packageManager: PackageManager,
        packageName: String
    ): Drawable? {
        val method = cachedApplicationMethod ?: return null
        for (mode in iconTrayModes) {
            val drawable = runCatching {
                method.invoke(packageManager, packageName, mode) as? Drawable
            }.getOrNull()
            if (drawable != null) {
                return drawable
            }
        }
        return null
    }

    private fun Drawable.toBitmap(sizePx: Int): Bitmap? {
        if (this is BitmapDrawable && bitmap != null && !bitmap.isRecycled) {
            val sourceBitmap = bitmap
            if (sourceBitmap.width == sizePx && sourceBitmap.height == sizePx) {
                return sourceBitmap
            }
        }

        val width = sizePx.coerceAtLeast(1)
        val height = sizePx.coerceAtLeast(1)
        return runCatching {
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { target ->
                val canvas = Canvas(target)
                setBounds(0, 0, canvas.width, canvas.height)
                draw(canvas)
            }
        }.getOrNull()
    }

}
