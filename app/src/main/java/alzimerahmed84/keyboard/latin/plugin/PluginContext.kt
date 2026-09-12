// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.plugin

import android.content.Context
import android.content.ContextWrapper

/**
 * Context wrapper handed to plugin implementations: exposes the plugin APK's
 * resources/assets and classloader over the host base context, and forwards the
 * host's WorkManager configuration so plugin-scheduled work initializes.
 */
open class PluginContext(
    base: Context,
    apkPath: String,
    private val pluginClassLoader: ClassLoader,
    tag: String = "PluginContext"
) : ContextWrapper(base), androidx.work.Configuration.Provider {

    @Suppress("TooGenericExceptionCaught")
    private val pluginResources: android.content.res.Resources by lazy {
        try {
            val assetManager = android.content.res.AssetManager::class.java.getDeclaredConstructor().newInstance()
            val addAssetPathMethod = android.content.res.AssetManager::class.java
                .getDeclaredMethod("addAssetPath", String::class.java)
            addAssetPathMethod.invoke(assetManager, apkPath)
            android.content.res.Resources(assetManager, base.resources.displayMetrics, base.resources.configuration)
        } catch (e: Throwable) {
            alzimerahmed84.keyboard.latin.utils.Log.e(tag, "Failed to create plugin resources", e)
            base.resources
        }
    }

    override fun getResources(): android.content.res.Resources = pluginResources

    override fun getAssets(): android.content.res.AssetManager = pluginResources.assets

    override fun getClassLoader(): ClassLoader = pluginClassLoader

    override fun getApplicationContext(): Context = this

    override val workManagerConfiguration: androidx.work.Configuration
        get() = (baseContext.applicationContext as? androidx.work.Configuration.Provider)?.workManagerConfiguration
            ?: androidx.work.Configuration.Builder().build()
}
