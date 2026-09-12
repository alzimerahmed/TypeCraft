// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.plugin

import android.content.Context
import alzimerahmed84.keyboard.latin.utils.prefs
import java.io.File
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class PluginCoreTest {

    private lateinit var context: Context

    private val spec = PluginSpec(
        id = "test",
        repoName = "TypeCraft-Test-Plugin",
        baseApkName = "test_plugin",
        pluginClassName = "alzimerahmed84.keyboard.test.plugin.TestImpl",
        interfaceVersion = 1,
        prefHasPlugin = "pref_test_has_plugin"
    )

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `download urls use abi-specific apk first then fallback`() {
        val urls = PluginDownloads.buildDownloadUrls(spec, null)
        assertEquals(2, urls.size)
        assertTrue(urls[0].endsWith("/releases/latest/download/test_plugin-${PluginDownloads.getTargetAbi(spec.supportedAbis)}.apk"))
        assertTrue(urls[1].endsWith("/releases/latest/download/test_plugin.apk"))
        assertEquals(urls.toSet().size, urls.size)
    }

    @Test
    fun `download urls with tag use tagged release path`() {
        val urls = PluginDownloads.buildDownloadUrls(spec, "v1.2.3")
        assertTrue(urls.all { it.contains("/releases/download/v1.2.3/") })
        assertFalse(urls.any { it.contains("latest") })
    }

    @Test
    fun `native lib dir is keyed by apk timestamp and prunes stale dirs`() {
        val apk = File(context.filesDir, spec.apkFilename).apply {
            writeBytes(ByteArray(10))
            setLastModified(1000L)
        }
        val stale = File(File(context.filesDir, "plugin_libs"), "test_999").apply { mkdirs() }
        val otherPlugin = File(File(context.filesDir, "plugin_libs"), "ocr_999").apply { mkdirs() }

        val dir = PluginFiles.getNativeLibDir(context, spec, apk)

        assertEquals("test_1000", dir.name)
        assertFalse(stale.exists())
        assertTrue(otherPlugin.exists())
    }

    @Test
    fun `extract native libs pulls matching abi so files`() {
        val apk = File(context.cacheDir, "fake_plugin.apk")
        ZipOutputStream(apk.outputStream()).use { zip ->
            val entry = java.util.zip.ZipEntry("lib/arm64-v8a/libtest.so")
            zip.putNextEntry(entry)
            zip.write(ByteArray(16) { it.toByte() })
            zip.closeEntry()
        }
        val outDir = File(context.cacheDir, "fake_libs")

        PluginFiles.extractNativeLibs(apk, outDir, listOf("x86", "arm64-v8a"), "PluginCoreTest")

        val lib = File(outDir, "libtest.so")
        assertTrue(lib.exists())
        assertEquals(16L, lib.length())
        assertTrue(lib.canExecute())
    }

    @Test
    fun `hasPluginApk depends only on a non-empty apk file`() {
        assertFalse(PluginFiles.hasPluginApk(context, spec))

        context.prefs().edit().putBoolean(spec.prefHasPlugin, true).apply()
        assertFalse(PluginFiles.hasPluginApk(context, spec))

        PluginFiles.apkFile(context, spec).writeBytes(ByteArray(4))
        assertTrue(PluginFiles.hasPluginApk(context, spec))
    }

    @Test
    fun `getPluginVersion returns null for missing or invalid apk`() {
        assertNull(PluginFiles.getPluginVersion(context, spec))
        PluginFiles.apkFile(context, spec).writeBytes(ByteArray(4))
        assertNull(PluginFiles.getPluginVersion(context, spec))
    }

    @Test
    fun `deletePluginArtifacts removes apk and native lib dirs`() {
        val apk = PluginFiles.apkFile(context, spec).apply { writeBytes(ByteArray(4)) }
        val libDir = File(File(context.filesDir, "plugin_libs"), "test_1").apply { mkdirs() }

        PluginFiles.deletePluginArtifacts(context, spec)

        assertFalse(apk.exists())
        assertFalse(libDir.exists())
    }
}
