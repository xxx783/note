package com.yutie.note.ui

import android.app.Activity
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.AIService
import com.yutie.note.utils.FeatureGuard
import com.yutie.note.model.AIModel
import com.yutie.note.model.AIModelConfig
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

data class ChatMsg(val role: String, val content: String, val isThinking: Boolean = false)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIToolboxScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val messages = remember { mutableStateListOf<ChatMsg>() }
    var inputText by remember { mutableStateOf("") }
    var selectedModel by remember { mutableStateOf<AIModel?>(null) }
    var isThinking by remember { mutableStateOf(false) }
    var thinkingJob by remember { mutableStateOf<Job?>(null) }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        val currentPlan = FeatureGuard.getCurrentPlan()
        hasPermission = currentPlan == "apex"

        val aiService = AIService()
        aiService.init()
        AIService.loadAIModelsFromCloud()
        selectedModel = AIModelConfig.getAutoModel()

        if (hasPermission) {
            messages.add(
                ChatMsg(
                    role = "assistant",
                    content = "你好！我是你的 AI 助手，有什么可以帮到你吗？(๑•̀ㅂ•́)و✧"
                )
            )
        }
        isLoading = false
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        if (inputText.isNotBlank() && !isThinking) {
            val userText = inputText
            val model = selectedModel ?: return

            messages.add(ChatMsg(role = "user", content = userText))
            messages.add(ChatMsg(role = "assistant", content = "模型正在思考中...", isThinking = true))
            inputText = ""
            isThinking = true

            thinkingJob = scope.launch {
                val aiService = AIService()
                aiService.init()
                val result = aiService.chat(
                    model = model,
                    message = userText,
                    systemPrompt = "你是一个贴心的生活助手。你可以回答用户关于日常生活、美食、健康、旅行、娱乐等方面的问题。回答要友好、简洁、实用，可以使用 Markdown 格式来美化回答内容。"
                )

                val reply = result.getOrDefault("抱歉，我暂时无法回复您的问题。请稍后再试。")

                val thinkingIndex = messages.indexOfLast { it.isThinking }
                if (thinkingIndex >= 0) {
                    messages[thinkingIndex] = ChatMsg(role = "assistant", content = reply)
                } else {
                    messages.add(ChatMsg(role = "assistant", content = reply))
                }

                isThinking = false
                thinkingJob = null
            }
        }
    }

    fun stopThinking() {
        thinkingJob?.cancel()
        val thinkingIndex = messages.indexOfLast { it.isThinking }
        if (thinkingIndex >= 0) {
            messages[thinkingIndex] = ChatMsg(role = "assistant", content = "（已停止生成）")
        }
        isThinking = false
        thinkingJob = null
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (!hasPermission) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "请升级到 APEX 版本以使用 AI 聊天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = { navController.navigate("upgrade_application") }) {
                    Text("立即升级")
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        CustomTitleBar(
            title = "AI 助手",
            showBackButton = true,
            onBackClick = { activity?.finish() }
        )

        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(
                    items = messages,
                    key = { index, _ -> index.toLong() }
                ) { _, message ->
                    ChatMessageBubble(message = message)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedModel?.name ?: "选择模型",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        placeholder = { Text("输入消息...") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (!isThinking) {
                                    sendMessage()
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isThinking) {
                            Button(
                                onClick = { stopThinking() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("停止")
                            }
                        } else {
                            Button(
                                onClick = { sendMessage() },
                                enabled = inputText.isNotBlank()
                            ) {
                                Text("发送")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMsg) {
    val isUser = message.role == "user"
    val context = LocalContext.current

    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(StrikethroughPlugin.create())
            .usePlugin(TablePlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.85f),
            color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            if (message.isThinking) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else if (isUser) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                AndroidView(
                    modifier = Modifier.padding(16.dp).fillMaxWidth().wrapContentHeight(),
                    factory = { ctx ->
                        TextView(ctx).apply {
                            layoutParams = android.view.ViewGroup.LayoutParams(
                                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setLineSpacing(0f, 1.6f)
                            textSize = 15f
                            val typedValue = android.util.TypedValue()
                            ctx.theme.resolveAttribute(
                                android.R.attr.textColorPrimary,
                                typedValue,
                                true
                            )
                            setTextColor(typedValue.data)
                        }
                    },
                    update = { textView ->
                        val processedContent = message.content.replace("\n", "  \n")
                        markwon.setMarkdown(textView, processedContent)
                        textView.invalidate()
                        textView.requestLayout()
                    }
                )
            }
        }
    }
}
