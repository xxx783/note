package com.yutie.note.ui

import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.yutie.note.R
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.ui.viewmodel.NoteEditViewModel
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin

/**
 * 笔记详情页面 - 支持阅读模式和编辑模式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long = 0L,
    onBackClick: () -> Unit,
    viewModel: NoteEditViewModel = viewModel()
) {
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val showSaveHint by viewModel.showSaveHint.collectAsState()
    
    // 是否是编辑模式，默认 false（阅读模式），新建笔记时直接进编辑模式
    var isEditMode by remember(noteId) { mutableStateOf(noteId == 0L) }
    
    // 关键修复：使用阻塞式加载确保数据立即显示
    LaunchedEffect(noteId) {
        // 直接调用 setNoteId，它会处理加载或清空逻辑
        viewModel.setNoteId(noteId)
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
                title = if (noteId == 0L) stringResource(R.string.str_new_note) else 
                       if (title.isEmpty()) stringResource(R.string.str_no_title) else title,
                showBackButton = true,
                rightIcon = if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                onBackClick = onBackClick,
                onRightClick = {
                    if (isEditMode) {
                        viewModel.saveNote(showHint = true)
                        isEditMode = false
                    } else {
                        isEditMode = true
                    }
                }
            )
            
            // 保存状态提示（只在编辑模式显示）
            if (isEditMode && showSaveHint) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    AssistChip(
                        onClick = { },
                        label = { 
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = stringResource(R.string.str_note_saved),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                }
            }
            
            // 内容区域
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEditMode) {
                    // 编辑模式 - 标题输入
                    BasicTextField(
                        value = title,
                        onValueChange = { viewModel.setTitle(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (title.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.str_note_title_placeholder),
                                        style = TextStyle(
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        maxLines = 3
                    )
                    
                    // 编辑模式 - 内容输入
                    BasicTextField(
                        value = content,
                        onValueChange = { viewModel.setContent(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 600.dp)
                            .padding(16.dp),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (content.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.str_note_content_placeholder),
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            lineHeight = 24.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        },
                        maxLines = Int.MAX_VALUE
                    )
                    
                    // 底部提示
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { 
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.str_note_save_hint),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                            )
                        )
                    }
                } else {
                    // 阅读模式 - 展示标题
                    if (title.isEmpty()) {
                        Text(
                            text = stringResource(R.string.str_no_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // 阅读模式 - 展示内容（Markdown 渲染）
                    if (content.isEmpty()) {
                        Text(
                            text = stringResource(R.string.str_no_content),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            lineHeight = 28.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        val context = LocalContext.current
                        val markwon = remember {
                            Markwon.builder(context)
                                .usePlugin(StrikethroughPlugin.create())
                                .usePlugin(TablePlugin.create(context))
                                .usePlugin(HtmlPlugin.create())
                                .usePlugin(LinkifyPlugin.create())
                                .build()
                        }
                        AndroidView(
                            factory = { ctx ->
                                TextView(ctx).apply {
                                    setPadding(
                                        (16 * ctx.resources.displayMetrics.density).toInt(),
                                        0,
                                        (16 * ctx.resources.displayMetrics.density).toInt(),
                                        0
                                    )
                                    setLineSpacing(0f, 1.4f)
                                }
                            },
                            update = { textView ->
                                markwon.setMarkdown(textView, content)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
