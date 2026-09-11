// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.settings

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import alzimerahmed84.keyboard.latin.common.LocaleUtils.constructLocale
import alzimerahmed84.keyboard.latin.settings.SettingsSubtype.Companion.toSettingsSubtype
import alzimerahmed84.keyboard.latin.settings.getTransitionAnimationScale
import alzimerahmed84.keyboard.settings.screens.AIIntegrationScreen
import alzimerahmed84.keyboard.settings.screens.AboutScreen
import alzimerahmed84.keyboard.settings.screens.AdvancedSettingsScreen
import alzimerahmed84.keyboard.settings.screens.AppearanceScreen
import alzimerahmed84.keyboard.settings.screens.ColorsScreen
import alzimerahmed84.keyboard.settings.screens.DebugScreen
import alzimerahmed84.keyboard.settings.screens.CustomAIKeysScreen
import alzimerahmed84.keyboard.settings.screens.TextExpanderScreen
import alzimerahmed84.keyboard.settings.screens.DictionaryScreen
import alzimerahmed84.keyboard.settings.screens.LibrariesHubScreen
import alzimerahmed84.keyboard.settings.screens.GestureTypingScreen
import alzimerahmed84.keyboard.settings.screens.LanguageScreen
import alzimerahmed84.keyboard.settings.screens.MainSettingsScreen
import alzimerahmed84.keyboard.settings.screens.PersonalDictionariesScreen
import alzimerahmed84.keyboard.settings.screens.PersonalDictionaryScreen
import alzimerahmed84.keyboard.settings.screens.BlockedWordsScreen
import alzimerahmed84.keyboard.settings.screens.PreferencesScreen
import alzimerahmed84.keyboard.settings.screens.SecondaryLayoutScreen
import alzimerahmed84.keyboard.settings.screens.SoundSettingsScreen
import alzimerahmed84.keyboard.settings.screens.SubtypeScreen
import alzimerahmed84.keyboard.settings.screens.TextCorrectionScreen
import alzimerahmed84.keyboard.settings.screens.ToolbarScreen
import alzimerahmed84.keyboard.settings.screens.UpdatesScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
fun SettingsNavHost(
    onClickBack: () -> Unit,
    startDestination: String? = null,
) {
    val navController = rememberNavController()
    val dir = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 1 else -1
    val target = SettingsDestination.navTarget.collectAsState()

    // duration does not change when system setting changes, but that's rare enough to not care
    val duration = (250 * getTransitionAnimationScale(LocalContext.current)).toInt()
    val animation = tween<IntOffset>(durationMillis = duration)

    fun goBack() {
        if (!navController.popBackStack()) onClickBack()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination ?: SettingsDestination.Settings,
        enterTransition = { slideInHorizontally(initialOffsetX = { +it * dir }, animationSpec = animation) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it * dir }, animationSpec = animation) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it * dir }, animationSpec = animation) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { +it * dir }, animationSpec = animation) }
    ) {
        composable(SettingsDestination.Settings) {
            MainSettingsScreen(
                onClickUpdates = { navController.navigate(SettingsDestination.Updates) },
                onClickAbout = { navController.navigate(SettingsDestination.About) },
                onClickTextCorrection = { navController.navigate(SettingsDestination.TextCorrection) },
                onClickPreferences = { navController.navigate(SettingsDestination.Preferences) },
                onClickToolbar = { navController.navigate(SettingsDestination.Toolbar) },
                onClickGestureTyping = { navController.navigate(SettingsDestination.GestureTyping) },
                onClickLibraries = { navController.navigate(SettingsDestination.Libraries) },
                onClickAdvanced = { navController.navigate(SettingsDestination.Advanced) },
                onClickAppearance = { navController.navigate(SettingsDestination.Appearance) },
                onClickLanguage = { navController.navigate(SettingsDestination.Languages) },
                onClickDictionaries = { navController.navigate(SettingsDestination.Dictionaries) },
                onClickAIIntegration = { navController.navigate(SettingsDestination.AIIntegration) },
                onClickGesture = { navController.navigate(SettingsDestination.GestureTyping) },
                onClickBack = ::goBack,
            )
        }
        composable(SettingsDestination.Updates) {
            UpdatesScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.About) {
            AboutScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.TextCorrection) {
            TextCorrectionScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Preferences) {
            PreferencesScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Toolbar) {
            ToolbarScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.GestureTyping) {
            GestureTypingScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Advanced) {
            AdvancedSettingsScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.AIIntegration) {
            AIIntegrationScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Libraries) {
            LibrariesHubScreen(
                onClickBack = ::goBack,
                onClickOfflineVoice = { navController.navigate(SettingsDestination.OfflineVoice) },
                onClickTranslation = { navController.navigate(SettingsDestination.Translation) },
                onClickHandwriting = { navController.navigate(SettingsDestination.Handwriting) },
                onClickOcr = { navController.navigate(SettingsDestination.OCR) },
                onClickAIIntegration = { navController.navigate(SettingsDestination.AIIntegration) },
                onClickSound = { navController.navigate(SettingsDestination.Sound) }
            )
        }
        composable(SettingsDestination.Sound) {
            SoundSettingsScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.OCR) {
            alzimerahmed84.keyboard.settings.screens.OcrSettingsScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.CustomAIKeys) {
            CustomAIKeysScreen(
                onClickBack = ::goBack,
                onNavigateToConfig = { index -> 
                    navController.navigate(SettingsDestination.CustomAIKeyConfig + index) 
                }
            )
        }
        composable(SettingsDestination.CustomAIKeyConfig + "{index}") {
            val index = it.arguments?.getString("index")?.toIntOrNull() ?: 1
            alzimerahmed84.keyboard.settings.screens.ConfigCustomAIKeyScreen(
                index = index,
                onClickBack = ::goBack
            )
        }
        composable(SettingsDestination.Debug) {
            DebugScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Appearance) {
            AppearanceScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.PersonalDictionary + "{locale}") {
            val locale = it.arguments?.getString("locale")?.takeIf { loc -> loc.isNotBlank() }?.constructLocale()
            PersonalDictionaryScreen(
                onClickBack = ::goBack,
                locale = locale
            )
        }
        composable(SettingsDestination.PersonalDictionaries) {
            PersonalDictionariesScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.BlockedWords) {
            BlockedWordsScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Languages) {
            LanguageScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.LanguagesList) {
            alzimerahmed84.keyboard.settings.screens.LanguagesListScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Dictionaries) {
            DictionaryScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Layouts) {
            SecondaryLayoutScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Colors + "{theme}") {
            ColorsScreen(isNight = false, theme = it.arguments?.getString("theme"), onClickBack = ::goBack)
        }
        composable(SettingsDestination.ColorsNight + "{theme}") {
            ColorsScreen(isNight = true, theme = it.arguments?.getString("theme"), onClickBack = ::goBack)
        }
        composable(SettingsDestination.Subtype + "{subtype}") {
            val subtypeArg = it.arguments?.getString("subtype") ?: ""
            SubtypeScreen(initialSubtype = subtypeArg.toSettingsSubtype(), onClickBack = ::goBack)
        }
        composable(SettingsDestination.TextExpander) {
            TextExpanderScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.BackgroundServices) {
            alzimerahmed84.keyboard.settings.screens.BackgroundServicesScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.OfflineVoice) {
            alzimerahmed84.keyboard.latin.voice.VoiceSettingsScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Translation) {
            alzimerahmed84.keyboard.settings.screens.TranslationSettingsScreen(onClickBack = ::goBack)
        }
        composable(SettingsDestination.Handwriting) {
            alzimerahmed84.keyboard.settings.screens.HandwritingSettingsScreen(onClickBack = ::goBack)
        }
    }
    if (target.value != SettingsDestination.Settings/* && target.value != navController.currentBackStackEntry?.destination?.route*/)
        navController.navigate(route = target.value)
}

