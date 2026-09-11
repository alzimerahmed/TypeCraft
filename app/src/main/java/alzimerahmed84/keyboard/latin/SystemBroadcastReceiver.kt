/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package alzimerahmed84.keyboard.latin

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import alzimerahmed84.keyboard.keyboard.KeyboardLayoutSet
import alzimerahmed84.keyboard.latin.settings.Settings
import alzimerahmed84.keyboard.latin.utils.Log
import alzimerahmed84.keyboard.latin.utils.prefs
import alzimerahmed84.keyboard.settings.SettingsActivity

/**
 * This class detects the [Intent.ACTION_MY_PACKAGE_REPLACED] broadcast intent when this IME
 * package has been replaced by a newer version of the same package. This class also detects
 * [Intent.ACTION_BOOT_COMPLETED] broadcast intent.
 */
class SystemBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Log.i(TAG, "Package has been replaced: " + context.packageName)
                toggleAppIcon(context)
            }
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.i(TAG, "Boot has been completed")
                toggleAppIcon(context)
            }
            Intent.ACTION_LOCALE_CHANGED -> {
                Log.i(TAG, "System locale changed")
                KeyboardLayoutSet.onSystemLocaleChanged()
            }
        }
    }

    companion object {
        private val TAG = SystemBroadcastReceiver::class.simpleName

        fun toggleAppIcon(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return // can't change visibility in Android 10 and above
            }
            val prefs = context.prefs()
            val state = if (Settings.readShowSetupWizardIcon(prefs, context)) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            context.packageManager.setComponentEnabledSetting(
                ComponentName(context, SettingsActivity::class.java),
                state,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
