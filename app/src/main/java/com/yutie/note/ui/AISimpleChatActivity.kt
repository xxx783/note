package com.yutie.note.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yutie.note.model.AIModel
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.ui.theme.LocalNoteTheme
import com.yutie.note.utils.AIService
import kotlinx.coroutines.launch

/**
 * 消息数据类
 */
data class Message(val content: String, val isUser: Boolean)

/**
 * AI 聊天 Activity
 */
class AISimpleChatActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocalNoteTheme {
                AISimpleChatScreen()
            }
        }
    }
}

/**
 * 简单的 AI 聊天界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISimpleChatScreen() {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // 聊天记录
    var messages by remember { mutableStateOf(listOf<Message>()) }
    
    // 输入框
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // AI 服务
    val aiService = remember { AIService() }
    
    // 固定的 API 配置
    val apiKey = "sk-LIrhyhlwV3mWggpxdjUB4ii9cIKne6zRs8idjs10EqtIRhjY"
    val apiBaseUrl = "https://api.iamhc.cn/v1"
    val autoModel = AIModel(
        id = "auto",
        name = "全自动模型路由",
        inputPrice = 0.0,
        outputPrice = 0.0,
        isAuto = true,
        description = "智能选择最优模型"
    )
    
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
                title = "AI 助手",
                showBackButton = true,
                onBackClick = { activity?.finish() }
            )
            
            // 聊天消息列表
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "你好！我是你的 AI 助手，有什么可以帮你的吗？",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                items(messages) { message ->
                    MessageBubble(message = message)
                }
            }
            
            // 输入区域
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // 输入框
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 120.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = "输入消息...",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                innerTextField()
                            },
                            maxLines = 5
                        )
                    }
                    
                    // 发送按钮
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank() && !isLoading) {
                                // 添加用户消息
                                messages = messages + Message(inputText, true)
                                val userMessage = inputText
                                inputText = ""
                                isLoading = true
                                
                                // 滚动到底部
                                scope.launch {
                                    listState.animateScrollToItem(messages.size)
                                }
                                
                                // 调用 AI
                                scope.launch {
                                    try {
                                        // 初始化 AI 服务（使用硬编码的配置）
                                        aiService.initWithConfig(apiKey, apiBaseUrl)
                                        
                                        // 调用 AI
                                        val result = aiService.chat(autoModel, userMessage)
                                        
                                        result.fold(
                                            onSuccess = { response ->
                                                messages = messages + Message(response, false)
                                            },
                                            onFailure = { error ->
                                                messages = messages + Message("错误：${error.message}", false)
                                            }
                                        )
                                    } catch (e: Exception) {
                                        messages = messages + Message("错误：${e.message}", false)
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        enabled = inputText.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "发送",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 消息气泡
 */
@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            color = if (message.isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(16.dp),
                color = if (message.isUser) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 15.sp
            )
        }
    }
}
