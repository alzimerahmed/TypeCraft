// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.Manifest
import android.provider.Settings as AndroidSettings
import alzimerahmed84.keyboard.latin.settings.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import alzimerahmed84.keyboard.latin.BuildConfig
import alzimerahmed84.keyboard.latin.R
import alzimerahmed84.keyboard.latin.permissions.PermissionsUtil
import alzimerahmed84.keyboard.latin.settings.Defaults
import alzimerahmed84.keyboard.latin.utils.JniUtils
import alzimerahmed84.keyboard.latin.utils.SubtypeSettings
import alzimerahmed84.keyboard.settings.dialogs.MultiListPickerDialog
import alzimerahmed84.keyboard.settings.WithSmallTitle
import alzimerahmed84.keyboard.settings.DropDownField
import alzimerahmed84.keyboard.latin.utils.SubtypeLocaleUtils.displayName
import alzimerahmed84.keyboard.latin.utils.locale
import alzimerahmed84.keyboard.latin.utils.UncachedInputMethodManagerUtils
import alzimerahmed84.keyboard.latin.utils.getActivity
import alzimerahmed84.keyboard.latin.utils.prefs
import alzimerahmed84.keyboard.settings.preferences.ListPreference
import alzimerahmed84.keyboard.settings.preferences.LoadEmojiLibPreference
import alzimerahmed84.keyboard.settings.preferences.LoadGestureLibPreference
import alzimerahmed84.keyboard.settings.preferences.MultiSliderPreference
import alzimerahmed84.keyboard.settings.preferences.Preference
import alzimerahmed84.keyboard.settings.preferences.SwitchPreference
import alzimerahmed84.keyboard.settings.preferences.TextInputPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WelcomeWizard(
    close: () -> Unit,
    finish: () -> Unit
) {
    val ctx = LocalContext.current
    val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    fun determineStep(): Int = when {
        !UncachedInputMethodManagerUtils.isThisImeEnabled(ctx, imm) -> 0
        !UncachedInputMethodManagerUtils.isThisImeCurrent(ctx, imm) -> 2
        else -> 3
    }
    var step by rememberSaveable { mutableIntStateOf(determineStep()) }
    var requiresRestart by rememberSaveable { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(step) {
        if (step == 2)
            scope.launch {
                while (step == 2 && !UncachedInputMethodManagerUtils.isThisImeCurrent(ctx, imm)) {
                    delay(50)
                }
                step = 3
            }
    }
    val useWideLayout = isWideScreen()
    val appName = stringResource(ctx.applicationInfo.labelRes)
    
    @Composable fun bigText() {
        val resource = if (step == 0) R.string.setup_welcome_title else R.string.setup_steps_title
        Column(Modifier.padding(bottom = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(resource, appName),
                style = MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (JniUtils.sHaveNativeGestureLib && step == 0) {
                Text(
                    stringResource(R.string.setup_welcome_additional_description),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }

    @Composable
    fun ColumnScope.Step(
        currentStep: Int, 
        title: String, 
        instruction: String, 
        actionText: String, 
        icon: Painter, 
        action: () -> Unit, 
        onBack: (() -> Unit)? = null, 
        content: @Composable () -> Unit = {}
    ) {
        // Progress indicator
        Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (i in 1..9) {
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .background(
                            if (i <= currentStep) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surfaceVariant,
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
        
        androidx.compose.material3.ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(instruction, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                Spacer(Modifier.height(16.dp))
                content()
                
                Spacer(Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onBack != null) {
                        androidx.compose.material3.FilledTonalButton(onClick = onBack) {
                            Text(stringResource(R.string.setup_step_previous))
                        }
                    } else {
                        Spacer(Modifier.weight(0.1f)) // Placeholder to maintain spacing
                    }
                    
                    Spacer(Modifier.weight(1f))
                    
                    androidx.compose.material3.FilledTonalButton(onClick = action) {
                        Icon(icon, null, Modifier.padding(end = 8.dp).size(20.dp))
                        Text(actionText)
                    }
                }
            }
        }
    }

    @Composable fun steps() {
        if (step == 0)
            Step0 { step = 1 }
        else
            Column(Modifier.verticalScroll(rememberScrollState())) {
                val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                    step = determineStep()
                }
                if (step == 1) {
                    Step(
                        1,
                        stringResource(R.string.setup_step1_title, appName),
                        stringResource(R.string.setup_step1_instruction, appName),
                        stringResource(R.string.setup_step1_action),
                        painterResource(R.drawable.ic_setup_key),
                        {
                            val intent = Intent()
                            intent.action = AndroidSettings.ACTION_INPUT_METHOD_SETTINGS
                            intent.addCategory(Intent.CATEGORY_DEFAULT)
                            launcher.launch(intent)
                        },
                        null
                    )
                } else if (step == 2) {
                    Step(
                        2,
                        stringResource(R.string.setup_step2_title, appName),
                        stringResource(R.string.setup_step2_instruction, appName),
                        stringResource(R.string.setup_step2_action),
                        painterResource(R.drawable.ic_setup_select),
                        { imm.showInputMethodPicker() },
                        null
                    )
                } else if (step == 3) {
                    var showDialog by remember { mutableStateOf(false) }
                    val allSubtypes = remember { SubtypeSettings.getAllAvailableSubtypes() }
                    var enabledSubtypes by remember { mutableStateOf(SubtypeSettings.getEnabledSubtypes(true)) }

                    LaunchedEffect(Unit) {
                        if (SubtypeSettings.getEnabledSubtypes(false).isEmpty()) {
                            enabledSubtypes.forEach { subtype ->
                                SubtypeSettings.addEnabledSubtype(ctx.prefs(), subtype)
                            }
                            enabledSubtypes = SubtypeSettings.getEnabledSubtypes(true)
                        }
                    }

                    Step(
                        3,
                        stringResource(R.string.setup_step_languages_title),
                        stringResource(R.string.setup_step_languages_instruction),
                        stringResource(R.string.setup_next_action),
                        painterResource(R.drawable.sym_keyboard_language_switch),
                        { step++ },
                        { step-- }
                    ) {
                        WithSmallTitle(stringResource(R.string.setup_languages_section)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    stringResource(R.string.setup_languages_enabled),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))
                                enabledSubtypes.forEach { subtype ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_setup_check),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp).padding(end = 8.dp)
                                        )
                                        Text(
                                            text = subtype.displayName(),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                androidx.compose.material3.Button(
                                    onClick = { showDialog = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.setup_languages_choose))
                                }
                            }
                        }

                        if (showDialog) {
                            MultiListPickerDialog(
                                onDismissRequest = { showDialog = false },
                                items = allSubtypes,
                                initialSelection = enabledSubtypes,
                                onConfirmed = { selected ->
                                    selected.forEach { subtype ->
                                        if (subtype !in enabledSubtypes) {
                                            SubtypeSettings.addEnabledSubtype(ctx.prefs(), subtype)
                                        }
                                    }
                                    enabledSubtypes.toList().forEach { subtype ->
                                        if (subtype !in selected && selected.isNotEmpty()) {
                                            SubtypeSettings.removeEnabledSubtype(ctx, subtype)
                                        }
                                    }
                                    enabledSubtypes = SubtypeSettings.getEnabledSubtypes(true)
                                    showDialog = false
                                },
                                getItemName = { it.displayName() }
                            )
                        }
                    }
                } else if (step == 4) {
                    Step(
                        4,
                        stringResource(R.string.setup_step_libraries_title),
                        stringResource(R.string.setup_step_libraries_instruction),
                        stringResource(R.string.setup_next_action),
                        painterResource(R.drawable.sym_keyboard_language_switch),
                        { step++ },
                        { step-- }
                    ) {
                        val trigger = refreshTrigger // Force recomposition
                        val locale = alzimerahmed84.keyboard.latin.RichInputMethodManager.getInstance().currentSubtype.locale
                        val emojiLibInstalled = java.io.File(alzimerahmed84.keyboard.latin.utils.DictionaryInfoUtils.getCacheDirectoryForLocale(locale, ctx), "emoji_${locale.language}.dict").exists()
                        val gestureLibInstalled = java.io.File(ctx.filesDir, "libjni_latinime.so").exists() || JniUtils.sHaveNativeGestureLib

                        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)) {
                            LoadEmojiLibPreference(
                                title = stringResource(R.string.setup_emoji_dictionary),
                                onSuccess = { refreshTrigger++ }
                            )
                            if (emojiLibInstalled) {
                                Icon(painterResource(R.drawable.ic_setup_check), null, Modifier.align(Alignment.CenterEnd).padding(end = 16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)) {
                            LoadGestureLibPreference(
                                title = stringResource(R.string.setup_gesture_library),
                                restartOnSuccess = false,
                                onSuccess = { 
                                    requiresRestart = true
                                    refreshTrigger++ 
                                }
                            )
                            if (gestureLibInstalled) {
                                Icon(painterResource(R.drawable.ic_setup_check), null, Modifier.align(Alignment.CenterEnd).padding(end = 16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        var gestureEnabled by remember {
                            mutableStateOf(ctx.prefs().getBoolean(Settings.PREF_GESTURE_INPUT, alzimerahmed84.keyboard.latin.settings.Defaults.PREF_GESTURE_INPUT))
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.setup_gesture_enable),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stringResource(R.string.setup_gesture_enable_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            androidx.compose.material3.Switch(
                                checked = gestureEnabled,
                                onCheckedChange = { checked ->
                                    gestureEnabled = checked
                                    ctx.prefs().edit().putBoolean(Settings.PREF_GESTURE_INPUT, checked).apply()
                                }
                            )
                        }
                    }
                } else if (step == 5) {
                    val stepTitle = stringResource(
                        if (BuildConfig.FLAVOR == "offline") R.string.setup_step_ai_offline_title
                        else R.string.setup_step_ai_title
                    )
                    val stepInstruction = stringResource(
                        if (BuildConfig.FLAVOR == "offline") R.string.setup_step_ai_offline_instruction
                        else R.string.setup_step_ai_instruction
                    )

                    Step(
                        5,
                        stepTitle,
                        stepInstruction,
                        stringResource(R.string.setup_next_action),
                        painterResource(R.drawable.sym_keyboard_language_switch),
                        { step++ },
                        { step-- }
                    ) {
                        if (BuildConfig.FLAVOR == "standard" || BuildConfig.FLAVOR == "standardfull") {
                            val service = remember { alzimerahmed84.keyboard.latin.utils.ProofreadService(ctx) }
                            var currentProvider by remember { mutableStateOf(service.getProvider()) }
                            val aiConfigured = when (currentProvider) {
                                alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.GEMINI -> service.hasApiKey()
                                alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.GROQ -> service.getGroqToken() != null
                                alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.OPENAI -> service.getHuggingFaceToken() != null
                            }

                            Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)) {
                                Column {
                                    alzimerahmed84.keyboard.settings.Setting(ctx, alzimerahmed84.keyboard.settings.SettingsWithoutKey.AI_PROVIDER, R.string.ai_provider_title, R.string.ai_provider_summary) { setting ->
                                        ListPreference(setting, listOf(
                                            ctx.getString(R.string.ai_provider_huggingface) to alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.GROQ.name,
                                            ctx.getString(R.string.ai_provider_gemini) to alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.GEMINI.name,
                                            ctx.getString(R.string.ai_provider_openai) to alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.OPENAI.name
                                        ), currentProvider.name, onChanged = {
                                            val newProvider = alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.valueOf(it)
                                            service.setProvider(newProvider)
                                            currentProvider = newProvider
                                            refreshTrigger++
                                        })
                                    }.Preference()
                                    
                                    when (currentProvider) {
                                        alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.GEMINI -> {
                                            alzimerahmed84.keyboard.settings.Setting(ctx, alzimerahmed84.keyboard.settings.SettingsWithoutKey.GEMINI_API_KEY, R.string.gemini_api_key_title, R.string.gemini_api_key_summary) { setting ->
                                                TextInputPreference(setting, service.getApiKey() ?: "", onConfirmed = {
                                                    service.setApiKey(it)
                                                    refreshTrigger++
                                                })
                                            }.Preference()
                                        }
                                        alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.GROQ -> {
                                            alzimerahmed84.keyboard.settings.Setting(ctx, alzimerahmed84.keyboard.settings.SettingsWithoutKey.GROQ_TOKEN, R.string.groq_token_title, R.string.groq_token_summary) { setting ->
                                                TextInputPreference(setting, service.getGroqToken() ?: "", onConfirmed = {
                                                    service.setGroqToken(it)
                                                    refreshTrigger++
                                                })
                                            }.Preference()
                                        }
                                        alzimerahmed84.keyboard.latin.utils.ProofreadService.AIProvider.OPENAI -> {
                                            alzimerahmed84.keyboard.settings.Setting(ctx, alzimerahmed84.keyboard.settings.SettingsWithoutKey.HUGGINGFACE_TOKEN, R.string.huggingface_token_title, R.string.huggingface_token_summary) { setting ->
                                                TextInputPreference(setting, service.getHuggingFaceToken() ?: "", onConfirmed = {
                                                    service.setHuggingFaceToken(it)
                                                    refreshTrigger++
                                                })
                                            }.Preference()
                                        }
                                    }
                                }
                                if (aiConfigured) {
                                    Icon(painterResource(R.drawable.ic_setup_check), null, Modifier.align(Alignment.CenterEnd).padding(end = 16.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        } else if (BuildConfig.FLAVOR == "offline") {
                            val service = remember { alzimerahmed84.keyboard.latin.utils.ProofreadService(ctx) }
                            var modelPath by remember { mutableStateOf(service.getModelPath()) }
                            val hasPlugin = alzimerahmed84.keyboard.latin.ai.OfflineAiLoader.hasPlugin(ctx)
                            val pluginVersion = alzimerahmed84.keyboard.latin.ai.OfflineAiLoader.getPluginVersion(ctx)

                            val modelLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.OpenDocument()
                            ) { uri ->
                                uri?.let {
                                    try {
                                        ctx.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    } catch (e: Exception) {
                                        android.util.Log.e("WelcomeWizard", "Failed to take persistable permission", e)
                                    }
                                    service.setModelPath(it.toString())
                                    modelPath = it.toString()
                                    refreshTrigger++
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)) {
                                    alzimerahmed84.keyboard.settings.preferences.LoadOfflineAiPluginPreference(
                                        title = stringResource(R.string.setup_offline_ai_plugin),
                                        summary = if (hasPlugin)
                                            stringResource(
                                                R.string.setup_offline_ai_plugin_active,
                                                pluginVersion ?: "1.0"
                                            )
                                        else stringResource(R.string.setup_offline_ai_plugin_missing),
                                        icon = R.drawable.ic_proofread,
                                        onSuccess = { refreshTrigger++ }
                                    )
                                    if (hasPlugin) {
                                        Icon(painterResource(R.drawable.ic_setup_check), null, Modifier.align(Alignment.CenterEnd).padding(end = 16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)) {
                                    Column {
                                        Preference(
                                            name = stringResource(R.string.setup_gguf_model),
                                            description = if (modelPath != null) service.getModelName()
                                                else stringResource(R.string.setup_gguf_model_desc),
                                            onClick = { modelLauncher.launch(arrayOf("application/octet-stream", "*/*")) },
                                            icon = R.drawable.ic_settings_advanced
                                        )
                                        if (modelPath != null) {
                                            Preference(
                                                name = stringResource(R.string.setup_remove_model),
                                                description = stringResource(R.string.setup_remove_model_desc),
                                                onClick = {
                                                    service.unloadModel()
                                                    service.setModelPath(null)
                                                    modelPath = null
                                                    refreshTrigger++
                                                }
                                            )
                                        }
                                    }
                                    if (modelPath != null) {
                                        Icon(painterResource(R.drawable.ic_setup_check), null, Modifier.align(Alignment.CenterEnd).padding(end = 16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_setup_check),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    Text(
                                        stringResource(R.string.setup_offline_lite_note),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                } else if (step == 6) {
                    Step(
                        6,
                        stringResource(R.string.setup_step_floating_title),
                        stringResource(R.string.setup_step_floating_instruction),
                        stringResource(R.string.setup_next_action),
                        painterResource(R.drawable.sym_keyboard_language_switch),
                        { step++ },
                        { step-- }
                    ) {
                        val trigger = refreshTrigger
                        val canDrawOverlays = AndroidSettings.canDrawOverlays(ctx)
                        val overlayLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { refreshTrigger++ }
                        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium).clickable {
                            if (!canDrawOverlays) {
                                val intent = Intent(AndroidSettings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${ctx.packageName}"))
                                overlayLauncher.launch(intent)
                            }
                        }.padding(16.dp)) {
                            if (!canDrawOverlays) {
                                Text(
                                    stringResource(R.string.setup_overlay_permission_required),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(R.drawable.ic_setup_check), null, Modifier.padding(end = 8.dp), tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        stringResource(R.string.setup_overlay_permission_granted),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                } else if (step == 7) {
                    Step(
                        7,
                        stringResource(R.string.setup_step_screenshots_title),
                        stringResource(R.string.setup_step_screenshots_instruction),
                        stringResource(R.string.setup_next_action),
                        painterResource(R.drawable.sym_keyboard_language_switch),
                        { step++ },
                        { step-- }
                    ) {
                        val trigger = refreshTrigger
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        val granted = PermissionsUtil.checkAllPermissionsGranted(ctx.getActivity(), permission)
                        val screenshotsEnabled = ctx.prefs().getBoolean(Settings.PREF_SUGGEST_SCREENSHOTS, Defaults.PREF_SUGGEST_SCREENSHOTS)

                        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)) {
                            alzimerahmed84.keyboard.settings.Setting(ctx, Settings.PREF_SUGGEST_SCREENSHOTS, R.string.suggest_screenshots, R.string.suggest_screenshots_summary) { setting ->
                                val activity = ctx.getActivity() ?: return@Setting
                                var isGranted by remember { mutableStateOf(PermissionsUtil.checkAllPermissionsGranted(activity, permission)) }
                                val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                                    isGranted = it
                                    if (isGranted)
                                        activity.prefs().edit { putBoolean(setting.key, true) }
                                    refreshTrigger++
                                }
                                SwitchPreference(setting, Defaults.PREF_SUGGEST_SCREENSHOTS, allowCheckedChange = {
                                    if (it && !isGranted) {
                                        permLauncher.launch(permission)
                                        false
                                    } else true
                                })
                            }.Preference()
                        }
                    }
                } else if (step == 8) {
                    Step(
                        8,
                        stringResource(R.string.setup_step_height_title),
                        stringResource(R.string.setup_step_height_instruction),
                        stringResource(R.string.setup_next_action),
                        painterResource(R.drawable.sym_keyboard_language_switch),
                        { step++ },
                        { step-- }
                    ) {
                        val heightSet = ctx.prefs().contains(Settings.PREF_KEYBOARD_HEIGHT_SCALE_PREFIX + "0")

                        Box(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)) {
                            alzimerahmed84.keyboard.settings.Setting(ctx, Settings.PREF_KEYBOARD_HEIGHT_SCALE_PREFIX, R.string.prefs_keyboard_height_scale) { setting ->
                                MultiSliderPreference(
                                    name = setting.title,
                                    baseKey = setting.key,
                                    dimensions = listOf(stringResource(R.string.landscape)),
                                    defaults = Defaults.PREF_KEYBOARD_HEIGHT_SCALE,
                                    range = 0.3f..1.5f,
                                    description = { "${(100 * it).toInt()}%" }
                                ) {}
                            }.Preference()
                            
                            if (heightSet) {
                                Icon(painterResource(R.drawable.ic_setup_check), null, Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                } else { // step 9
                    Step(
                        9,
                        stringResource(R.string.setup_step3_title),
                        stringResource(R.string.setup_step3_instruction, appName),
                        stringResource(R.string.setup_finish_action),
                        painterResource(R.drawable.ic_setup_check),
                        {
                            if (SubtypeSettings.getEnabledSubtypes(false).isEmpty()) {
                                SubtypeSettings.getEnabledSubtypes(true).forEach { subtype ->
                                    SubtypeSettings.addEnabledSubtype(ctx.prefs(), subtype)
                                }
                            }
                            finish()
                            if (requiresRestart) {
                                Runtime.getRuntime().exit(0)
                            }
                        },
                        { step-- }
                    )
                }
            }
    }
    Surface(color = MaterialTheme.colorScheme.background) {
        Box(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (useWideLayout)
                Row {
                    Box(Modifier.weight(0.4f)) {
                        bigText()
                    }
                    Box(Modifier.weight(0.6f)) {
                        steps()
                    }
                }
            else
                Column {
                    bigText()
                    steps()
                }
        }
    }
}

@Composable
fun Step0(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painterResource(R.drawable.setup_welcome_image), 
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth().weight(1f)
        )
        
        Spacer(Modifier.height(16.dp))
        
        androidx.compose.material3.Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(
                stringResource(R.string.setup_start_action),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    Theme(previewDark) {
        Surface {
            WelcomeWizard({}) {  }
        }
    }
}

@Preview(
    device = "spec:orientation=landscape,width=400dp,height=780dp"
)
@Composable
private fun WidePreview() {
    Theme(previewDark) {
        Surface {
            WelcomeWizard({}) {  }
        }
    }
}
