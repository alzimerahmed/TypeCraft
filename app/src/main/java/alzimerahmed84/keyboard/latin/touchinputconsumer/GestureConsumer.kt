/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package alzimerahmed84.keyboard.latin.touchinputconsumer

import android.view.inputmethod.EditorInfo
import alzimerahmed84.keyboard.keyboard.Keyboard
import alzimerahmed84.keyboard.latin.DictionaryFacilitator
import alzimerahmed84.keyboard.latin.SuggestedWords
import alzimerahmed84.keyboard.latin.common.InputPointers
import alzimerahmed84.keyboard.latin.inputlogic.PrivateCommandPerformer
import java.util.Locale

/**
 * Stub for GestureConsumer.
 * <br>
 * The methods of this class should only be called from a single thread, e.g.,
 * the UI Thread.
 */
@Suppress("unused")
class GestureConsumer private constructor() {

    fun willConsume(): Boolean = false

    fun onInit(locale: Locale?, keyboard: Keyboard?) {}

    fun onGestureStarted(locale: Locale?, keyboard: Keyboard?) {}

    fun onGestureCanceled() {}

    fun onGestureCompleted(inputPointers: InputPointers?) {}

    fun onImeSuggestionsProcessed(
        suggestedWords: SuggestedWords?,
        composingStart: Int,
        composingLength: Int,
        dictionaryFacilitator: DictionaryFacilitator?
    ) {}

    companion object {
        val NULL_GESTURE_CONSUMER: GestureConsumer = GestureConsumer()

        fun newInstance(
            editorInfo: EditorInfo?,
            commandPerformer: PrivateCommandPerformer?,
            locale: Locale?,
            keyboard: Keyboard?
        ): GestureConsumer {
            return NULL_GESTURE_CONSUMER
        }
    }
}
