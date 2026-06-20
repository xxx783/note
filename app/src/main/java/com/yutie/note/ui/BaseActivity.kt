package com.yutie.note.ui

import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * 所有 Activity 的基类
 * 自动应用语言设置
 */
abstract class BaseActivity : AppCompatActivity() {
    
    override fun attachBaseContext(newBase: Context) {
        val language = getSavedLanguage(newBase)
        val context = if (language == "auto") {
            newBase
        } else {
            updateContextLanguage(newBase, language)
        }
        super.attachBaseContext(context)
    }
    
    /**
     * 获取保存的语言设置
     */
    private fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getString("app_language", "auto") ?: "auto"
    }
    
    /**
     * 更新 Context 的语言
     */
    private fun updateContextLanguage(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val configuration = context.resources.configuration
            configuration.setLocales(LocaleList(locale))
            context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            val configuration = context.resources.configuration
            configuration.setLocale(locale)
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }
}
