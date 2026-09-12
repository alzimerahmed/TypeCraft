// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.translation

import android.content.Context
import android.net.Uri
import alzimerahmed84.keyboard.latin.plugin.PluginClassLoader
import alzimerahmed84.keyboard.latin.plugin.PluginContext
import alzimerahmed84.keyboard.latin.plugin.PluginDownloads
import alzimerahmed84.keyboard.latin.plugin.PluginFiles
import alzimerahmed84.keyboard.latin.plugin.PluginSpec
import alzimerahmed84.keyboard.latin.utils.Log
import alzimerahmed84.keyboard.latin.utils.prefs
import java.io.File

object TranslationLoader {
    private const val CURRENT_INTERFACE_VERSION = 2

    private val SPEC = PluginSpec(
        id = "translation",
        repoName = "TypeCraft-Translation-Plugin",
        baseApkName = "translation_plugin",
        pluginClassName = "alzimerahmed84.keyboard.translation.plugin.TranslationProviderImpl",
        interfaceVersion = CURRENT_INTERFACE_VERSION,
        prefHasPlugin = "pref_translation_has_plugin",
        tag = "TranslationLoader"
    )
    private val TAG = SPEC.tag

    private var activeProvider: ITranslationProvider? = null
    private var cachedClassLoader: PluginClassLoader? = null
    private var cachedApkModified: Long = 0L

    fun getTargetAbi(): String = PluginDownloads.getTargetAbi(SPEC.supportedAbis)

    fun getPluginDownloadUrl(tag: String? = null): String = PluginDownloads.getPluginDownloadUrl(SPEC, tag)

    fun downloadPluginApk(context: Context, tag: String? = null, tempFile: File): Boolean =
        PluginDownloads.downloadPluginApk(SPEC, tag, tempFile)

    fun getProvider(context: Context): ITranslationProvider? {
        val cached = activeProvider
        if (cached != null) return cached
        if (!hasPlugin(context)) return null

        val apkFile = PluginFiles.apkFile(context, SPEC)
        if (!apkFile.exists()) {
            context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
            return null
        }
        apkFile.setReadOnly()

        return try {
            val provider = loadProviderFromApk(context, apkFile)
            if (provider != null) {
                activeProvider = provider
            }
            provider
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load translation plugin", e)
            null
        }
    }

    private fun loadProviderFromApk(context: Context, apkFile: File): ITranslationProvider? {
        TranslationModelImporter.migrateLegacyModels(context)
        PluginFiles.ensureWorkManagerInitialized(context)
        val nativeLibDir = PluginFiles.getNativeLibDir(context, SPEC, apkFile)
        PluginFiles.extractNativeLibs(apkFile, nativeLibDir, android.os.Build.SUPPORTED_ABIS.toList(), TAG)
        val cachedLoader = cachedClassLoader
        val classLoader = if (cachedLoader != null && cachedApkModified == apkFile.lastModified()) {
            cachedLoader
        } else {
            val cl = PluginClassLoader(
                apkFile.absolutePath,
                context.codeCacheDir.absolutePath,
                nativeLibDir.absolutePath,
                context.classLoader,
                listOf(SPEC.pluginPackagePrefix) + SPEC.classLoaderPrefixes
            )
            cachedClassLoader = cl
            cachedApkModified = apkFile.lastModified()
            cl
        }
        val clazz = classLoader.loadClass(SPEC.pluginClassName)
        val provider = clazz.getDeclaredConstructor().newInstance() as ITranslationProvider

        if (provider.getInterfaceVersion() > SPEC.interfaceVersion) {
            Log.w(TAG, "Plugin version newer than supported interface!")
            return null
        }

        val mergedContext = PluginContext(
            context.applicationContext,
            apkFile.absolutePath,
            classLoader,
            TAG
        )
        val pluginRuntime = alzimerahmed84.keyboard.latin.work.PluginRuntime(
            classLoader = classLoader,
            workerContext = mergedContext
        )
        alzimerahmed84.keyboard.latin.App.pluginWorkerFactory.pluginRuntime = pluginRuntime

        provider.init(mergedContext)
        return provider
    }

    fun hasPlugin(context: Context): Boolean {
        return context.prefs().getBoolean(SPEC.prefHasPlugin, false)
    }

    fun getPluginVersion(context: Context): String? = PluginFiles.getPluginVersion(context, SPEC)

    @Suppress("ReturnCount")
    fun importPlugin(context: Context, uri: Uri): Boolean {
        try {
            PluginFiles.clearCodeCache(context)

            val apkFile = PluginFiles.apkFile(context, SPEC)
            if (apkFile.exists()) {
                apkFile.delete()
            }
            context.contentResolver.openInputStream(uri)?.use { input ->
                apkFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            apkFile.setReadOnly()

            // Verify the plugin loads successfully
            val provider = loadProviderFromApk(context, apkFile)
            if (provider == null) {
                PluginFiles.deletePluginArtifacts(context, SPEC)
                PluginFiles.clearCodeCache(context)
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
                activeProvider = null
                return false
            }

            context.prefs().edit().putBoolean(SPEC.prefHasPlugin, true).apply()
            activeProvider = provider
            return true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to import translation plugin APK", e)
            try {
                PluginFiles.apkFile(context, SPEC).delete()
            } catch (_: Exception) {
            }
            try {
                PluginFiles.clearCodeCache(context)
            } catch (_: Exception) {
            }
            try {
                PluginFiles.deletePluginArtifacts(context, SPEC)
            } catch (_: Exception) {
            }
            context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
            activeProvider = null
        }
        return false
    }

    fun unloadPlugin() {
        try {
            activeProvider?.cleanup()
        } catch (e: Throwable) {
            Log.e(TAG, "Error during plugin cleanup", e)
        }
        activeProvider = null
    }

    fun removePlugin(context: Context) {
        unloadPlugin()
        cachedClassLoader = null
        cachedApkModified = 0L
        alzimerahmed84.keyboard.latin.App.pluginWorkerFactory.pluginRuntime = null
        try {
            PluginFiles.apkFile(context, SPEC).delete()
        } catch (_: Exception) {
        }
        try {
            File(context.cacheDir, "temp_translation_plugin.apk").delete()
        } catch (_: Exception) {
        }
        try {
            context.cacheDir.listFiles()?.forEach { f ->
                if (f.name.contains("translation_plugin")) {
                    f.delete()
                }
            }
        } catch (_: Exception) {
        }
        try {
            PluginFiles.clearCodeCache(context)
        } catch (_: Exception) {
        }
        try {
            PluginFiles.deletePluginArtifacts(context, SPEC)
        } catch (_: Exception) {
        }
        context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
    }
}
