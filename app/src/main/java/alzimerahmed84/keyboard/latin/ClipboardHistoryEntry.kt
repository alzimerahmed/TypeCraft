// SPDX-License-Identifier: GPL-3.0-only

package alzimerahmed84.keyboard.latin

import alzimerahmed84.keyboard.latin.settings.Settings

class ClipboardHistoryEntry(
    val id: Long,
    var timeStamp: Long,
    var isPinned: Boolean,
    var text: String,
    val imageUri: String? = null
) : Comparable<ClipboardHistoryEntry> {
    override fun compareTo(other: ClipboardHistoryEntry): Int {
        val showPinnedFirst = Settings.getInstance()?.readClipboardHistoryPinnedFirst()
            ?: Settings.getValues()?.mClipboardHistoryPinnedFirst
            ?: false
        if (showPinnedFirst) {
            val result = other.isPinned.compareTo(isPinned)
            if (result != 0) return result
        }
        return other.timeStamp.compareTo(timeStamp)
    }
}
