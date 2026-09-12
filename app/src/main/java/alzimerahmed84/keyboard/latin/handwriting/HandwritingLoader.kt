// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.handwriting

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

object HandwritingLoader {
    private const val PLUGIN_CLASS_NAME = "alzimerahmed84.keyboard.handwriting.plugin.HandwritingRecognizerImpl"

    private val SPEC = PluginSpec(
        id = "handwriting",
        repoName = "TypeCraft-Handwriting-Plugin",
        baseApkName = "handwriting_plugin",
        pluginClassName = PLUGIN_CLASS_NAME,
        interfaceVersion = Int.MAX_VALUE,
        prefHasPlugin = "pref_handwriting_has_plugin",
        tag = "HandwritingLoader"
    )
    private val TAG = SPEC.tag

    const val PREF_HANDWRITING_LANGUAGE = "pref_handwriting_language"
    const val LANG_FOLLOW_KEYBOARD = "default"

    private var activeRecognizer: HandwritingRecognizer? = null

    fun resetRecognizer() {
        activeRecognizer = null
        displayNameCache = null
    }

    fun getHandwritingLanguagePref(context: Context): String {
        return context.prefs().getString(PREF_HANDWRITING_LANGUAGE, LANG_FOLLOW_KEYBOARD) ?: LANG_FOLLOW_KEYBOARD
    }

    fun setHandwritingLanguage(context: Context, language: String) {
        context.prefs().edit().putString(PREF_HANDWRITING_LANGUAGE, language).apply()
    }

    fun findInstalledModelForLanguage(context: Context, languageTag: String): String? {
        val target = languageTag.trim()
        if (target.isBlank()) return null
        val targetCanonical = HandwritingModelImporter.canonicalTagKey(target)
        val targetBase = targetCanonical.substringBefore('-')

        // 1. Direct / Canonical match for target (e.g. "fr-CA", "en-US", "ml")
        if (HandwritingModelImporter.hasModelDirectly(context, target)) {
            return target
        }
        if (HandwritingModelImporter.hasModelDirectly(context, targetCanonical)) {
            return targetCanonical
        }

        // 2. Base language match (e.g. "fr" for "fr-CA", "en" for "en-IN", "ml" for "ml-IN")
        if (HandwritingModelImporter.hasModelDirectly(context, targetBase)) {
            return targetBase
        }

        // 3. Sibling regional variant of same base language (e.g. "fr-FR" for "fr-CA", "en-US" for "en-IN")
        val installedMap = HandwritingModelImporter.getInstalledLanguageStatuses(context)
        for ((installedTag, status) in installedMap) {
            if (status.isReady) {
                val installedCanonical = HandwritingModelImporter.canonicalTagKey(installedTag)
                val installedBase = installedCanonical.substringBefore('-')
                if (installedBase == targetBase) {
                    return installedTag
                }
            }
        }

        return null
    }

    fun getEffectiveLanguage(context: Context, subtypeLanguage: String): String {
        val pref = getHandwritingLanguagePref(context)
        val target = if (pref == LANG_FOLLOW_KEYBOARD || pref.isBlank()) {
            subtypeLanguage
        } else {
            pref
        }
        return findInstalledModelForLanguage(context, target) ?: target
    }

    private class DisplayNameCache(val tag: String, val name: String)

    @Volatile
    private var displayNameCache: DisplayNameCache? = null

    fun getEffectiveDisplayName(context: Context, subtypeLanguage: String): String {
        val tag = getEffectiveLanguage(context, subtypeLanguage)
        val currentCache = displayNameCache
        if (currentCache != null && currentCache.tag == tag) {
            return currentCache.name
        }
        val displayName = try {
            val locale = java.util.Locale.forLanguageTag(tag)
            val sysLocale = context.resources.configuration.locales[0]
            val name = locale.getDisplayName(sysLocale)
            if (name.isNullOrBlank()) tag else name
        } catch (_: Exception) {
            tag
        }
        displayNameCache = DisplayNameCache(tag, displayName)
        return displayName
    }

