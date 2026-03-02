package cn.naivetomcat.hrt_tracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cn.naivetomcat.hrt_tracker.data.ColorTheme
import cn.naivetomcat.hrt_tracker.data.ThemeMode
import cn.naivetomcat.hrt_tracker.data.UserSettings
import cn.naivetomcat.hrt_tracker.ui.theme.HRTTrackerTheme

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    onBodyWeightChange: (Double) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onColorThemeChange: (ColorTheme) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 体重输入
            BodyWeightSection(
                bodyWeight = settings.bodyWeight,
                onBodyWeightChange = onBodyWeightChange
            )

            // 夜间模式选择
            ThemeModeSection(
                currentMode = settings.themeMode,
                onModeChange = onThemeModeChange
            )

            // 颜色主题选择
            ColorThemeSection(
                currentTheme = settings.colorTheme,
                onThemeChange = onColorThemeChange
            )
        }
    }
}

/**
 * 体重输入部分
 */
@Composable
private fun BodyWeightSection(
    bodyWeight: Double,
    onBodyWeightChange: (Double) -> Unit
) {
    var weightText by remember(bodyWeight) { mutableStateOf(bodyWeight.toString()) }
    var isError by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "体重",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "用于血药浓度计算",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        OutlinedTextField(
            value = weightText,
            onValueChange = { newValue ->
                weightText = newValue
                val weight = newValue.toDoubleOrNull()
                if (weight != null && weight > 0 && weight <= 300) {
                    isError = false
                    onBodyWeightChange(weight)
                } else {
                    isError = true
                }
            },
            label = { Text("体重 (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = isError,
            supportingText = {
                if (isError) {
                    Text("请输入有效的体重 (0-300 kg)")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 夜间模式选择部分
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeSection(
    currentMode: ThemeMode,
    onModeChange: (ThemeMode) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "夜间模式",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeMode.entries.forEachIndexed { index, mode ->
                val label = when (mode) {
                    ThemeMode.LIGHT -> "浅色"
                    ThemeMode.DARK -> "深色"
                    ThemeMode.SYSTEM -> "系统默认"
                }
                val description = when (mode) {
                    ThemeMode.LIGHT -> "始终使用浅色主题"
                    ThemeMode.DARK -> "始终使用深色主题"
                    ThemeMode.SYSTEM -> "跟随系统设置"
                }
                val icon = when (mode) {
                    ThemeMode.LIGHT -> Icons.Outlined.LightMode
                    ThemeMode.DARK -> Icons.Outlined.DarkMode
                    ThemeMode.SYSTEM -> Icons.Outlined.PhoneAndroid
                }

                SegmentedListItem(
                    selected = currentMode == mode,
                    onClick = { onModeChange(mode) },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = ThemeMode.entries.size
                    ),
                    colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = null
                        )
                    },
                    supportingContent = { Text(description) }
                ) {
                    Text(label)
                }
            }
        }
    }
}

/**
 * 颜色主题选择部分
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorThemeSection(
    currentTheme: ColorTheme,
    onThemeChange: (ColorTheme) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "颜色主题",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ColorTheme.entries.forEachIndexed { index, theme ->
                val label = when (theme) {
                    ColorTheme.DYNAMIC -> "动态着色"
                    ColorTheme.BUILTIN -> "内置配色"
                }
                val description = when (theme) {
                    ColorTheme.DYNAMIC -> "跟随系统动态着色 (Android 12+)"
                    ColorTheme.BUILTIN -> "使用应用内置配色方案"
                }
                val icon = when (theme) {
                    ColorTheme.DYNAMIC -> Icons.Outlined.ColorLens
                    ColorTheme.BUILTIN -> Icons.Outlined.Palette
                }

                SegmentedListItem(
                    selected = currentTheme == theme,
                    onClick = { onThemeChange(theme) },
                    shapes = ListItemDefaults.segmentedShapes(
                        index = index,
                        count = ColorTheme.entries.size
                    ),
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    leadingContent = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null
                        )
                    },
                    trailingContent = {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = null
                        )
                    },
                    supportingContent = { Text(description) }
                ) {
                    Text(label)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    HRTTrackerTheme {
        SettingsScreen(
            settings = UserSettings(
                bodyWeight = 55.0,
                themeMode = ThemeMode.SYSTEM,
                colorTheme = ColorTheme.DYNAMIC
            ),
            onBodyWeightChange = {},
            onThemeModeChange = {},
            onColorThemeChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    HRTTrackerTheme(themeMode = ThemeMode.DARK) {
        SettingsScreen(
            settings = UserSettings(
                bodyWeight = 60.0,
                themeMode = ThemeMode.DARK,
                colorTheme = ColorTheme.BUILTIN
            ),
            onBodyWeightChange = {},
            onThemeModeChange = {},
            onColorThemeChange = {}
        )
    }
}
