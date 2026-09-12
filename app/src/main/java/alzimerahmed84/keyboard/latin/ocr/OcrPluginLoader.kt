// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.ocr

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

object OcrPluginLoader {
    private const val CURRENT_INTERFACE_VERSION = 1
    const val PREF_OCR_SCRIPT = "pref_ocr_script"
    const val DEFAULT_OCR_SCRIPT = "latin"
    const val PREF_OCR_KEEP_LINE_BREAKS = "pref_ocr_keep_line_breaks"
    const val PREF_OCR_TRIM_WHITESPACE = "pref_ocr_trim_whitespace"
    const val PREF_OCR_CASING = "pref_ocr_casing"
    const val PREF_OCR_LINE_JOIN_FORMAT = "pref_ocr_line_join_format"
    const val PREF_OCR_DEHYPHENATE = "pref_ocr_dehyphenate"
    const val PREF_OCR_NORMALIZE_PUNCTUATION = "pref_ocr_normalize_punctuation"
    const val PREF_OCR_STRIP_BULLETS = "pref_ocr_strip_bullets"
    const val PREF_OCR_REMOVE_NOISE = "pref_ocr_remove_noise"
    const val PREF_OCR_AUTO_COPY = "pref_ocr_auto_copy"
    const val PREF_OCR_AUTO_INSERT = "pref_ocr_auto_insert"
    const val PREF_OCR_SUGGEST_SCREENSHOT_TEXT = "pref_ocr_suggest_screenshot_text"
    const val PREF_OCR_PERSIST_FLASH = "pref_ocr_persist_flash"

    private val SPEC = PluginSpec(
        id = "ocr",
        repoName = "TypeCraft-OCR-Plugin",
        baseApkName = "ocr_plugin",
        pluginClassName = "alzimerahmed84.keyboard.ocr.plugin.TextRecognizerImpl",
        interfaceVersion = CURRENT_INTERFACE_VERSION,
        prefHasPlugin = "pref_ocr_has_plugin",
        tag = "OcrPluginLoader"
    )
    private val TAG = SPEC.tag

    private var activeRecognizer: ITextRecognizer? = null
    private var cachedClassLoader: PluginClassLoader? = null
    private var cachedApkModified: Long = 0L

    fun resetRecognizer() {
        activeRecognizer?.release()
        activeRecognizer = null
    }

    private fun invalidateClassLoader() {
        resetRecognizer()
        cachedClassLoader = null
        cachedApkModified = 0L
    }

    fun getTargetAbi(): String = PluginDownloads.getTargetAbi(SPEC.supportedAbis)

    fun getPluginDownloadUrl(tag: String? = null): String = PluginDownloads.getPluginDownloadUrl(SPEC, tag)

    fun downloadPluginApk(context: Context, tag: String? = null, tempFile: File): Boolean =
        PluginDownloads.downloadPluginApk(SPEC, tag, tempFile)

    fun getRecognizer(context: Context): ITextRecognizer? {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) return null
        if (activeRecognizer != null) return activeRecognizer
        if (!hasPlugin(context)) return null

        val prefs = context.prefs()
        if (!prefs.contains(PREF_OCR_SCRIPT)) {
            prefs.edit().putString(PREF_OCR_SCRIPT, DEFAULT_OCR_SCRIPT).apply()
        }

        val apkFile = PluginFiles.apkFile(context, SPEC)
        if (!apkFile.exists()) {
            context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
            return null
        }
        apkFile.setReadOnly()