    fun getTargetAbi(): String = PluginDownloads.getTargetAbi(SPEC.supportedAbis)

    fun getPluginDownloadUrl(tag: String? = null): String = PluginDownloads.getPluginDownloadUrl(SPEC, tag)

    fun downloadPluginApk(context: Context, tag: String? = null, tempFile: File): Boolean =
        PluginDownloads.downloadPluginApk(SPEC, tag, tempFile)

    private const val NATIVE_LOADER_DEX_BASE64 = "ZGV4CjAzNQAkiCvTdFX0r/3RrbselneGBCvx+cvJKtkwAwAAcAAAAHhWNBIAAAAAAAAAAJACAAAKAAAAcAAAAAUAAACYAAAAAgAAAKwAAAAAAAAAAAAAAAQAAADEAAAAAQAAAOQAAAAsAgAABAEAAEYBAABOAQAAhAEAAJgBAACsAQAAwAEAANMBAADWAQAA2gEAAOABAAABAAAAAgAAAAMAAAAEAAAABgAAAAYAAAAEAAAAAAAAAAcAAAAEAAAAQAEAAAAAAAAAAAAAAAABAAgAAAABAAAAAAAAAAMAAQAIAAAAAAAAAAEAAAABAAAAAAAAAAUAAAAAAAAAfgIAAAAAAAABAAEAAQAAADQBAAAEAAAAcBACAAAADgABAAEAAQAAADgBAAAEAAAAcRADAAAADgACAA4ABAEADjwAAAABAAAAAgAGPGluaXQ+ADRMaGVsaXVtMzE0L2tleWJvYXJkL2hhbmR3cml0aW5nL3BsdWdpbi9OYXRpdmVMb2FkZXI7ABJMamF2YS9sYW5nL09iamVjdDsAEkxqYXZhL2xhbmcvU3RyaW5nOwASTGphdmEvbGFuZy9TeXN0ZW07ABFOYXRpdmVMb2FkZXIuamF2YQABVgACVkwABGxvYWQAmwF+fkQ4eyJiYWNrZW5kIjoiZGV4IiwiY29tcGlsYXRpb24tbW9kZSI6ImRlYnVnIiwiaGFzLWNoZWNrc3VtcyI6ZmFsc2UsIm1pbi1hcGkiOjEsInNoYS0xIjoiNzUwYTIxYjRmNDI4MWIxZjQ1M2I2NDllMGI4NGYxYmE5YzA0ZjRmYyIsInZlcnNpb24iOiI5LjAuMy1kZXYifQAAAAIAAIGABIQCAQmcAgAAAAANAAAAAAAAAAEAAAAAAAAAAQAAAAoAAABwAAAAAgAAAAUAAACYAAAAAwAAAAIAAACsAAAABQAAAAQAAADEAAAABgAAAAEAAADkAAAAASAAAAIAAAAEAQAAAyAAAAIAAAA0AQAAARAAAAEAAABAAQAAAiAAAAoAAABGAQAAACAAAAEAAAB+AgAAAxAAAAEAAACMAgAAABAAAAEAAACQAgAA"

