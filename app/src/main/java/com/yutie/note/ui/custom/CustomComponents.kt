package com.yutie.note.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yutie.note.R

/**
 * 自定义标题栏组件
 * 支持返回按钮、标题、右侧功能按钮
 */
@Composable
fun CustomTitleBar(
    title: String,
    showBackButton: Boolean = true,
    rightIcon: ImageVector? = null,
    onBackClick: () -> Unit = {},
    onRightClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 根据主题设置背景色：浅色白色，深色黑色
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp)
            .padding(top = 40.dp), // 添加顶部30dp间距
        color = backgroundColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧区域
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = contentColor
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // 标题
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1
                )
            }
            
            // 右侧区域
            rightIcon?.let { icon ->
                IconButton(
                    onClick = onRightClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor
                    )
                }
            } ?: run {
                Spacer(modifier = Modifier.width(40.dp))
            }
        }
    }
}

/**
 * 自定义空状态视图
 * 用于无数据时展示
 */
@Composable
fun CustomEmptyView(
    message: String = "暂无数据",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 简单图标（用文字代替）
        Text(
            text = "📝",
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 自定义加载弹窗
 */
@Composable
fun CustomLoadingDialog(
    isLoading: Boolean,
    message: String = "加载中…",
    modifier: Modifier = Modifier
) {
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(text = message) },
            confirmButton = { },
            modifier = modifier
        )
    }
}

/**
 * 自定义确认弹窗
 */
@Composable
fun CustomConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.str_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.str_cancel))
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    )
}
