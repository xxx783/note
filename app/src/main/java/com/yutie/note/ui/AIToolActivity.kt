package com.yutie.note.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.model.AIModel
import com.yutie.note.model.AIModelConfig
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.AIService
import kotlinx.coroutines.launch

/**
 * AI 工具使用界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIToolScreen(
    navController: NavController,
    toolType: String,
    toolName: String,
    toolIcon: Int
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(AIModelConfig.getAutoModel()) }
    var showModelSelector by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val aiService = remember { AIService() }
    
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
                title = toolName,
                showBackButton = true,
                onBackClick = { 
                    navController.popBackStack()
                }
            )
            
            // 模型选择
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
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "选择模型",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // 输入区域
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // 输入框
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("请输入文本") },
                    placeholder = { Text("在此输入需要处理的文本...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = Int.MAX_VALUE
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 输出框
                OutlinedTextField(
                    value = outputText,
                    onValueChange = { },
                    label = { Text("AI 处理结果") },
                    placeholder = { Text("处理结果将显示在这里...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.secondary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = Int.MAX_VALUE,
                    readOnly = true,
                    trailingIcon = {
                        if (outputText.isNotEmpty()) {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(outputText))
                                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
                
                // 错误信息
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "错误：$errorMessage",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            
            // 底部按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 清空按钮
                Button(
                    onClick = {
                        inputText = ""
                        outputText = ""
                        errorMessage = null
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清空")
                }
                
                // 开始按钮
                Button(
                    onClick = {
                        if (inputText.isBlank()) {
                            Toast.makeText(context, "请输入文本", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            
                            try {
                                val result = when (toolType) {
                                    "polish" -> aiService.polishText(selectedModel, inputText)
                                    "summarize" -> aiService.summarizeText(selectedModel, inputText)
                                    "translate" -> aiService.translateText(selectedModel, inputText)
                                    "grammar" -> aiService.checkGrammar(selectedModel, inputText)
                                    "format" -> aiService.formatText(selectedModel, inputText)
                                    else -> Result.failure(Exception("未知的工具类型"))
                                }
                                
                                result.fold(
                                    onSuccess = { outputText = it },
                                    onFailure = { errorMessage = it.message ?: "处理失败" }
                                )
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "处理失败"
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.weight(2f),
                    enabled = !isLoading && inputText.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("处理中...")
                    } else {
                        Icon(
                            imageVector = if (toolType == "polish") Icons.Default.AutoFixHigh else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("开始处理")
                    }
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
