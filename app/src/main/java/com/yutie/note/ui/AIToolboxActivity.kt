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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.model.AIModel
import com.yutie.note.model.AIModelConfig
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.AIService
import com.yutie.note.utils.FeatureGuard
import kotlinx.coroutines.launch

/**
 * AI 工具箱界面（APEX 功能）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIToolboxScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // 检查 APEX 权限
    var hasPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var showModelSelector by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(AIModelConfig.getAutoModel()) }
    
    LaunchedEffect(Unit) {
        val currentPlan = FeatureGuard.getCurrentPlan()
        hasPermission = currentPlan == "apex"
        
        println("=== AI 工具箱初始化 ===")
        println("当前计划：$currentPlan")
        println("是否有权限：$hasPermission")
        
        // 从云端加载 AI 模型列表
        val aiService = AIService()
        val apiInited = aiService.init()
        println("API 初始化：$apiInited")
        
        val modelsLoaded = AIService.loadAIModelsFromCloud()
        println("模型加载：$modelsLoaded")
        println("模型数量：${AIModelConfig.models.size}")
        
        isLoading = false
    }
    
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
        // 无权限提示
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "此功能仅限 APEX 用户",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "请升级到 APEX 版本以使用 AI 工具箱",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Button(
                    onClick = {
                        // TODO: 跳转到升级页面
                    },
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    Text("立即升级")
                }
            }
        }
        return
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 标题栏
            CustomTitleBar(
                title = "AI 工具箱",
                showBackButton = true,
                onBackClick = { activity?.finish() }
            )
            
            // 当前模型显示
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { showModelSelector = true },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
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
                            text = "当前模型",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = selectedModel.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = selectedModel.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "选择模型",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // AI 工具列表
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AIToolCard(
                        icon = Icons.Default.AutoFixHigh,
                        title = "智能润色",
                        description = "优化文字表达",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            navController.navigate("ai_tool/polish/智能润色")
                        }
                    )
                }
                
                item {
                    AIToolCard(
                        icon = Icons.Default.Summarize,
                        title = "内容总结",
                        description = "生成简洁摘要",
                        color = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            navController.navigate("ai_tool/summarize/内容总结")
                        }
                    )
                }
                
                item {
                    AIToolCard(
                        icon = Icons.Default.Translate,
                        title = "智能翻译",
                        description = "多语言互译",
                        color = MaterialTheme.colorScheme.tertiary,
                        onClick = {
                            navController.navigate("ai_tool/translate/智能翻译")
                        }
                    )
                }
                
                item {
                    AIToolCard(
                        icon = Icons.Default.CheckCircle,
                        title = "语法检查",
                        description = "纠错和检查",
                        color = MaterialTheme.colorScheme.error,
                        onClick = {
                            navController.navigate("ai_tool/grammar/语法检查")
                        }
                    )
                }
                
                item {
                    AIToolCard(
                        icon = Icons.Default.FormatAlignLeft,
                        title = "格式转换",
                        description = "Markdown 格式化",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = {
                            navController.navigate("ai_tool/format/格式转换")
                        }
                    )
                }
            }
        }
        
        // 模型选择器弹窗
        if (showModelSelector) {
            AIModelSelectorDialog(
                currentModel = selectedModel,
                onModelSelected = { model ->
                    selectedModel = model
                    showModelSelector = false
                },
                onDismiss = { showModelSelector = false }
            )
        }
    }
}

/**
 * AI 工具卡片
 */
@Composable
fun AIToolCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * AI 模型选择器弹窗
 */
@Composable
fun AIModelSelectorDialog(
    currentModel: AIModel,
    onModelSelected: (AIModel) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "选择 AI 模型",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AIModelConfig.models.size) { index ->
                        val model = AIModelConfig.models[index]
                        AIModelItem(
                            model = model,
                            isSelected = model.id == currentModel.id,
                            onClick = { onModelSelected(model) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Text("取消")
                }
            }
        }
    }
}

/**
 * AI 模型选项
 */
@Composable
fun AIModelItem(
    model: AIModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (model.isAuto) {
                    AssistChip(
                        onClick = { },
                        label = {
                            Text(
                                text = "智能",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "输入：$${model.inputPrice}/1K",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
                Text(
                    text = "输出：$${model.outputPrice}/1K",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            }
        }
    }
}
