// SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
package alzimerahmed84.keyboard.latin

import android.app.Application
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.work.Configuration
import alzimerahmed84.keyboard.keyboard.emoji.SupportedEmojis
import alzimerahmed84.keyboard.latin.define.DebugFlags
import alzimerahmed84.keyboard.latin.settings.Defaults
import alzimerahmed84.keyboard.latin.settings.Settings
import alzimerahmed84.keyboard.latin.utils.LayoutUtilsCustom
import alzimerahmed84.keyboard.latin.utils.Log
import alzimerahmed84.keyboard.latin.utils.SubtypeSettings

import alzimerahmed84.keyboard.latin.work.PluginWorkerFactory

class App : Application(), Configuration.Provider {

    // WorkManager Configuration.Provider — required for dynamic plugins (ML Kit Digital Ink & Translation).
    override val workManagerConfiguration: Configuration
        get() {
            val delegating = androidx.work.DelegatingWorkerFactory()
            delegating.addFactory(pluginWorkerFactory)
            return Configuration.Builder()
                .setWorkerFactory(delegating)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        DebugFlags.init(this)
        Settings.init(this)
        val useSystemEmoji = Settings.getInstance().useSystemEmoji()
        if (!useSystemEmoji) {
            val config = DefaultEmojiCompatConfig.create(this)
            if (config != null) {
                EmojiCompat.init(config)
            }
        }
        SubtypeSettings.init(this)
        RichInputMethodManager.init(this)

        AppUpgrade.checkVersionUpgrade(this)
        AppUpgrade.transferOldPinnedClips(this) // todo: remove in a few months, maybe mid 2026
        app = this
        Defaults.initDynamicDefaults(this)
        LayoutUtilsCustom.removeMissingLayouts(this) // only after version upgrade
        SupportedEmojis.load(this)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        @Suppress("DEPRECATION")
        Log.i(
            "startup", "Starting ${applicationInfo.processName} version ${packageInfo.versionName} (${
                packageInfo.versionCode
            }) on Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})"
        )
    }

    companion object {
        val pluginWorkerFactory = PluginWorkerFactory()
        // used so JniUtils can access application once
        private var app: App? = null
        fun getApp(): App? {
            val application = app
            app = null
            return application
        }
    }
}