        return loadRecognizerInternal(context, apkFile)
    }

    private fun loadRecognizerInternal(context: Context, apkFile: File): ITextRecognizer? {
        return try {
            PluginFiles.ensureWorkManagerInitialized(context)
            val apkLastModified = apkFile.lastModified()
            val nativeLibDir = PluginFiles.getNativeLibDir(context, SPEC, apkFile)
            PluginFiles.extractNativeLibs(apkFile, nativeLibDir, android.os.Build.SUPPORTED_ABIS.toList(), TAG)

            val cachedLoader = cachedClassLoader
            val classLoader = if (cachedLoader != null && cachedApkModified == apkLastModified) {
                cachedLoader
            } else {
                PluginClassLoader(
                    apkFile.absolutePath,
                    context.codeCacheDir.absolutePath,
                    nativeLibDir.absolutePath,
                    context.classLoader,
                    listOf(SPEC.pluginPackagePrefix) + SPEC.classLoaderPrefixes
                ).also {
                    cachedClassLoader = it
                    cachedApkModified = apkLastModified
                }
            }

            val clazz = classLoader.loadClass(SPEC.pluginClassName)
            val recognizer = clazz.getDeclaredConstructor().newInstance() as ITextRecognizer

            if (recognizer.getInterfaceVersion() > CURRENT_INTERFACE_VERSION) {
                Log.w(TAG, "Plugin interface version is newer than supported")
                return null
            }

            val pluginContext = PluginContext(context.applicationContext, apkFile.absolutePath, classLoader, TAG)
            recognizer.init(pluginContext)

            if (recognizer.isAvailable()) {
                activeRecognizer = recognizer
                Log.i(TAG, "OCR recognizer loaded successfully (${recognizer.getScriptName()})")
                recognizer
            } else {
                Log.w(TAG, "OCR recognizer is not available after initialization")
                null
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to load OCR plugin", e)
            null
        }
    }

    fun hasPlugin(context: Context): Boolean {
        val has = context.prefs().getBoolean(SPEC.prefHasPlugin, false)
        if (!has) return false
        return PluginFiles.hasPluginApk(context, SPEC)
    }

    fun getPluginVersion(context: Context): String? = PluginFiles.getPluginVersion(context, SPEC)

    fun getActiveScriptName(context: Context): String? {
        val recognizer = getRecognizer(context)
        return recognizer?.getDisplayName() ?: recognizer?.getScriptName()
    }

    fun importPlugin(context: Context, uri: Uri): Boolean {
        return try {
            PluginFiles.clearCodeCache(context)

            val targetFile = PluginFiles.apkFile(context, SPEC)
            if (targetFile.exists()) targetFile.delete()

            context.contentResolver.openInputStream(uri)?.use { input ->
                java.io.FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return false

            targetFile.setReadOnly()
            invalidateClassLoader()

            val recognizer = loadRecognizerInternal(context, targetFile)
            val success = recognizer != null
            if (success) {
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, true).apply()
                Log.i(TAG, "OCR plugin imported and verified successfully")
            } else {
                targetFile.delete()
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
                Log.w(TAG, "OCR plugin verification failed")
            }
            success
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to import OCR plugin", e)
            false
        }
    }

    fun importPluginFromTempFile(context: Context, tempFile: File): Boolean {
        return try {
            PluginFiles.clearCodeCache(context)

            val targetFile = PluginFiles.apkFile(context, SPEC)
            if (targetFile.exists()) targetFile.delete()

            tempFile.copyTo(targetFile, overwrite = true)
            tempFile.delete()
            targetFile.setReadOnly()

            invalidateClassLoader()

            val recognizer = loadRecognizerInternal(context, targetFile)
            val success = recognizer != null
            if (success) {
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, true).apply()
                Log.i(TAG, "OCR plugin imported from temp file successfully")
            } else {
                targetFile.delete()
                context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
                Log.w(TAG, "OCR plugin temp file verification failed")
            }
            success
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to import OCR plugin from temp file", e)
            false
        }
    }

    fun removePlugin(context: Context) {
        try {
            invalidateClassLoader()
            PluginFiles.deletePluginArtifacts(context, SPEC)
        } catch (_: Exception) {
        }
        context.prefs().edit().putBoolean(SPEC.prefHasPlugin, false).apply()
    }

    fun release() {
        resetRecognizer()
    }
}
