package com.yutie.note.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.ui.custom.CustomTitleBar

/**
 * 功能说明页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureGuideScreen(
    navController: NavController
) {
    Column {
        // 标题栏
        CustomTitleBar(
            title = stringResource(R.string.str_feature_guide),
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )
        
        // 内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 核心功能
            FeatureSection(
                title = stringResource(R.string.str_core_features),
                icon = Icons.Default.Star,
                content = "• 简洁的笔记记录和管理\n• 支持长篇内容写作\n• 自动保存草稿\n• 笔记置顶功能\n• 笔记加密保护隐私"
            )
            
            // 批量管理
            FeatureSection(
                title = stringResource(R.string.str_batch_manage_guide),
                icon = Icons.Default.SelectAll,
                content = "• 长按笔记进入批量模式\n• 支持多选笔记\n• 一键全选/取消全选\n• 批量删除笔记"
            )
            
            // 搜索功能
            FeatureSection(
                title = stringResource(R.string.str_search_guide),
                icon = Icons.Default.Search,
                content = "• 支持标题和内容搜索\n• 实时模糊匹配\n• 快速定位笔记"
            )
            
            // 主题设置
            FeatureSection(
                title = stringResource(R.string.str_theme_guide),
                icon = Icons.Default.Palette,
                content = "• 浅色模式 - 清爽明亮\n• 深色模式 - 护眼舒适\n• 跟随系统 - 自动切换"
            )
            
            // 数据安全
            FeatureSection(
                title = stringResource(R.string.str_security_guide),
                icon = Icons.Default.Security,
                content = "• 纯本地存储\n• 无需网络连接\n• 加密保护隐私\n• 私密安全"
            )
            
            // 使用提示
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.str_tips),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = stringResource(R.string.str_feature_tips),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * 功能模块卡片
 */
@Composable
fun FeatureSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                lineHeight = 24.sp
            )
        }
    }
}
