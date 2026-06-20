package com.yutie.note.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

/**
 * 语言管理工具类
 * 支持 25 种语言的动态切换
 */
object LanguageManager {
    
    // 支持的语言列表
    data class LanguageInfo(
        val code: String,
        val name: String,
        val nativeName: String
    )
    
    val SUPPORTED_LANGUAGES = listOf(
        LanguageInfo("auto", "跟随系统", "System"),
        LanguageInfo("zh", "简体中文", "简体中文"),
        LanguageInfo("zh-TW", "繁體中文", "繁體中文"),
        LanguageInfo("en", "English", "English"),
        LanguageInfo("ja", "日本語", "日本語"),
        LanguageInfo("ko", "한국어", "한국어"),
        LanguageInfo("fr", "Français", "Français"),
        LanguageInfo("de", "Deutsch", "Deutsch"),
        LanguageInfo("es", "Español", "Español"),
        LanguageInfo("it", "Italiano", "Italiano"),
        LanguageInfo("pt", "Português", "Português"),
        LanguageInfo("ru", "Русский", "Русский"),
        LanguageInfo("ar", "العربية", "العربية"),
        LanguageInfo("hi", "हिन्दी", "हिन्दी"),
        LanguageInfo("th", "ไทย", "ไทย"),
        LanguageInfo("vi", "Tiếng Việt", "Tiếng Việt"),
        LanguageInfo("id", "Bahasa Indonesia", "Bahasa Indonesia"),
        LanguageInfo("ms", "Bahasa Melayu", "Bahasa Melayu"),
        LanguageInfo("tr", "Türkçe", "Türkçe"),
        LanguageInfo("pl", "Polski", "Polski"),
        LanguageInfo("nl", "Nederlands", "Nederlands"),
        LanguageInfo("sv", "Svenska", "Svenska"),
        LanguageInfo("da", "Dansk", "Dansk"),
        LanguageInfo("fi", "Suomi", "Suomi"),
        LanguageInfo("no", "Norsk", "Norsk"),
        LanguageInfo("cs", "Čeština", "Čeština"),
        LanguageInfo("el", "Ελληνικά", "Ελληνικά")
    )
    
    private const val PREF_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"
    
    /**
     * 获取当前选中的语言
     */
    fun getSelectedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "auto") ?: "auto"
    }
    
    /**
     * 设置语言
     */
    fun setLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
        updateLocale(context, language)
    }
    
    /**
     * 更新应用语言
     */
    fun updateLocale(context: Context, languageCode: String) {
        val locale = getLocaleFromCode(languageCode)
        updateResources(context, locale)
    }
    
    /**
     * 根据语言代码获取 Locale
     */
    private fun getLocaleFromCode(languageCode: String): Locale {
        return when (languageCode) {
            "auto" -> Locale.getDefault()
            "zh" -> Locale.CHINA
            "zh-TW" -> Locale.TRADITIONAL_CHINESE
            "en" -> Locale.US
            "ja" -> Locale.JAPAN
            "ko" -> Locale.KOREA
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "es" -> Locale("es", "ES")
            "it" -> Locale.ITALIAN
            "pt" -> Locale("pt", "BR")
            "ru" -> Locale("ru", "RU")
            "ar" -> Locale("ar", "SA")
            "hi" -> Locale("hi", "IN")
            "th" -> Locale("th", "TH")
            "vi" -> Locale("vi", "VN")
            "id" -> Locale("in", "ID")
            "ms" -> Locale("ms", "MY")
            "tr" -> Locale("tr", "TR")
            "pl" -> Locale("pl", "PL")
            "nl" -> Locale("nl", "NL")
            "sv" -> Locale("sv", "SE")
            "da" -> Locale("da", "DK")
            "fi" -> Locale("fi", "FI")
            "no" -> Locale("no", "NO")
            "cs" -> Locale("cs", "CZ")
            "el" -> Locale("el", "GR")
            else -> Locale.getDefault()
        }
    }
    
    /**
     * 更新资源配置
     */
    private fun updateResources(context: Context, locale: Locale) {
        Locale.setDefault(locale)
        
        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocale(locale)
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }
    
    /**
     * 初始化应用语言（在 Application 中调用）
     */
    fun init(context: Context) {
        val languageCode = getSelectedLanguage(context)
        updateLocale(context, languageCode)
    }
}
