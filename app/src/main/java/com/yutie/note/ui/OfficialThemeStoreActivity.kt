package com.yutie.note.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.theme.ThemeConfig
import com.yutie.note.theme.ThemeManager
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.FeatureGuard
import kotlinx.coroutines.launch
import android.graphics.Color
import com.yutie.note.R

/**
 * 官方主题商店界面（Pro 功能）
 * 展示预设主题包，用户可以选择和应用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficialThemeStoreScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // 检查权限
    var hasPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        hasPermission = FeatureGuard.isProOrApex()
        isLoading = false
    }
    
    // 当前主题配置
    var currentThemeConfig by remember { mutableStateOf(ThemeConfig()) }
    
    LaunchedEffect(Unit) {
        currentThemeConfig = ThemeManager.getThemeConfig(context)
    }
    
    // 官方预设主题
    val officialThemes = remember {
        listOf(
            OfficialTheme(
                id = "default",
                name = "默认主题",
                description = "经典紫色主题",
                primaryColor = "#6200EE",
                backgroundColor = "#FFFFFF",
                surfaceColor = "#FFFFFF",
                isDark = false,
                isFree = true
            ),
            OfficialTheme(
                id = "dark",
                name = "深色模式",
                description = "护眼深色主题",
                primaryColor = "#BB86FC",
                backgroundColor = "#121212",
                surfaceColor = "#1E1E1E",
                isDark = true,
                isFree = true
            ),
            OfficialTheme(
                id = "ocean",
                name = "海洋蓝",
                description = "清新海洋风格",
                primaryColor = "#0077C8",
                backgroundColor = "#E3F2FD",
                surfaceColor = "#FFFFFF",
                isDark = false,
                isFree = false
            ),
            OfficialTheme(
                id = "ocean_dark",
                name = "海洋蓝·深色",
                description = "深邃海洋风格",
                primaryColor = "#4FC3F7",
                backgroundColor = "#0D1B2A",
                surfaceColor = "#1B263B",
                isDark = true,
                isFree = false
            ),
            OfficialTheme(
                id = "forest",
                name = "森林绿",
                description = "自然清新绿色",
                primaryColor = "#2E7D32",
                backgroundColor = "#E8F5E9",
                surfaceColor = "#FFFFFF",
                isDark = false,
                isFree = false
            ),
            OfficialTheme(
                id = "forest_dark",
                name = "森林绿·深色",
                description = "幽静森林风格",
                primaryColor = "#81C784",
                backgroundColor = "#1B2D1B",
                surfaceColor = "#2D3B2D",
                isDark = true,
                isFree = false
            ),
            OfficialTheme(
                id = "sunset",
                name = "日落橙",
                description = "温暖夕阳色彩",
                primaryColor = "#FF6D00",
                backgroundColor = "#FFF3E0",
                surfaceColor = "#FFFFFF",
                isDark = false,
                isFree = false
            ),
            OfficialTheme(
                id = "sunset_dark",
                name = "日落橙·深色",
                description = "黄昏夜色风格",
                primaryColor = "#FFB74D",
                backgroundColor = "#2D1B0E",
                surfaceColor = "#3B2518",
                isDark = true,
                isFree = false
            ),
            OfficialTheme(
                id = "cherry",
                name = "樱花粉",
                description = "浪漫樱花粉色",
                primaryColor = "#EC407A",
                backgroundColor = "#FCE4EC",
                surfaceColor = "#FFFFFF",
                isDark = false,
                isFree = false
            ),
            OfficialTheme(
                id = "cherry_dark",
                name = "樱花粉·深色",
                description = "夜樱浪漫风格",
                primaryColor = "#F48FB1",
                backgroundColor = "#2D1B2E",
                surfaceColor = "#3B253B",
                isDark = true,
                isFree = false
            ),
            OfficialTheme(
                id = "midnight",
                name = "午夜深蓝",
                description = "深邃夜空蓝调",
                primaryColor = "#536DFE",
                backgroundColor = "#0D1117",
                surfaceColor = "#161B22",
                isDark = true,
                isFree = false
            ),
            OfficialTheme(
                id = "gold",
                name = "奢华金",
                description = "高贵金色主题",
                primaryColor = "#FFD700",
                backgroundColor = "#1A1A1A",
                surfaceColor = "#2A2A2A",
                isDark = true,
                isFree = false
            )
        )
    }
    
    // 如果没有权限，显示升级提示
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (!hasPermission) {
        PermissionDeniedScreen(
            featureName = "官方主题商店",
            requiredPlan = "Pro",
            onUpgradeClick = {
                FeatureGuard.showUpgradeDialog(context, "官方主题商店")
            },
            onBackClick = { navController.popBackStack() }
        )
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTitleBar(
            title = context.getString(R.string.str_official_theme_store_title),
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 说明卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = context.getString(R.string.str_featured_themes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = context.getString(R.string.str_theme_cards_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 主题网格
            item {
                Text(
                    text = context.getString(R.string.str_all_themes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(
                        ((officialThemes.size + 1) / 2 * 200).dp
                    )
                ) {
                    items(officialThemes) { theme ->
                        OfficialThemeCard(
                            theme = theme,
                            isCurrentTheme = currentThemeConfig.primaryColor == theme.primaryColor,
                            onClick = {
                                // 应用主题
                                scope.launch {
                                    val newConfig = ThemeConfig(
                                        primaryColor = theme.primaryColor,
                                        backgroundColor = theme.backgroundColor,
                                        surfaceColor = theme.surfaceColor,
                                        isDarkMode = theme.isDark,
                                        isCustomTheme = true  // 改为 true，让主题生效
                                    )
                                    ThemeManager.saveThemeConfig(context, newConfig)
                                    // 立即更新本地状态，让 UI 显示当前主题标识
                                    currentThemeConfig = newConfig
                                    android.widget.Toast.makeText(
                                        context,
                                        "已应用：${theme.name}",
                                        android.widget.Toast.LENGTH_SHORT
                                    ).show()
                                    // 延迟一点重建以应用新主题
                                    kotlinx.coroutines.delay(300)
                                    activity?.recreate()
                                }
                            }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 官方主题数据类
 */
data class OfficialTheme(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: String,
    val backgroundColor: String,
    val surfaceColor: String,
    val isDark: Boolean,
    val isFree: Boolean
)

/**
 * 官方主题卡片
 */
@Composable
fun OfficialThemeCard(
    theme: OfficialTheme,
    isCurrentTheme: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = hexToComposeColor(theme.surfaceColor)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentTheme) 8.dp else 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 颜色预览区域
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 主色调
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(hexToComposeColor(theme.primaryColor))
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                )
                
                // 背景色
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(hexToComposeColor(theme.backgroundColor))
                )
                
                // 表面色
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(hexToComposeColor(theme.surfaceColor))
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 主题名称
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                if (isCurrentTheme) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                if (!theme.isFree) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 主题描述
            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
            
            // 深色模式标识
            if (theme.isDark) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NightsStay,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.str_dark),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        // 当前主题边框
        if (isCurrentTheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }
    }
}

/**
 * 将 HEX 颜色字符串转换为 Compose Color
 */
private fun hexToComposeColor(hex: String): ComposeColor {
    return try {
        ComposeColor(Color.parseColor(hex))
    } catch (e: Exception) {
        ComposeColor(Color.parseColor("#6200EE"))
    }
}
