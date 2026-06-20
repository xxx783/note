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
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.theme.ThemeConfig
import com.yutie.note.theme.ThemeManager
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.FeatureGuard
import com.yutie.note.utils.SupabaseClient
import kotlinx.coroutines.launch
import android.graphics.Color
import androidx.compose.ui.res.stringResource
import com.yutie.note.R

/**
 * 社区主题列表界面（APEX 功能）
 * 浏览和下载其他用户分享的自定义主题
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityThemeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    @Suppress("UNUSED_VARIABLE") val activity = context as? Activity
    @Suppress("UNUSED_VARIABLE") val scope = rememberCoroutineScope()
    
    // 检查权限 - APEX 专属功能
    var hasPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        val currentPlan = FeatureGuard.getCurrentPlan()
        hasPermission = currentPlan == "apex"
        isLoading = false
    }
    
    // 当前主题配置
    var currentThemeConfig by remember { mutableStateOf(ThemeConfig()) }
    
    LaunchedEffect(Unit) {
        currentThemeConfig = ThemeManager.getThemeConfig(context)
    }
    
    // 从 Supabase 获取真实的社区主题数据
    var communityThemes by remember { mutableStateOf<List<CommunityTheme>>(emptyList()) }
    var isFetchingThemes by remember { mutableStateOf(true) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            val themes = fetchCommunityThemesFromSupabase()
            communityThemes = themes
            fetchError = null
        } catch (e: Exception) {
            android.util.Log.e("CommunityTheme", "获取主题失败", e)
            fetchError = e.message ?: "获取失败"
        } finally {
            isFetchingThemes = false
        }
    }
    
    // 如果没有权限，显示升级提示
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
        ApexPermissionDeniedScreen(
            featureName = "社区主题",
            currentPlan = "free",
            onUpgradeClick = {
                FeatureGuard.showUpgradeDialog(context, "社区主题")
            },
            onBackClick = { navController.popBackStack() }
        )
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTitleBar(
            title = stringResource(R.string.str_community_themes),
            showBackButton = true,
            onBackClick = { navController.popBackStack() },
            rightIcon = Icons.Default.Add,
            onRightClick = {
                navController.navigate("upload_theme")
            }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 说明卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
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
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = stringResource(R.string.str_community_featured),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(R.string.str_community_discover),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 加载状态
            if (isFetchingThemes) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.str_loading_community),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else if (fetchError != null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.str_load_failed),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = fetchError ?: "未知错误",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else if (communityThemes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.str_no_community_themes),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.str_upload_first_theme),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                // 主题网格
                item {
                    Text(
                        text = stringResource(R.string.str_popular_themes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(
                            ((communityThemes.size + 1) / 2 * 220).dp
                        )
                    ) {
                        items(communityThemes) { theme ->
                            CommunityThemeCard(
                                theme = theme,
                                onClick = {
                                    navController.navigate("theme_detail/${theme.id}")
                                }
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

/**
 * 社区主题数据类
 */
data class CommunityTheme(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val primaryColor: String,
    val backgroundColor: String,
    val surfaceColor: String,
    val downloads: Int,
    val likes: Int,
    val isDark: Boolean
)

/**
 * 社区主题卡片
 */
@Composable
fun CommunityThemeCard(
    theme: CommunityTheme,
    onClick: () -> Unit
) {
    // 使用中性灰色作为卡片背景，避免与主题颜色冲突
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 颜色预览区域 - 显示主题的真实配色
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(hexToComposeColor(theme.backgroundColor))
                        .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(hexToComposeColor(theme.surfaceColor))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(hexToComposeColor(theme.primaryColor))
                        .clip(RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = theme.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "@${theme.author}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = formatNumber(theme.downloads),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatNumber(theme.likes),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * 格式化数字（超过 1000 显示 k）
 */
private fun formatNumber(num: Int): String {
    return if (num >= 1000) {
        String.format("%.1fk", num / 1000.0)
    } else {
        num.toString()
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

/**
 * 从 Supabase 获取社区主题列表
 */
private suspend fun fetchCommunityThemesFromSupabase(): List<CommunityTheme> {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            android.util.Log.d("CommunityTheme", "请求被取消")
        }
        
        val user = SupabaseClient.currentUser
        
        // 构建 URL，获取所有主题，按创建时间倒序
        val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/community_themes?order=created_at.desc"
        
        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user?.token ?: SupabaseClient.SUPABASE_ANON_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                android.util.Log.e("CommunityTheme", "网络请求失败", e)
                continuation.resumeWith(Result.success(emptyList()))
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                android.util.Log.d("CommunityTheme", "响应数据：$responseBody")
                
                try {
                    val jsonArray = org.json.JSONArray(responseBody ?: "[]")
                    val themes = mutableListOf<CommunityTheme>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        
                        // 从 user_email 提取用户名（@前面的部分）
                        val userEmail = json.optString("user_email", "")
                        val author = if (userEmail.isNotEmpty()) {
                            userEmail.split("@")[0]
                        } else {
                            "匿名用户"
                        }
                        
                        themes.add(
                            CommunityTheme(
                                id = json.getString("id"),
                                name = json.optString("name", "未命名主题"),
                                author = author,
                                description = json.optString("description", "暂无描述"),
                                primaryColor = json.optString("light_text_color", "#000000"),
                                backgroundColor = json.optString("light_background_color", "#FFFFFF"),
                                surfaceColor = json.optString("light_card_color", "#F5F5F5"),
                                downloads = json.optInt("download_count", 0),
                                likes = json.optInt("zan", 0),
                                isDark = false
                            )
                        )
                    }
                    
                    android.util.Log.d("CommunityTheme", "获取到 ${themes.size} 个主题")
                    continuation.resumeWith(Result.success(themes))
                } catch (e: Exception) {
                    android.util.Log.e("CommunityTheme", "解析数据失败", e)
                    continuation.resumeWith(Result.success(emptyList()))
                }
            }
        })
    }
}
