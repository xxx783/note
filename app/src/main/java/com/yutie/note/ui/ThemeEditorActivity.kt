package com.yutie.note.ui

import android.app.Activity
import android.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.theme.ThemeConfig
import com.yutie.note.theme.ThemeManager
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.FeatureGuard
import kotlinx.coroutines.launch
import com.yutie.note.R

/**
 * 主题编辑器界面（Pro 功能）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeEditorScreen(
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
    
    // 加载当前主题配置
    var themeConfig by remember { mutableStateOf(ThemeConfig()) }
    
    // 临时颜色值
    var tempPrimaryColor by remember { mutableStateOf("#6200EE") }
    var tempBackgroundColor by remember { mutableStateOf("#FFFFFF") }
    var tempSurfaceColor by remember { mutableStateOf("#FFFFFF") }
    
    LaunchedEffect(Unit) {
        themeConfig = ThemeManager.getThemeConfig(context)
        // 初始化临时颜色值
        tempPrimaryColor = themeConfig.primaryColor
        tempBackgroundColor = themeConfig.backgroundColor
        tempSurfaceColor = themeConfig.surfaceColor
    }
    
    // 颜色选择器状态
    var showPrimaryColorPicker by remember { mutableStateOf(false) }
    var showBackgroundColorPicker by remember { mutableStateOf(false) }
    var showSurfaceColorPicker by remember { mutableStateOf(false) }
    
    // 预览文本
    val previewTitle = "预览标题"
    val previewContent = "这是一段预览文本，用于展示当前主题的效果。您可以实时看到颜色、字体等设置的变化。"
    
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
            featureName = "主题编辑器",
            requiredPlan = "Pro",
            onUpgradeClick = {
                FeatureGuard.showUpgradeDialog(context, "主题编辑器")
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
            title = stringResource(R.string.str_theme_editor_title),
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 颜色配置区域
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
                        Text(
                            text = stringResource(R.string.str_color_config),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 主色调选择
                        ThemeColorPickerItem(
                            title = stringResource(R.string.str_primary_color),
                            colorHex = tempPrimaryColor,
                            onClick = {
                                tempPrimaryColor = themeConfig.primaryColor
                                showPrimaryColorPicker = true
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 背景色选择
                        ThemeColorPickerItem(
                            title = stringResource(R.string.str_background_color),
                            colorHex = tempBackgroundColor,
                            onClick = {
                                tempBackgroundColor = themeConfig.backgroundColor
                                showBackgroundColorPicker = true
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 表面色选择
                        ThemeColorPickerItem(
                            title = stringResource(R.string.str_surface_color),
                            colorHex = tempSurfaceColor,
                            onClick = {
                                tempSurfaceColor = themeConfig.surfaceColor
                                showSurfaceColorPicker = true
                            }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 字体配置区域
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
                        Text(
                            text = stringResource(R.string.str_font_config),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 字体大小
                        Text(
                            text = stringResource(R.string.str_font_size_label, themeConfig.fontSize.toInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Slider(
                            value = themeConfig.fontSize,
                            onValueChange = { 
                                themeConfig = themeConfig.copy(fontSize = it)
                            },
                            valueRange = 14f..24f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = hexToComposeColor(themeConfig.primaryColor),
                                activeTrackColor = hexToComposeColor(themeConfig.primaryColor)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 字体类型
                        Text(
                            text = stringResource(R.string.str_font_type),
                            style = MaterialTheme.typography.bodyMedium,
                            color = hexToComposeColor(themeConfig.primaryColor)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        FontTypeSelector(
                            currentFont = themeConfig.fontFamily,
                            onFontSelected = { themeConfig = themeConfig.copy(fontFamily = it) }
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 预览区域
            item {
                Text(
                    text = stringResource(R.string.str_live_preview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = previewTitle,
                            fontSize = themeConfig.fontSize.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontFamily = getFontFamily(themeConfig.fontFamily)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = previewContent,
                            fontSize = themeConfig.fontSize.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            fontFamily = getFontFamily(themeConfig.fontFamily),
                            lineHeight = (themeConfig.fontSize * 1.5).sp
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 保存按钮
            item {
                Button(
                    onClick = {
                        scope.launch {
                            // 更新配置
                            themeConfig = themeConfig.copy(
                                primaryColor = tempPrimaryColor,
                                backgroundColor = tempBackgroundColor,
                                surfaceColor = tempSurfaceColor,
                                isCustomTheme = true
                            )
                            // 保存到 DataStore
                            ThemeManager.saveThemeConfig(context, themeConfig)
                            // 提示保存成功
                            android.widget.Toast.makeText(
                                context,
                                "主题已保存并应用",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            // 重建活动以应用新主题
                            activity?.recreate()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.str_save_apply_theme),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // 颜色选择器弹窗
    if (showPrimaryColorPicker) {
        ColorPickerDialog(
            currentColor = tempPrimaryColor,
            onColorSelected = { tempPrimaryColor = it },
            onDismiss = { showPrimaryColorPicker = false }
        )
    }
    
    if (showBackgroundColorPicker) {
        ColorPickerDialog(
            currentColor = tempBackgroundColor,
            onColorSelected = { tempBackgroundColor = it },
            onDismiss = { showBackgroundColorPicker = false }
        )
    }
    
    if (showSurfaceColorPicker) {
        ColorPickerDialog(
            currentColor = tempSurfaceColor,
            onColorSelected = { tempSurfaceColor = it },
            onDismiss = { showSurfaceColorPicker = false }
        )
    }
}

/**
 * 主题颜色选择项
 */
