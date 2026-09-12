// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.plugin

/**
 * Static description of a loadable plugin APK. Each feature loader (AI, OCR,
 * handwriting, translation) owns one instance; all shared plugin mechanics are
 * driven from this spec so the per-loader code contains no duplicated logic.
 */
@Suppress("LongParameterList")
class PluginSpec(
    /** Short identifier, also used as the native-lib directory prefix (e.g. "ocr_"). */
    val id: String,
    /** GitHub repository hosting the plugin releases, e.g. "TypeCraft-OCR-Plugin". */
    val repoName: String,
    /** Base APK filename without extension/ABI, e.g. "ocr_plugin". */
    val baseApkName: String,
    /** Fully-qualified plugin implementation class inside the APK. */
    val pluginClassName: String,
    /** Highest plugin interface version this host supports. */
    val interfaceVersion: Int,
    /** SharedPreferences key marking the plugin as installed. */
    val prefHasPlugin: String,
    /**
     * ABI candidates in priority order, used for download URLs and native-lib
     * extraction. Loaders with a narrower ABI set pass fewer entries.
     */
    val supportedAbis: List<String> = listOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86"),
    /** Package prefixes that must be loaded child-first by [PluginClassLoader]. */
    val classLoaderPrefixes: List<String> = listOf(
        "com.google.mlkit.",
        "com.google.android.datatransport.",
        "com.google.android.gms.",
        "com.google.firebase."
    ),
    /** Log tag used by the shared helpers. */
    val tag: String = "PluginLoader:$id"
) {
    val pluginPackagePrefix: String
        get() = pluginClassName.substringBeforeLast('.') + "."

    val apkFilename: String
        get() = "$baseApkName.apk"

    fun abiApkFilename(abi: String): String = "$baseApkName-$abi.apk"
}
