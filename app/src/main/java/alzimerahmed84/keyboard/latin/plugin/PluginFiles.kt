// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.plugin

import alzimerahmed84.keyboard.latin.utils.Log
import java.io.File

/** Shared plugin APK cache/install mechanics: native libs, versions, artifact cleanup. */
@Suppress("TooManyFunctions")
object PluginFiles {
    private const val PLUGIN_LIBS_DIR = "plugin_libs"

    fun apkFile(context: android.content.Context, spec: PluginSpec): File =
        File(context.filesDir, spec.apkFilename)

    /**
     * Returns (and prunes stale siblings of) the native-lib directory for this
     * plugin, keyed by the APK's last-modified timestamp.
     */
    fun getNativeLibDir(context: android.content.Context, spec: PluginSpec, apkFile: File): File {
        val baseDir = File(context.filesDir, PLUGIN_LIBS_DIR)
        if (!baseDir.exists()) baseDir.mkdirs()
        val targetName = "${spec.id}_${apkFile.lastModified()}"
        pruneNativeLibDirs(baseDir, spec, targetName)
        return File(baseDir, targetName)
    }

    private fun pruneNativeLibDirs(baseDir: File, spec: PluginSpec, exceptName: String) {
        baseDir.listFiles()?.forEach { f ->
            val isOwn = f.name.startsWith(spec.id + "_") || f.name == spec.id
            if (f.isDirectory && isOwn && f.name != exceptName) {
                deleteQuietly(f)
            }
        }
    }

    private fun deleteQuietly(f: File) {
        try {
            f.deleteRecursively()
        } catch (_: Exception) {
        }
    }

    /**
     * Extracts `lib/<abi>/` shared-object entries from the plugin APK into [outputDir].
     * The ABI is the first entry of [abiCandidates] that exists in the APK.
     */
    @Suppress("TooGenericExceptionCaught")
    fun extractNativeLibs(apkFile: File, outputDir: File, abiCandidates: List<String>, tag: String) {
        if (!outputDir.exists()) outputDir.mkdirs()
        try {
            java.util.zip.ZipFile(apkFile).use { zip ->
                val targetAbi = findTargetAbi(zip, abiCandidates) ?: return
                extractAbiEntries(zip, "lib/$targetAbi/", outputDir)
            }
        } catch (e: Throwable) {
            Log.e(tag, "Failed to extract native libraries", e)
        }
    }

    private fun extractAbiEntries(zip: java.util.zip.ZipFile, prefix: String, outputDir: File) {
        for (entry in zip.entries().asSequence()) {
            if (entry.name.startsWith(prefix) && entry.name.endsWith(".so")) {
                extractEntry(zip, entry, prefix, outputDir)
            }
        }
    }

    private fun findTargetAbi(zip: java.util.zip.ZipFile, abiCandidates: List<String>): String? =
        abiCandidates.firstOrNull { abi ->
            zip.entries().asSequence().any { it.name.startsWith("lib/$abi/") && it.name.endsWith(".so") }
        }

    private fun extractEntry(
        zip: java.util.zip.ZipFile,
        entry: java.util.zip.ZipEntry,
        prefix: String,
        outputDir: File
    ) {
        val outFile = File(outputDir, entry.name.substring(prefix.length))
        if (!outFile.exists() || outFile.length() != entry.size) {
            zip.getInputStream(entry).use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            outFile.setReadable(true, false)
            outFile.setExecutable(true, false)
            outFile.setReadOnly()
        }
    }

    fun hasPluginApk(context: android.content.Context, spec: PluginSpec): Boolean {
        val apkFile = apkFile(context, spec)
        return apkFile.exists() && apkFile.length() > 0
    }

    fun getPluginVersion(context: android.content.Context, spec: PluginSpec): String? {
        val apkFile = apkFile(context, spec)
        if (!apkFile.exists()) return null
        return try {
            val info = context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            info?.versionName
        } catch (_: Exception) {
            null
        }
    }

    fun clearCodeCache(context: android.content.Context) {
        try {
            context.codeCacheDir.deleteRecursively()
        } catch (_: Exception) {
        }
    }

    /** Deletes the plugin APK and all native-lib directories for [spec]. */
    fun deletePluginArtifacts(context: android.content.Context, spec: PluginSpec) {
        try {
            apkFile(context, spec).delete()
        } catch (_: Exception) {
        }
        try {
            pruneNativeLibDirs(File(context.filesDir, PLUGIN_LIBS_DIR), spec, exceptName = "")
        } catch (_: Exception) {
        }
    }

    fun ensureWorkManagerInitialized(context: android.content.Context) {
        try {
            androidx.work.WorkManager.getInstance(context)
        } catch (_: IllegalStateException) {
            try {
                androidx.work.WorkManager.initialize(
                    context.applicationContext,
                    (context.applicationContext as? androidx.work.Configuration.Provider)?.workManagerConfiguration
                        ?: androidx.work.Configuration.Builder().build()
                )
            } catch (_: Throwable) {
            }
        }
    }
}