@Composable
fun ThemeColorPickerItem(
    title: String,
    colorHex: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = colorHex,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ComposeColor(Color.parseColor(colorHex)))
                    .border(
                        BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                        CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 字体类型选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontTypeSelector(
    currentFont: String,
    onFontSelected: (String) -> Unit
) {
    val fontOptions = listOf(
        "default" to stringResource(R.string.str_default),
        "serif" to stringResource(R.string.str_serif),
        "monospace" to stringResource(R.string.str_monospace)
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(fontOptions) { (fontValue, fontLabel) ->
            FilterChip(
                selected = currentFont == fontValue,
                onClick = { onFontSelected(fontValue) },
                label = { Text(fontLabel) },
                leadingIcon = if (currentFont == fontValue) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * 颜色选择器弹窗
 */
@Composable
fun ColorPickerDialog(
    currentColor: String,
    onColorSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedColor by remember { mutableStateOf(currentColor) }
    
    // 预设颜色
    val presetColors = listOf(
        "#6200EE", "#03DAC6", "#BB86FC", "#CF6679",
        "#FF5722", "#795548", "#607D8B", "#4CAF50",
        "#FFC107", "#FF9800", "#E91E63", "#9C27B0",
        "#3F51B5", "#2196F3", "#00BCD4", "#009688",
        "#424242", "#000000", "#FFFFFF"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.str_color_picker)) },
        text = {
            Column {
                // 当前颜色显示
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.str_current_label),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ComposeColor(Color.parseColor(selectedColor)))
                            .border(
                                BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                                RoundedCornerShape(8.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = selectedColor,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // 预设颜色网格
                Text(
                    text = stringResource(R.string.str_preset_colors),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetColors) { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ComposeColor(Color.parseColor(colorHex)))
                                .border(
                                    if (selectedColor == colorHex) 
                                        BorderStroke(3.dp, MaterialTheme.colorScheme.primary) 
                                    else 
                                        BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedColor = colorHex
                                }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 自定义颜色输入（简化版，仅显示提示）
                Text(
                    text = stringResource(R.string.str_color_picker_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onColorSelected(selectedColor)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.str_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.str_cancel))
            }
        }
    )
}

/**
 * 将 HEX 颜色字符串转换为 Compose Color
 */
private fun hexToComposeColor(hex: String): ComposeColor {
    return try {
        ComposeColor(Color.parseColor(hex))
    } catch (e: Exception) {
        ComposeColor(Color.parseColor("#6200EE")) // 默认紫色
    }
}

/**
 * 获取字体类型
 */
private fun getFontFamily(fontType: String): FontFamily {
    return when (fontType) {
        "serif" -> FontFamily.Serif
        "monospace" -> FontFamily.Monospace
        else -> FontFamily.Default
    }
}

/**
 * 权限不足提示页面
 */
@Composable
fun PermissionDeniedScreen(
    featureName: String,
    requiredPlan: String,
    onUpgradeClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "🔒 $featureName",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "此功能需要 $requiredPlan 计划",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onUpgradeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.str_learn_more),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onBackClick) {
            Text(stringResource(R.string.str_back))
        }
    }
}
