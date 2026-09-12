// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.ai

import android.content.Context
import android.net.Uri
import alzimerahmed84.keyboard.latin.plugin.PluginClassLoader
import alzimerahmed84.keyboard.latin.plugin.PluginDownloads
import alzimerahmed84.keyboard.latin.plugin.PluginFiles
import alzimerahmed84.keyboard.latin.plugin.PluginSpec
import alzimerahmed84.keyboard.latin.utils.Log
import alzimerahmed84.keyboard.latin.utils.prefs
import java.io.File

object OfflineAiLoader {
    private const val CURRENT_INTERFACE_VERSION = 1

    private val SPEC = PluginSpec(
        id = "offline_ai",
        repoName = "TypeCraft-Offline-AI-Plugin",
        baseApkName = "ai_plugin",
        pluginClassName = "alzimerahmed84.keyboard.ai.plugin.OfflineAiProviderImpl",
        interfaceVersion = CURRENT_INTERFACE_VERSION,
        prefHasPlugin = "pref_offline_ai_has_plugin",
        supportedAbis = listOf("arm64-v8a", "x86_64"),
        classLoaderPrefixes = emptyList(),
        tag = "OfflineAiLoader"
    )
    private val TAG = SPEC.tag

    private var activeProvider: IOfflineAiProvider? = null

    fun getTargetAbi(): String = PluginDownloads.getTargetAbi(SPEC.supportedAbis)

    fun getPluginDownloadUrl(tag: String? = null): String = PluginDownloads.getPluginDownloadUrl(SPEC, tag)

    fun downloadPluginApk(context: Context, tag: String? = null, tempFile: File): Boolean =
        PluginDownloads.downloadPluginApk(SPEC, tag, tempFile)

    fun getProvider(context: Context): IOfflineAiProvider? {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return null
        val cached = activeProvider
        if (cached != null) return cached
        if (!hasPlugin(context)) return null

        val apkFile = PluginFiles.apkFile(context, SPEC)
        if (!apkFile.exists()) {
            context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
            return null
        }
        val provider = loadProviderInternal(context, apkFile)
        if (provider != null) {
            activeProvider = provider
        }
        return provider
    }

    private fun loadProviderInternal(context: Context, apkFile: File): IOfflineAiProvider? {
        return try {
            apkFile.setReadOnly()
            val nativeLibDir = PluginFiles.getNativeLibDir(context, SPEC, apkFile)
            PluginFiles.extractNativeLibs(apkFile, nativeLibDir, listOf(getTargetAbi()), TAG)
            val classLoader = PluginClassLoader(
                apkFile.absolutePath,
                context.codeCacheDir.absolutePath,
                nativeLibDir.absolutePath,
                context.classLoader
            )
            val clazz = classLoader.loadClass(SPEC.pluginClassName)
            val provider = clazz.getDeclaredConstructor().newInstance() as IOfflineAiProvider

            if (provider.getInterfaceVersion() > CURRENT_INTERFACE_VERSION) {
                Log.w(TAG, "Plugin version newer than supported interface!")
                return null
            }

            provider.init(context)
            Log.i(TAG, "Offline AI provider loaded successfully")
            provider
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load offline AI plugin", e)
            null
        }
    }

    fun hasPlugin(context: Context): Boolean {
        val has = context.prefs().getBoolean(SPEC.prefHasPlugin, false)
        if (!has) return false
        return PluginFiles.hasPluginApk(context, SPEC)
    }

    fun getPluginVersion(context: Context): String? = PluginFiles.getPluginVersion(context, SPEC)

    fun loadPlugin(context: Context, sourceUri: Uri): Boolean {
        return try {
            PluginFiles.clearCodeCache(context)

            val targetFile = PluginFiles.apkFile(context, SPEC)
            if (targetFile.exists()) targetFile.delete()

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                java.io.FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return false

            targetFile.setReadOnly()
            activeProvider?.cleanup()
            activeProvider = null

            val provider = loadProviderInternal(context, targetFile)
            val success = provider != null && provider.isAvailable()
            if (success) {
                activeProvider = provider
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, true).apply()
                Log.i(TAG, "Plugin imported and registered successfully")
            } else {
                targetFile.delete()
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
                Log.w(TAG, "Plugin import verification failed")
            }
            success
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load plugin from URI", e)
            false
        }
    }

    fun loadPluginFromTempFile(context: Context, tempFile: File): Boolean {
        return try {
            PluginFiles.clearCodeCache(context)

            val targetFile = PluginFiles.apkFile(context, SPEC)
            if (targetFile.exists()) targetFile.delete()

            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
            targetFile.setReadOnly()

            activeProvider?.cleanup()
            activeProvider = null

            val provider = loadProviderInternal(context, targetFile)
            val success = provider != null && provider.isAvailable()
            if (success) {
                activeProvider = provider
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, true).apply()
                Log.i(TAG, "Plugin imported from temp file successfully")
            } else {
                targetFile.delete()
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
                Log.w(TAG, "Plugin temp file verification failed")
            }
            success
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load plugin from temp file", e)
            false
        }
    }

    fun removePlugin(context: Context) {
        try {
            activeProvider?.cleanup()
            activeProvider = null
            PluginFiles.deletePluginArtifacts(context, SPEC)
            context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
            Log.i(TAG, "Plugin removed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing plugin", e)
        }
    }
}
