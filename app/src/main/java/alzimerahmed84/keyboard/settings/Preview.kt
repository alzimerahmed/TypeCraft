// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.settings

import android.content.Context
import alzimerahmed84.keyboard.keyboard.internal.KeyboardIconsSet
import alzimerahmed84.keyboard.latin.settings.Settings
import alzimerahmed84.keyboard.latin.utils.SubtypeSettings

// file is meant for making compose previews work

fun initPreview(context: Context) {
    Settings.init(context)
    SubtypeSettings.init(context)
    SettingsActivity.settingsContainer = SettingsContainer(context)
    KeyboardIconsSet.instance.loadIcons(context)
}

const val previewDark = true
