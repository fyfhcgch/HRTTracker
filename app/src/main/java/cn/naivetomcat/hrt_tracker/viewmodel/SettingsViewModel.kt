package cn.naivetomcat.hrt_tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import cn.naivetomcat.hrt_tracker.data.ColorTheme
import cn.naivetomcat.hrt_tracker.data.SettingsDataStore
import cn.naivetomcat.hrt_tracker.data.ThemeMode
import cn.naivetomcat.hrt_tracker.data.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 设置 ViewModel
 * 管理用户设置和偏好
 */
class SettingsViewModel(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    /**
     * 用户设置状态
     */
    val userSettings: StateFlow<UserSettings> = settingsDataStore.userSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserSettings()
        )

    /**
     * 更新体重
     */
    fun updateBodyWeight(weight: Double) {
        viewModelScope.launch {
            settingsDataStore.updateBodyWeight(weight)
        }
    }

    /**
     * 更新主题模式
     */
    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsDataStore.updateThemeMode(mode)
        }
    }

    /**
     * 更新颜色主题
     */
    fun updateColorTheme(theme: ColorTheme) {
        viewModelScope.launch {
            settingsDataStore.updateColorTheme(theme)
        }
    }
}

/**
 * SettingsViewModel 工厂类
 */
class SettingsViewModelFactory(
    private val settingsDataStore: SettingsDataStore
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(settingsDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
