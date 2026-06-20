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
import com.yutie.note.ui.editor.MarkdownSplitScreen
import com.yutie.note.ui.editor.MarkdownToolbar
import com.yutie.note.ui.editor.ViewMode
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
    
    // 视图模式：新建笔记默认分屏模式，已有笔记默认阅读模式
    var viewMode by remember(noteId) { 
        mutableStateOf(if (noteId == 0L) ViewMode.SPLIT else ViewMode.PREVIEW_ONLY) 
    }
    
    // 关键修复：使用阻塞式加载确保数据立即显示
    LaunchedEffect(noteId) {
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
                rightIcon = getViewModeIcon(viewMode),
                onBackClick = onBackClick,
                onRightClick = {
                    viewMode = cycleViewMode(viewMode)
                }
            )
            
            // 保存状态提示
            if (viewMode != ViewMode.PREVIEW_ONLY && showSaveHint) {
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
            
            // Markdown 工具栏（编辑模式和分屏模式显示）
            if (viewMode != ViewMode.PREVIEW_ONLY) {
                MarkdownToolbar(onFormatClick = { formatType ->
                    // 应用格式化
                    val (newContent, newCursorPos) = com.yutie.note.ui.editor.applyFormat(
                        content, 
                        0, 
                        formatType
                    )
                    viewModel.setContent(newContent)
                })
            }
            
            // 内容区域
            Box(modifier = Modifier.fillMaxSize()) {
                MarkdownSplitScreen(
                    viewMode = viewMode,
                    title = title,
                    content = content,
                    onTitleChange = { viewModel.setTitle(it) },
                    onContentChange = { viewModel.setContent(it) },
                    isProUser = true
                )
            }
        }
    }
}

/**
 * 根据视图模式获取图标
 */
@Composable
fun getViewModeIcon(viewMode: ViewMode): androidx.compose.ui.graphics.vector.ImageVector {
    return when (viewMode) {
        ViewMode.EDIT_ONLY -> Icons.Default.EditNote
        ViewMode.SPLIT -> Icons.Default.PanoramaHorizontal
        ViewMode.PREVIEW_ONLY -> Icons.Default.RemoveRedEye
    }
}

/**
 * 循环切换视图模式
 */
fun cycleViewMode(current: ViewMode): ViewMode {
    return when (current) {
        ViewMode.EDIT_ONLY -> ViewMode.SPLIT
        ViewMode.SPLIT -> ViewMode.PREVIEW_ONLY
        ViewMode.PREVIEW_ONLY -> ViewMode.EDIT_ONLY
    }
}
