package com.yutie.note.ui

import android.app.Activity
import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.db.AppDatabase
import com.yutie.note.theme.ThemeConfig
import com.yutie.note.theme.ThemeManager
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.FeatureGuard
import com.yutie.note.utils.SPUtils
import com.yutie.note.utils.SupabaseClient
import com.yutie.note.utils.SupabaseNoteService
import kotlinx.coroutines.launch

/**
 * 判断颜色是否为深色
 */
private fun isColorDark(colorHex: String): Boolean {
    return try {
        val color = Color.parseColor(colorHex)
        val brightness = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000
        brightness < 128 // 亮度低于 128 认为是深色
    } catch (e: Exception) {
        false
    }
}

/**
 * 反转颜色（深色变浅色，浅色变深色）
 * 简单实现：将 RGB 值反转
 */
private fun invertColor(colorHex: String): String {
    return try {
        val color = Color.parseColor(colorHex)
        val red = 255 - Color.red(color)
        val green = 255 - Color.green(color)
        val blue = 255 - Color.blue(color)
        String.format("#%02X%02X%02X", red, green, blue)
    } catch (e: Exception) {
        colorHex // 如果失败，返回原颜色
    }
}

/**
 * 设置页面（精简版 - 带登录）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val spUtils = remember { SPUtils.getInstance(context) }
    val scope = rememberCoroutineScope()
    
    var themeMode by remember { mutableStateOf(spUtils.getThemeMode()) }
    
    // 获取登录状态 - 使用 derivedStateOf 确保状态变化时 UI 刷新
    var isLoggedIn by remember { mutableStateOf(SupabaseClient.isLoggedIn()) }
    
    // 用于强制刷新 UI 的计数器
    var refreshTrigger by remember { mutableStateOf(0) }
    
    // 同步上传状态
    var isUploading by remember { mutableStateOf(false) }
    // 拉取下载状态
    var isDownloading by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf("") }
    var showSyncDialog by remember { mutableStateOf(false) }
    // 确认清除云端数据弹窗
    var showClearCloudDialog by remember { mutableStateOf(false) }
    
    // 防抖锁 - 防止重复点击
    var isSyncingInProgress by remember { mutableStateOf(false) }
    
    // 获取版本号
    val versionName = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    // 获取 build 号
    val buildNumber = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toString()
        } catch (e: Exception) {
            "0"
        }
    }
    
    // 版本号点击计数（点击 7 次显示 build 号）
    var versionClickCount by remember { mutableStateOf(0) }
    var showBuildNumber by remember { mutableStateOf(false) }
    
    // 获取当前用户（提升作用域）
    val user = SupabaseClient.currentUser
    
    LazyColumn {
        item {
            CustomTitleBar(
                title = stringResource(R.string.str_settings),
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 账户设置
        item {
            SettingsSectionTitle(title = stringResource(R.string.str_account))
        }
        
        item {
            if (isLoggedIn) {
                // 已登录状态
                
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = user?.email ?: stringResource(R.string.str_user),
                    subtitle = "${stringResource(R.string.str_logged_in)}\nUID: ${user?.id ?: ""}",
                    selected = false,
                    onClick = {
                        // 显示账户详情或登出选项
                    }
                )
                
                // 同步按钮
                SettingsItem(
                    icon = Icons.Default.Sync,
                    title = stringResource(R.string.str_sync_notes),
                    subtitle = if (isUploading) stringResource(R.string.str_syncing) else stringResource(R.string.str_sync_notes_to_cloud),
                    selected = false,
                    enabled = !isUploading && !isDownloading && !isSyncingInProgress,
                    onClick = {
                        // 防抖检查
                        if (isSyncingInProgress) return@SettingsItem
                        isSyncingInProgress = true
                        isUploading = true
                        scope.launch {
                            val database = AppDatabase.getInstance(context)
                            val notes = database.noteDao().getAllNotesList()
                            SupabaseNoteService.syncAllNotes(notes) { result, idMapping ->
                                scope.launch {
                                    // 更新本地笔记的云端ID
                                    idMapping.forEach { (localId, cloudId) ->
                                        val note = database.noteDao().getNoteById(localId)
                                        note?.let {
                                            database.noteDao().updateNote(
                                                it.copy(cloudId = cloudId, isDraft = false)
                                            )
                                        }
                                    }
                                    isUploading = false
                                    isSyncingInProgress = false
                                    syncMessage = result.message
                                    showSyncDialog = true
                                }
                            }
                        }
                    }
                )

                // 拉取云端笔记按钮
                SettingsItem(
                    icon = Icons.Default.CloudDownload,
                    title = stringResource(R.string.str_pull_cloud_notes),
                    subtitle = if (isDownloading) stringResource(R.string.str_pulling) else stringResource(R.string.str_pull_cloud_notes_subtitle),
                    selected = false,
                    enabled = !isUploading && !isDownloading,
                    onClick = {
                        isDownloading = true
                        SupabaseNoteService.downloadNotes(callback = { response ->
                            scope.launch {
                                if (response.success && response.notes != null) {
                                    val database = AppDatabase.getInstance(context)
                                    var downloadCount = 0
                                    var skipCount = 0
                                    var updateContentCount = 0

                                    // 先获取所有本地笔记用于比对
                                    val localNotes = database.noteDao().getAllNotesList()

                                    response.notes.forEach { cloudNote ->
                                        // 跳过已删除的笔记
                                        if (cloudNote.is_deleted) {
                                            return@forEach
                                        }

                                        // 通过 cloudId 查找本地笔记
                                        val existingNote = localNotes.find { it.cloudId == cloudNote.id }

                                        if (existingNote != null) {
                                            // cloudId 相同，检查内容是否一致
                                            if (existingNote.title == cloudNote.title &&
                                                existingNote.content == cloudNote.content) {
                                                // 内容完全一致，跳过
                                                skipCount++
                                            } else {
                                                // cloudId 相同但内容不同，更新本地笔记内容
                                                val updatedNote = existingNote.copy(
                                                    title = cloudNote.title,
                                                    content = cloudNote.content,
                                                    modifyTime = parseTimestamp(cloudNote.update_time),
                                                    isDeleted = cloudNote.is_deleted
                                                )
                                                database.noteDao().updateNote(updatedNote)
                                                updateContentCount++
                                            }
                                        } else {
                                            // 不存在，新建笔记（云端ID保存到 cloudId 字段）
                                            val note = com.yutie.note.bean.NoteBean(
                                                id = 0, // 本地自增ID
                                                cloudId = cloudNote.id, // 云端ID
                                                title = cloudNote.title,
                                                content = cloudNote.content,
                                                createTime = parseTimestamp(cloudNote.create_time),
                                                modifyTime = parseTimestamp(cloudNote.update_time),
                                                isDeleted = cloudNote.is_deleted,
                                                isDraft = false
                                            )
                                            database.noteDao().insertNote(note)
                                            downloadCount++
                                        }
                                    }
                                    isDownloading = false
                                    syncMessage = "拉取完成：下载 $downloadCount 条，更新内容 $updateContentCount 条，跳过 $skipCount 条"
                                    showSyncDialog = true
                                } else {
                                    isDownloading = false
                                    syncMessage = "拉取失败：${response.error ?: "未知错误"}"
                                    showSyncDialog = true
                                }
                            }
                        })
                    }
                )
                
                // 清除云端数据按钮
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = stringResource(R.string.str_clear_cloud_data),
                    subtitle = stringResource(R.string.str_clear_cloud_data_subtitle),
                    selected = false,
                    enabled = !isUploading && !isDownloading,
                    onClick = {
                        showClearCloudDialog = true
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.Logout,
                    title = stringResource(R.string.str_logout),
                    subtitle = stringResource(R.string.str_logout_subtitle),
                    selected = false,
                    onClick = {
                        SupabaseClient.logout()
                        isLoggedIn = false
                        refreshTrigger++ // 触发 UI 刷新
                    }
                )
            } else {
                // 未登录状态
                SettingsItem(
                    icon = Icons.Default.Login,
                    title = stringResource(R.string.str_login),
                    subtitle = stringResource(R.string.str_login_subtitle),
                    selected = false,
                    onClick = {
                        navController.navigate("login")
                    }
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 主题设置
        item {
            SettingsSectionTitle(title = stringResource(R.string.str_theme))
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.WbSunny,
                title = stringResource(R.string.str_light_mode),
                selected = themeMode == 1,
                onClick = {
                    themeMode = 1
                    spUtils.setThemeMode(1)
                    // 如果有自定义主题，重置为浅色模式
                    scope.launch {
                        try {
                            val config = ThemeManager.getThemeConfig(context)
                            if (config.isCustomTheme) {
                                // 如果当前背景色是深色，自动切换为浅色版本
                                val isDarkBackground = isColorDark(config.backgroundColor)
                                if (isDarkBackground) {
                                    val newConfig = config.copy(
                                        isDarkMode = false,
                                        backgroundColor = invertColor(config.backgroundColor),
                                        surfaceColor = invertColor(config.surfaceColor)
                                    )
                                    ThemeManager.saveThemeConfig(context, newConfig)
                                } else {
                                    val newConfig = config.copy(isDarkMode = false)
                                    ThemeManager.saveThemeConfig(context, newConfig)
                                }
                            }
                        } catch (e: Exception) {
                            // 忽略错误
                        }
                    }
                    activity?.recreate()
                }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.NightsStay,
                title = stringResource(R.string.str_dark_mode),
                selected = themeMode == 2,
                onClick = {
                    themeMode = 2
                    spUtils.setThemeMode(2)
                    // 如果有自定义主题，重置为深色模式
                    scope.launch {
                        try {
                            val config = ThemeManager.getThemeConfig(context)
                            if (config.isCustomTheme) {
                                // 如果当前背景色是浅色，自动切换为深色版本
                                val isDarkBackground = isColorDark(config.backgroundColor)
                                if (!isDarkBackground) {
                                    val newConfig = config.copy(
                                        isDarkMode = true,
                                        backgroundColor = invertColor(config.backgroundColor),
                                        surfaceColor = invertColor(config.surfaceColor)
                                    )
                                    ThemeManager.saveThemeConfig(context, newConfig)
                                } else {
                                    val newConfig = config.copy(isDarkMode = true)
                                    ThemeManager.saveThemeConfig(context, newConfig)
                                }
                            }
                        } catch (e: Exception) {
                            // 忽略错误
                        }
                    }
                    activity?.recreate()
                }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Contrast,
                title = stringResource(R.string.str_follow_system),
                selected = themeMode == 0,
                onClick = {
                    themeMode = 0
                    spUtils.setThemeMode(0)
                    activity?.recreate()
                }
            )
        }
        
        // 主题编辑器（Pro 功能）
        item {
            var isProUser by remember { mutableStateOf(false) }
            var isApexUser by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(Unit) {
                val currentPlan = FeatureGuard.getCurrentPlan()
                isProUser = currentPlan == "pro" || currentPlan == "apex"
                isApexUser = currentPlan == "apex"
                isLoading = false
            }
            
            if (!isLoading && isProUser) {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = stringResource(R.string.str_theme_editor),
                    subtitle = stringResource(R.string.str_theme_editor_subtitle),
                    selected = false,
                    onClick = {
                        navController.navigate("theme_editor")
                    }
                )
                
                SettingsItem(
                    icon = Icons.Default.ShoppingBag,
                    title = stringResource(R.string.str_official_theme_store),
                    subtitle = stringResource(R.string.str_official_theme_store_subtitle),
                    selected = false,
                    onClick = {
                        navController.navigate("theme_store")
                    }
                )
            }
            
            if (!isLoading && isApexUser) {
                 SettingsItem(
                     icon = Icons.Default.People,
                     title = stringResource(R.string.str_community_themes),
                     subtitle = stringResource(R.string.str_community_themes_subtitle),
                     selected = false,
                     onClick = {
                         navController.navigate("community_theme")
                     }
                 )
             }
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 关于
        item {
            SettingsSectionTitle(title = stringResource(R.string.str_about))
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Info,
                title = stringResource(R.string.str_version),
                subtitle = if (showBuildNumber) "$versionName (Build: $buildNumber)" else versionName,
                onClick = {
                    versionClickCount++
                    if (versionClickCount >= 7) {
                        showBuildNumber = true
                        versionClickCount = 0
                    }
                }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.MenuBook,
                title = stringResource(R.string.str_feature_guide),
                subtitle = stringResource(R.string.str_feature_guide_subtitle),
                onClick = {
                    navController.navigate("featureGuide")
                }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Star,
                title = stringResource(R.string.str_request_upgrade),
                subtitle = stringResource(R.string.str_request_upgrade_subtitle),
                onClick = {
                    navController.navigate("upgrade_application")
                }
            )
        }
        
        // 管理员专属：发布公告
        if (user?.email == "makabaka204@gmail.com") {
            item {
                SettingsItem(
                    icon = Icons.Default.Campaign,
                    title = stringResource(R.string.str_publish_announcement),
                    subtitle = stringResource(R.string.str_publish_announcement_subtitle),
                    selected = false,
                    onClick = {
                        navController.navigate("publish_announcement")
                    }
                )
            }
        }
        
        // 历史公告（所有人可见）
        item {
            SettingsItem(
                icon = Icons.Default.History,
                title = stringResource(R.string.str_history_announcements),
                subtitle = stringResource(R.string.str_history_announcements_subtitle),
                selected = false,
                onClick = {
                    navController.navigate("history_announcements")
                }
            )
        }
        
        // 问题反馈
        item {
            SettingsItem(
                icon = Icons.Default.Feedback,
                title = stringResource(R.string.str_feedback),
                subtitle = stringResource(R.string.str_feedback_subtitle),
                selected = false,
                onClick = {
                    navController.navigate("feedback")
                }
            )
        }
        
        // 检查更新
        item {
            SettingsItem(
                icon = Icons.Default.SystemUpdate,
                title = stringResource(R.string.str_check_update),
                subtitle = if (showBuildNumber) "$versionName (Build: $buildNumber)" else stringResource(R.string.str_current_version) + ": $versionName",
                selected = false,
                onClick = {
                    navController.navigate("check_update")
                }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // 法律信息
        item {
            SettingsSectionTitle(title = stringResource(R.string.str_privacy))
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Description,
                title = stringResource(R.string.str_user_agreement),
                subtitle = stringResource(R.string.str_user_agreement_subtitle),
                onClick = {
                    navController.navigate("user_agreement")
                }
            )
        }
        
        item {
            SettingsItem(
                icon = Icons.Default.Security,
                title = stringResource(R.string.str_privacy_policy),
                subtitle = stringResource(R.string.str_privacy_policy_subtitle),
                onClick = {
                    navController.navigate("privacy_policy")
                }
            )
        }
        
        // 语言设置
        item {
            SettingsItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.str_language),
                subtitle = stringResource(R.string.str_language_auto),
                onClick = {
                    navController.navigate("language_settings")
                }
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // 同步结果弹窗
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = { Text(stringResource(R.string.str_success)) },
            text = { Text(syncMessage) },
            confirmButton = {
                TextButton(onClick = { showSyncDialog = false }) {
                    Text(stringResource(R.string.str_confirm))
                }
            }
        )
    }
    
    // 清除云端数据确认弹窗
    if (showClearCloudDialog) {
        AlertDialog(
            onDismissRequest = { showClearCloudDialog = false },
            title = { Text(stringResource(R.string.str_clear_cloud_data_confirm)) },
            text = { Text(stringResource(R.string.str_clear_cloud_data_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCloudDialog = false
                        isUploading = true
                        SupabaseNoteService.clearAllCloudNotes { _, message ->
                            isUploading = false
                            syncMessage = message ?: "Success"
                            showSyncDialog = true
                        }
                    }
                ) {
                    Text(stringResource(R.string.str_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCloudDialog = false }) {
                    Text(stringResource(R.string.str_cancel))
                }
            }
        )
    }
}

/**
 * 解析 ISO 8601 时间字符串为时间戳
 */
private fun parseTimestamp(isoTime: String): Long {
    return try {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        sdf.parse(isoTime)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

/**
 * 设置分组标题
 */
@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * 设置项
 */
@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
