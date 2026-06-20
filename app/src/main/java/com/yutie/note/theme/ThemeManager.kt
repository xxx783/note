package com.yutie.note.theme

import android.content.Context
import android.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yutie.note.ui.theme.LocalNoteTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/**
 * 主题配置数据类
 */
data class ThemeConfig(
    val primaryColor: String = "#6200EE",        // 主色调
    val backgroundColor: String = "#FFFFFF",      // 背景色
    val surfaceColor: String = "#FFFFFF",         // 表面颜色
    val fontSize: Float = 16f,                    // 字体大小 (sp)
    val fontFamily: String = "default",           // 字体类型：default, serif, monospace
    val isDarkMode: Boolean = false,              // 是否深色模式
    val isCustomTheme: Boolean = false            // 是否自定义主题
)

/**
 * 主题管理器
 * 单例，负责加载和保存主题配置
 */
object ThemeManager {
    
    // DataStore 键
    private val THEME_CONFIG_KEY = stringPreferencesKey("theme_config")
    
    // Context 的扩展属性，获取 DataStore
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")
    
    /**
     * 获取主题配置（Flow）
     */
    suspend fun getThemeConfigFlow(context: Context) = context.dataStore.data.map { preferences ->
        val jsonStr = preferences[THEME_CONFIG_KEY]
        if (jsonStr != null) {
            parseThemeConfig(JSONObject(jsonStr))
        } else {
            ThemeConfig()
        }
    }
    
    /**
     * 获取当前主题配置（挂起函数）
     */
    suspend fun getThemeConfig(context: Context): ThemeConfig {
        return getThemeConfigFlow(context).first()
    }
    
    /**
     * 保存主题配置
     */
    suspend fun saveThemeConfig(context: Context, config: ThemeConfig) {
        val json = themeConfigToJson(config)
        context.dataStore.edit { preferences ->
            preferences[THEME_CONFIG_KEY] = json.toString()
        }
    }
    
    /**
     * 应用主题配置
     * 注意：这个方法需要重建 Activity 才能生效
     */
    @Suppress("UNUSED_PARAMETER")
    fun applyTheme(context: Context, config: ThemeConfig) {
        // 这里主要是保存配置，实际的主题应用通过 CompositionLocal 实现
        // 在 Theme 中读取配置并应用
    }
    
    /**
     * 重置为主题默认
     */
    suspend fun resetToDefault(context: Context) {
        saveThemeConfig(context, ThemeConfig())
    }
    
    /**
     * 从 JSON 解析 ThemeConfig
     */
    private fun parseThemeConfig(json: JSONObject): ThemeConfig {
        return ThemeConfig(
            primaryColor = json.optString("primaryColor", "#6200EE"),
            backgroundColor = json.optString("backgroundColor", "#FFFFFF"),
            surfaceColor = json.optString("surfaceColor", "#FFFFFF"),
            fontSize = json.optDouble("fontSize", 16.0).toFloat(),
            fontFamily = json.optString("fontFamily", "default"),
            isDarkMode = json.optBoolean("isDarkMode", false),
            isCustomTheme = json.optBoolean("isCustomTheme", false)
        )
    }
    
    /**
     * 将 ThemeConfig 转为 JSON
     */
    private fun themeConfigToJson(config: ThemeConfig): JSONObject {
        return JSONObject().apply {
            put("primaryColor", config.primaryColor)
            put("backgroundColor", config.backgroundColor)
            put("surfaceColor", config.surfaceColor)
            put("fontSize", config.fontSize.toDouble())
            put("fontFamily", config.fontFamily)
            put("isDarkMode", config.isDarkMode)
            put("isCustomTheme", config.isCustomTheme)
        }
    }
    
    /**
     * 将颜色字符串转为 Int (ARGB)
     */
    fun colorStringToInt(colorStr: String): Int {
        return try {
            Color.parseColor(colorStr)
        } catch (e: Exception) {
            Color.parseColor("#6200EE") // 默认紫色
        }
    }
}
