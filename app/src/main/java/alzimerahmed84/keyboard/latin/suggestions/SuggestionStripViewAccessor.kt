/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package alzimerahmed84.keyboard.latin.suggestions

import alzimerahmed84.keyboard.latin.SuggestedWords

/**
 * An object that gives basic control of a suggestion strip and some info on it.
 */
interface SuggestionStripViewAccessor {
    fun setNeutralSuggestionStrip()
    fun setSuggestions(suggestedWords: SuggestedWords)
    fun showSuggestionStrip()
}
