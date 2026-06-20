package com.yutie.note.ui.theme

import android.app.Activity
import android.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.yutie.note.theme.ThemeManager
import com.yutie.note.utils.SPUtils
import com.yutie.note.constant.AppConstants
import kotlinx.coroutines.runBlocking

/**
 * 根据自定义主题配置创建 ColorScheme
 */
private fun createCustomColorScheme(
    primaryColor: String,
    backgroundColor: String,
    surfaceColor: String,
    isDarkMode: Boolean
) = if (isDarkMode) {
    darkColorScheme(
        primary = ComposeColor(Color.parseColor(primaryColor)),
        secondary = ComposeColor(Color.parseColor(primaryColor)).copy(alpha = 0.8f),
        tertiary = ComposeColor(Color.parseColor(primaryColor)).copy(alpha = 0.6f),
        background = ComposeColor(Color.parseColor(backgroundColor)),
        surface = ComposeColor(Color.parseColor(surfaceColor)),
        onPrimary = ComposeColor.White,
        onSecondary = ComposeColor.White,
        onTertiary = ComposeColor.Black,
        onBackground = ComposeColor.White,
        onSurface = ComposeColor.White
    )
} else {
    lightColorScheme(
        primary = ComposeColor(Color.parseColor(primaryColor)),
        secondary = ComposeColor(Color.parseColor(primaryColor)).copy(alpha = 0.8f),
        tertiary = ComposeColor(Color.parseColor(primaryColor)).copy(alpha = 0.6f),
        background = ComposeColor(Color.parseColor(backgroundColor)),
        surface = ComposeColor(Color.parseColor(surfaceColor)),
        onPrimary = ComposeColor.White,
        onSecondary = ComposeColor.White,
        onTertiary = ComposeColor.Black,
        onBackground = ComposeColor.Black,
        onSurface = ComposeColor.Black
    )
}

/**
 * 根据字体类型获取 FontFamily
 */
private fun getFontFamily(type: String): FontFamily {
    return when (type) {
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
}

/**
 * 创建自定义 Typography
 */
@Composable
private fun createCustomTypography(baseFontSize: Float, fontFamily: String): Typography {
    val baseSize = baseFontSize.sp
    val family = getFontFamily(fontFamily)
    
    // 使用默认的 Typography 作为基础，然后修改字体大小和字体
    return Typography(
        displayLarge = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 2.5f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.displayLarge.fontWeight
        ),
        displayMedium = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 2.2f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.displayMedium.fontWeight
        ),
        displaySmall = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 2f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.displaySmall.fontWeight
        ),
        headlineLarge = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 2f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.headlineLarge.fontWeight
        ),
        headlineMedium = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 1.8f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.headlineMedium.fontWeight
        ),
        headlineSmall = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 1.5f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.headlineSmall.fontWeight
        ),
        titleLarge = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 1.5f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.titleLarge.fontWeight
        ),
        titleMedium = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 1.3f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.titleMedium.fontWeight
        ),
        titleSmall = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 1.1f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.titleSmall.fontWeight
        ),
        bodyLarge = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 1.2f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.bodyLarge.fontWeight
        ),
        bodyMedium = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.bodyMedium.fontWeight
        ),
        bodySmall = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 0.8f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.bodySmall.fontWeight
        ),
        labelLarge = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 0.9f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.labelLarge.fontWeight
        ),
        labelMedium = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 0.8f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.labelMedium.fontWeight
        ),
        labelSmall = androidx.compose.ui.text.TextStyle(
            fontSize = baseSize * 0.7f,
            fontFamily = family,
            fontWeight = MaterialTheme.typography.labelSmall.fontWeight
        )
    )
}

/**
 * 默认深色主题
 */
private val DefaultDarkColorScheme = darkColorScheme(
    primary = ComposeColor(0xFF5FA3FF),
    secondary = ComposeColor(0xFF999999),
    tertiary = ComposeColor(0xFFBBBBBB),
    background = ComposeColor(0xFF121212),
    surface = ComposeColor(0xFF1E1E1E),
    onPrimary = ComposeColor.White,
    onSecondary = ComposeColor.White,
    onTertiary = ComposeColor.Black,
    onBackground = ComposeColor(0xFFEEEEEE),
    onSurface = ComposeColor(0xFFEEEEEE)
)

/**
 * 默认浅色主题
 */
private val DefaultLightColorScheme = lightColorScheme(
    primary = ComposeColor(0xFF2A86FF),
    secondary = ComposeColor(0xFF666666),
    tertiary = ComposeColor(0xFF999999),
    background = ComposeColor(0xFFFFFFFF),
    surface = ComposeColor(0xFFF5F5F5),
    onPrimary = ComposeColor.White,
    onSecondary = ComposeColor.White,
    onTertiary = ComposeColor.Black,
    onBackground = ComposeColor(0xFF333333),
    onSurface = ComposeColor(0xFF333333)
)

@Composable
fun LocalNoteTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val spUtils = SPUtils.getInstance(context)
    val themeMode = spUtils.getThemeMode()
    
    // 根据用户设置决定使用深色还是浅色主题
    val isSystemDark = isSystemInDarkTheme()
    val forceDark = when (themeMode) {
        AppConstants.THEME_MODE_LIGHT -> false
        AppConstants.THEME_MODE_DARK -> true
        else -> isSystemDark // 跟随系统
    }
    
    // 尝试加载自定义主题配置
    val config = runBlocking {
        try {
            ThemeManager.getThemeConfig(context)
        } catch (e: Exception) {
            null
        }
    }
    
    // 如果有自定义主题则使用，否则使用默认主题
    val colorScheme = if (config?.isCustomTheme == true) {
        createCustomColorScheme(
            primaryColor = config.primaryColor,
            backgroundColor = config.backgroundColor,
            surfaceColor = config.surfaceColor,
            isDarkMode = config.isDarkMode
        )
    } else {
        if (forceDark) DefaultDarkColorScheme else DefaultLightColorScheme
    }
    
    // 创建自定义 Typography
    val typography = if (config != null) {
        createCustomTypography(
            baseFontSize = config.fontSize,
            fontFamily = config.fontFamily
        )
    } else {
        MaterialTheme.typography
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 设置状态栏和导航栏为透明，让内容延伸
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // 根据背景色亮度自动调整状态栏图标颜色
            val backgroundColor = colorScheme.background
            // 简单的亮度计算：取 RGB 平均值
            val isLightBackground = (backgroundColor.red + backgroundColor.green + backgroundColor.blue) / 3 > 0.5f
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isLightBackground
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = isLightBackground
            
            // 设置状态栏和导航栏颜色为透明
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
