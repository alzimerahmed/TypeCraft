/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package alzimerahmed84.keyboard.keyboard.internal

import alzimerahmed84.keyboard.keyboard.Key
import alzimerahmed84.keyboard.keyboard.PopupKeysPanel
import alzimerahmed84.keyboard.keyboard.PointerTracker

interface DrawingProxy {
    fun onKeyPressed(key: Key, withPreview: Boolean)
    fun onKeyReleased(key: Key, withAnimation: Boolean)
    fun showPopupKeysKeyboard(key: Key, tracker: PointerTracker): PopupKeysPanel?
    fun startWhileTypingAnimation(fadeInOrOut: Int)
    fun showSlidingKeyInputPreview(tracker: PointerTracker?)
    fun showGestureTrail(tracker: PointerTracker, showsFloatingPreviewText: Boolean)
    fun dismissGestureFloatingPreviewTextWithoutDelay()

    companion object {
        const val FADE_IN = 0
        const val FADE_OUT = 1
    }
}
