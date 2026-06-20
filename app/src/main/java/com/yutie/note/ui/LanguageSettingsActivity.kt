package com.yutie.note.ui

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.ui.custom.CustomTitleBar
import java.util.*

/**
 * 语言数据类
 */
data class LanguageOption(
    val code: String,
    val name: String,
    val nativeName: String
)

/**
 * 语言设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // 获取当前语言
    val currentLocale = getSystemLanguage(context)
    
    // 语言选项列表
    val languages = listOf(
        LanguageOption("auto", stringResource(R.string.str_language_auto), ""),
        LanguageOption("zh", "简体中文", "Chinese"),
        LanguageOption("en", "English", "English")
    )
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTitleBar(
            title = stringResource(R.string.str_language_select),
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(languages) { language ->
                LanguageItem(
                    language = language,
                    isSelected = isLanguageSelected(currentLocale, language.code),
                    onClick = {
                        setAppLanguage(context, language.code)
                        // 重启 Activity 以应用新语言
                        activity?.recreate()
                    }
                )
            }
        }
    }
}

/**
 * 语言选项 Item
 */
@Composable
fun LanguageItem(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = language.nativeName.ifBlank { language.name },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (language.nativeName.isNotBlank() && language.code != "auto") {
                    Text(
                        text = language.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 获取当前应用语言
 */
fun getSystemLanguage(context: Context): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val localeList = context.getSystemService(LocaleManager::class.java).applicationLocales
        if (localeList.isEmpty) {
            "auto"
        } else {
            localeList.get(0)?.language ?: "auto"
        }
    } else {
        val locale = AppCompatDelegate.getApplicationLocales()
        if (locale.isEmpty) {
            "auto"
        } else {
            locale.get(0)?.language ?: "auto"
        }
    }
}

/**
 * 判断语言是否被选中
 */
fun isLanguageSelected(currentLocale: String, languageCode: String): Boolean {
    return if (languageCode == "auto") {
        currentLocale == "auto"
    } else {
        currentLocale == languageCode
    }
}

/**
 * 设置应用语言
 */
fun setAppLanguage(context: Context, languageCode: String) {
    val localeList = if (languageCode == "auto") {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(languageCode)
    }
    
    // 使用 AppCompatDelegate 设置语言
    AppCompatDelegate.setApplicationLocales(localeList)
    
    // 保存语言设置到 SharedPreferences
    val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    prefs.edit().putString("app_language", languageCode).apply()
    
    println("=== 语言设置 ===")
    println("设置语言：$languageCode")
    println("当前语言：${AppCompatDelegate.getApplicationLocales()}")
}