    private fun getNativeLoaderDex(context: Context): File {
        val dexFile = File(context.codeCacheDir, "native_loader.dex")
        if (!dexFile.exists() || dexFile.length() == 0L) {
            try {
                if (dexFile.exists()) {
                    dexFile.setWritable(true)
                    dexFile.delete()
                }
                val bytes = android.util.Base64.decode(NATIVE_LOADER_DEX_BASE64, android.util.Base64.DEFAULT)
                dexFile.outputStream().use { it.write(bytes) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write native loader dex", e)
            }
        }
        dexFile.setReadOnly()
        return dexFile
    }

    private fun loadNativeLibrariesInPlugin(classLoader: ClassLoader, libFile: File) {
        if (!libFile.exists()) return
        var loadedInPlugin = false
        try {
            val loaderClass = classLoader.loadClass("alzimerahmed84.keyboard.handwriting.plugin.NativeLoader")
            val loadMethod = loaderClass.getMethod("load", String::class.java)
            loadMethod.invoke(null, libFile.absolutePath)
            loadedInPlugin = true
            Log.i(TAG, "Successfully loaded native digitalink library into PluginClassLoader")
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load digitalink library via NativeLoader in PluginClassLoader", e)
        }
        if (!loadedInPlugin) {
            try {
                System.load(libFile.absolutePath)
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to System.load libdigitalink.so", e)
            }
        }
    }

    fun getRecognizer(context: Context): HandwritingRecognizer? {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return null
        if (activeRecognizer != null) return activeRecognizer
        if (!hasPlugin(context)) return null

        val apkFile = PluginFiles.apkFile(context, SPEC)
        if (!apkFile.exists()) {
            context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
            return null
        }
        apkFile.setReadOnly()

        try {
            Log.i(TAG, "Loaded plugin APK path: ${apkFile.absolutePath}, size: ${apkFile.length()}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log plugin info", e)
        }

        try {
            val recognizer = loadRecognizerFromApk(context, apkFile)
            if (recognizer != null) {
                activeRecognizer = recognizer
            }
            return recognizer
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load handwriting plugin", e)
        } catch (e: LinkageError) {
            Log.e(TAG, "Failed to link handwriting plugin (ML Kit incompatible on this OS)", e)
        }
        return null
    }

    private fun loadRecognizerFromApk(context: Context, apkFile: File): HandwritingRecognizer {
        PluginFiles.ensureWorkManagerInitialized(context)
        val nativeLibDir = PluginFiles.getNativeLibDir(context, SPEC, apkFile)
        PluginFiles.extractNativeLibs(apkFile, nativeLibDir, android.os.Build.SUPPORTED_ABIS.toList(), TAG)
        val libFile = File(nativeLibDir, "libdigitalink.so")
        val nativeLoaderDex = getNativeLoaderDex(context)
        val dexPaths = "${apkFile.absolutePath}${File.pathSeparator}${nativeLoaderDex.absolutePath}"
        val classLoader = PluginClassLoader(
            dexPaths,
            context.codeCacheDir.absolutePath,
            nativeLibDir.absolutePath,
            context.classLoader,
            listOf(SPEC.pluginPackagePrefix) + SPEC.classLoaderPrefixes
        )
        loadNativeLibrariesInPlugin(classLoader, libFile)
        val clazz = classLoader.loadClass(SPEC.pluginClassName)
        val recognizer = clazz.getDeclaredConstructor().newInstance() as HandwritingRecognizer
        val pluginContext = PluginContext(context.applicationContext, apkFile.absolutePath, classLoader, TAG)
        recognizer.init(pluginContext)
        return recognizer
    }

    fun hasPlugin(context: Context): Boolean {
        return context.prefs().getBoolean(SPEC.prefHasPlugin, false)
    }

    fun getPluginVersion(context: Context): String? = PluginFiles.getPluginVersion(context, SPEC)

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
            val recognizer = loadRecognizerFromApk(context, apkFile)

            context.prefs().edit().putBoolean(SPEC.prefHasPlugin, true).apply()
            activeRecognizer = recognizer
            return true
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to import plugin APK", e)
            // Cleanup on failure
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
            activeRecognizer = null
        }
        return false
    }

    fun removePlugin(context: Context) {
        try {
            File(context.cacheDir, "temp_handwriting_plugin.apk").delete()
        } catch (_: Exception) {
        }
        try {
            context.cacheDir.listFiles()?.forEach { f ->
                if (f.name.contains("handwriting_plugin")) {
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
        activeRecognizer = null
    }
}
