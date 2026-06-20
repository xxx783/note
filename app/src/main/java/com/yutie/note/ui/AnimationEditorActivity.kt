package com.yutie.note.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.theme.ThemeConfig
import com.yutie.note.theme.ThemeManager
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.AnimationConfig
import com.yutie.note.utils.AnimationManager
import com.yutie.note.utils.FeatureGuard
import kotlinx.coroutines.launch
import android.graphics.Color

/**
 * 动画编辑器界面（APEX 功能）
 * 允许用户自定义应用动画效果
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationEditorScreen(
    navController: NavController
) {
    val context = LocalContext.current
    @Suppress("UNUSED_VARIABLE") val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // 检查权限 - APEX 专属功能
    var hasPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var currentPlan by remember { mutableStateOf("free") }
    
    LaunchedEffect(Unit) {
        currentPlan = FeatureGuard.getCurrentPlan()
        hasPermission = currentPlan == "apex"
        isLoading = false
    }
    
    // 当前主题配置
    var currentThemeConfig by remember { mutableStateOf(ThemeConfig()) }
    
    // 动画配置
    var animationSpeed by remember { mutableStateOf(1.0f) } // 动画速度倍率
    var transitionType by remember { mutableStateOf("fade") } // 过渡类型：fade, slide, scale
    var springStiffness by remember { mutableStateOf(50f) } // 弹性系数
    var enableAnimations by remember { mutableStateOf(true) } // 是否启用动画
    var isConfigLoaded by remember { mutableStateOf(false) }
    
    // 加载配置
    LaunchedEffect(Unit) {
        currentThemeConfig = ThemeManager.getThemeConfig(context)
        // 从 AnimationManager 加载动画配置
        val animationConfig = AnimationManager.getAnimationConfig(context)
        animationSpeed = animationConfig.speed
        transitionType = animationConfig.transitionType
        springStiffness = animationConfig.springStiffness
        enableAnimations = animationConfig.enabled
        isConfigLoaded = true
    }
    
    // 如果没有权限，显示升级提示
    if (isLoading || !isConfigLoaded) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    if (!hasPermission) {
        ApexPermissionDeniedScreen(
            featureName = "动画编辑器",
            currentPlan = currentPlan,
            onUpgradeClick = {
                FeatureGuard.showUpgradeDialog(context, "动画编辑器")
            },
            onBackClick = { navController.popBackStack() }
        )
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(hexToComposeColor(currentThemeConfig.backgroundColor))
    ) {
        CustomTitleBar(
            title = "动画编辑器",
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // APEX 标识卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = stringResource(R.string.str_apex_feature),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(R.string.str_custom_animation),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 总开关
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = hexToComposeColor(currentThemeConfig.surfaceColor)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                                text = stringResource(R.string.str_enable_animation),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = hexToComposeColor(currentThemeConfig.primaryColor)
                            )
                            Text(
                                text = stringResource(R.string.str_disable_animation),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        Switch(
                            checked = enableAnimations,
                            onCheckedChange = { enableAnimations = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = hexToComposeColor(currentThemeConfig.primaryColor),
                                checkedTrackColor = hexToComposeColor(currentThemeConfig.primaryColor).copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 动画速度
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = hexToComposeColor(currentThemeConfig.surfaceColor)
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
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = hexToComposeColor(currentThemeConfig.primaryColor),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = stringResource(R.string.str_animation_speed),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = hexToComposeColor(currentThemeConfig.primaryColor)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = stringResource(R.string.str_current_speed, animationSpeed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Slider(
                            value = animationSpeed,
                            onValueChange = { animationSpeed = it },
                            valueRange = 0.5f..2.0f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = hexToComposeColor(currentThemeConfig.primaryColor),
                                activeTrackColor = hexToComposeColor(currentThemeConfig.primaryColor)
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "0.5x",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = stringResource(R.string.str_default_speed),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "2.0x",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 过渡效果类型
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = hexToComposeColor(currentThemeConfig.surfaceColor)
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
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = hexToComposeColor(currentThemeConfig.primaryColor),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = stringResource(R.string.str_transition_effect),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = hexToComposeColor(currentThemeConfig.primaryColor)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 过渡类型选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = transitionType == "fade",
                                onClick = { transitionType = "fade" },
                                label = { Text("渐隐渐现") },
                                leadingIcon = if (transitionType == "fade") {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "已选择",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            
                            FilterChip(
                                selected = transitionType == "slide",
                                onClick = { transitionType = "slide" },
                                label = { Text("滑动") },
                                leadingIcon = if (transitionType == "slide") {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "已选择",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            
                            FilterChip(
                                selected = transitionType == "scale",
                                onClick = { transitionType = "scale" },
                                label = { Text("缩放") },
                                leadingIcon = if (transitionType == "scale") {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "已选择",
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
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 预览区域
                        Text(
                            text = stringResource(R.string.str_effect_preview),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 弹性系数
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = hexToComposeColor(currentThemeConfig.surfaceColor)
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
                                imageVector = Icons.Default.Fitbit,
                                contentDescription = null,
                                tint = hexToComposeColor(currentThemeConfig.primaryColor),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = stringResource(R.string.str_spring_stiffness),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = hexToComposeColor(currentThemeConfig.primaryColor)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = stringResource(R.string.str_current_stiffness, springStiffness.toInt()),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Slider(
                            value = springStiffness,
                            onValueChange = { springStiffness = it },
                            valueRange = 20f..100f,
                            steps = 8,
                            colors = SliderDefaults.colors(
                                thumbColor = hexToComposeColor(currentThemeConfig.primaryColor),
                                activeTrackColor = hexToComposeColor(currentThemeConfig.primaryColor)
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.str_soft),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = stringResource(R.string.str_standard),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = stringResource(R.string.str_bouncy),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
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
                            // 保存动画配置到 DataStore
                            val animationConfig = AnimationConfig(
                                speed = animationSpeed,
                                transitionType = transitionType,
                                springStiffness = springStiffness,
                                enabled = enableAnimations
                            )
                            AnimationManager.saveAnimationConfig(context, animationConfig)
                            
                            android.widget.Toast.makeText(
                                context,
                                "✓ 动画配置已保存\n• 速度：${String.format("%.1fx", animationSpeed)}\n• 过渡：$transitionType\n• 弹性：${springStiffness.toInt()}\n• 开关：${if (enableAnimations) "开启" else "关闭"}",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = hexToComposeColor(currentThemeConfig.primaryColor)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.str_save_animation),
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
}

/**
 * APEX 权限不足提示页面
 */
@Composable
fun ApexPermissionDeniedScreen(
    featureName: String,
    currentPlan: String,
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
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "✨ $featureName",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.str_apex_required, currentPlan.uppercase()),
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
                text = stringResource(R.string.str_upgrade_to_apex),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onBackClick) {
            Text("返回")
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