object SettingsDestination {
    const val Settings = "settings"
    const val Updates = "updates"
    const val About = "about"
    const val TextCorrection = "text_correction"
    const val Preferences = "preferences"
    const val Toolbar = "toolbar"
    const val GestureTyping = "gesture_typing"
    const val Advanced = "advanced"
    const val Libraries = "libraries_hub"
    const val AIIntegration = "ai_integration"
    const val Debug = "debug"
    const val Appearance = "appearance"
    const val Colors = "colors/"
    const val ColorsNight = "colors_night/"
    const val PersonalDictionaries = "personal_dictionaries"
    const val PersonalDictionary = "personal_dictionary/"
    const val BlockedWords = "blocked_words"
    const val Languages = "languages"
    const val LanguagesList = "languages_list"
    const val Subtype = "subtype/"
    const val Layouts = "layouts"
    const val Dictionaries = "dictionaries"
    const val CustomAIKeys = "custom_ai_keys"
    const val CustomAIKeyConfig = "custom_ai_key_config/"
    const val TextExpander = "text_expander"
    const val BackgroundServices = "background_services"
    const val OfflineVoice = "offline_voice"
    const val Translation = "translation"
    const val Handwriting = "handwriting"
    const val OCR = "ocr"
    const val Sound = "sound"
    val navTarget = MutableStateFlow(Settings)

    // Use SupervisorJob so a cancellation in one navigation hop
    // doesn't tear down the rest of the settings UI. Dispatchers.Default
    // is fine here because we're only updating a MutableStateFlow.
    private val navScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    fun navigateTo(target: String) {
        if (navTarget.value == target) {
            // triggers recompose twice, but that's ok as it's a rare event
            navTarget.value = Settings
            navScope.launch { delay(10); navTarget.value = target }
        } else
            navTarget.value = target
        navScope.launch { delay(50); navTarget.value = Settings }
    }
}
