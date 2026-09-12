// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.plugin

import alzimerahmed84.keyboard.latin.utils.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Pure functions for building plugin download URLs and ABI selection. */
object PluginDownloads {
    private const val RELEASES_BASE = "https://github.com/alzimerahmed84"
    private const val USER_AGENT = "TypeCraftL"
    private const val MAX_REDIRECTS = 5

    /** First ABI from [candidates] present on this device, in device preference order. */
    fun getTargetAbi(candidates: List<String>): String {
        for (abi in android.os.Build.SUPPORTED_ABIS) {
            if (candidates.contains(abi)) return abi
        }
        return candidates.firstOrNull() ?: "arm64-v8a"
    }

    fun getPluginDownloadUrl(spec: PluginSpec, tag: String? = null): String {
        val abi = getTargetAbi(spec.supportedAbis)
        return buildReleaseUrl(spec.repoName, tag ?: "latest", spec.abiApkFilename(abi))
    }

    /**
     * Ordered list of download URLs to try: the ABI-specific APK first, then the
     * ABI-less fallback APK.
     */
    fun buildDownloadUrls(spec: PluginSpec, tag: String? = null): List<String> {
        val t = tag ?: "latest"
        return listOf(
            buildReleaseUrl(spec.repoName, t, spec.abiApkFilename(getTargetAbi(spec.supportedAbis))),
            buildReleaseUrl(spec.repoName, t, spec.apkFilename)
        ).distinct()
    }

    private fun buildReleaseUrl(repo: String, tag: String, filename: String): String {
        return if (tag == "latest") {
            "$RELEASES_BASE/$repo/releases/latest/download/$filename"
        } else {
            "$RELEASES_BASE/$repo/releases/download/$tag/$filename"
        }
    }

    /**
     * Downloads the plugin APK for [spec] into [tempFile], trying each candidate
     * URL in order. Returns true on success.
     */
    @Suppress("TooGenericExceptionCaught")
    fun downloadPluginApk(spec: PluginSpec, tag: String? = null, tempFile: File): Boolean {
        for (urlStr in buildDownloadUrls(spec, tag)) {
            try {
                if (downloadFrom(urlStr, tempFile)) return true
            } catch (e: Exception) {
                @Suppress("TooGenericExceptionCaught")
                Log.w(spec.tag, "Failed to download from $urlStr", e)
            }
        }
        return false
    }

    private fun downloadFrom(urlStr: String, tempFile: File): Boolean {
        var conn = open(urlStr)
        try {
            var status = conn.responseCode
            var redirectCount = 0
            while (isRedirect(status) && redirectCount < MAX_REDIRECTS) {
                val newUrl = conn.getHeaderField("Location")
                conn.disconnect()
                conn = open(newUrl)
                status = conn.responseCode
                redirectCount++
            }

            if (status != HttpURLConnection.HTTP_OK) return false
            conn.inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return true
        } finally {
            conn.disconnect()
        }
    }

    private fun isRedirect(status: Int): Boolean =
        status == HttpURLConnection.HTTP_MOVED_TEMP ||
            status == HttpURLConnection.HTTP_MOVED_PERM ||
            status == HttpURLConnection.HTTP_SEE_OTHER

    private fun open(urlStr: String): HttpURLConnection {
        val conn = URL(urlStr).openConnection() as HttpURLConnection
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", USER_AGENT)
        conn.connect()
        return conn
    }
}
