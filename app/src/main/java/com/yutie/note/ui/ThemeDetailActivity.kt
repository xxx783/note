package com.yutie.note.ui

import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.theme.ThemeConfig
import com.yutie.note.theme.ThemeManager
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.SupabaseClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * 主题详情页（社区主题详情）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDetailScreen(
    navController: NavController,
    themeId: String
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // 当前主题配置
    var currentThemeConfig by remember { mutableStateOf(ThemeConfig()) }
    
    LaunchedEffect(Unit) {
        currentThemeConfig = ThemeManager.getThemeConfig(context)
    }
    
    // 从 Supabase 获取真实主题数据
    var themeData by remember { mutableStateOf<CommunityTheme?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        try {
            val theme = fetchThemeFromSupabase(themeId)
            themeData = theme
            isError = (theme == null)
        } catch (e: Exception) {
            android.util.Log.e("ThemeDetail", "获取主题失败", e)
            isError = true
        } finally {
            isLoading = false
        }
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
    
    if (themeData == null || isError) {
        // 主题不存在
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.str_theme_not_found),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        return
    }
    
    // 下载和点赞状态
    var downloadCount by remember { mutableStateOf(themeData?.downloads ?: 0) }
    var likeCount by remember { mutableStateOf(themeData?.likes ?: 0) }
    var isLiked by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }
    
    themeData?.let { theme ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CustomTitleBar(
                title = stringResource(R.string.str_theme_detail),
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // 主题预览卡片
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // 主题名称
                            Text(
                                text = theme.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // 作者信息
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "@${theme.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 颜色预览
                            Text(
                                text = stringResource(R.string.str_color_scheme),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ColorPreviewSection(
                                    label = "背景色",
                                    colorHex = theme.backgroundColor
                                )
                                ColorPreviewSection(
                                    label = "卡片色",
                                    colorHex = theme.surfaceColor
                                )
                                ColorPreviewSection(
                                    label = "文字色",
                                    colorHex = theme.primaryColor
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // 深色模式标识
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (theme.isDark) Icons.Default.NightsStay else Icons.Default.WbSunny,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (theme.isDark) "深色模式" else "浅色模式",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 主题描述
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.str_theme_description),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = theme.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                lineHeight = 24.sp
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 统计信息
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                icon = Icons.Default.CloudDownload,
                                label = stringResource(R.string.str_download),
                                count = downloadCount
                            )
                            
                            Divider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            
                            StatItem(
                                icon = Icons.Default.Favorite,
                                label = stringResource(R.string.str_like),
                                count = likeCount,
                                isLiked = isLiked,
                                onLikeClick = {
                                    val newIsLiked = !isLiked
                                    isLiked = newIsLiked
                                    likeCount += if (newIsLiked) 1 else -1
                                    // 更新数据库中的点赞数
                                    scope.launch {
                                        updateLikeCount(themeId, newIsLiked)
                                    }
                                }
                            )
                        }
                    }
                }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 操作按钮
            item {
                // 应用主题按钮
                Button(
                    onClick = {
                        themeData?.let { theme ->
                            scope.launch {
                                val newConfig = ThemeConfig(
                                    primaryColor = theme.primaryColor,
                                    backgroundColor = theme.backgroundColor,
                                    surfaceColor = theme.surfaceColor,
                                    isDarkMode = theme.isDark,
                                    isCustomTheme = true
                                )
                                ThemeManager.saveThemeConfig(context, newConfig)
                                Toast.makeText(
                                    context,
                                    "已应用：${theme.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isDownloaded = true
                                downloadCount += 1
                                // 更新数据库中的下载计数
                                incrementDownloadCount(themeId)
                                kotlinx.coroutines.delay(300)
                                activity?.recreate()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isDownloaded) stringResource(R.string.str_applied) else stringResource(R.string.str_apply_theme),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 分享按钮
            if (!isDownloaded) {
                item {
                    OutlinedButton(
                        onClick = {
                            // 分享功能
                            Toast.makeText(
                                context,
                                "分享功能敬请期待",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.str_share_theme),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    }
}

/**
 * 颜色预览区域
 */
@Composable
fun ColorPreviewSection(
    label: String,
    colorHex: String
) {
    Column(
        modifier = Modifier.width(IntrinsicSize.Max),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(hexToComposeColor(colorHex))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

/**
 * 统计项
 */
@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    isLiked: Boolean = false,
    onLikeClick: () -> Unit = {}
) {
    if (label == "点赞") {
        // 点赞可以交互
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onLikeClick)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatNumber(count),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    } else {
        // 下载只是显示
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = formatNumber(count),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 从 Supabase 获取主题详情
 */
private suspend fun fetchThemeFromSupabase(themeId: String): CommunityTheme? {
    return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            android.util.Log.d("ThemeDetail", "请求被取消")
        }
        
        val user = SupabaseClient.currentUser
        
        val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/community_themes?id=eq.$themeId"
        
        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user?.token ?: SupabaseClient.SUPABASE_ANON_KEY}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                android.util.Log.e("ThemeDetail", "网络请求失败", e)
                continuation.resumeWith(Result.success(null))
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                android.util.Log.d("ThemeDetail", "响应数据：$responseBody")
                
                try {
                    val jsonArray = org.json.JSONArray(responseBody ?: "[]")
                    if (jsonArray.length() == 0) {
                        continuation.resumeWith(Result.success(null))
                        return
                    }
                    
                    val json = jsonArray.getJSONObject(0)
                    
                    // 从 user_email 提取用户名
                    val userEmail = json.optString("user_email", "")
                    val author = if (userEmail.isNotEmpty()) {
                        userEmail.split("@")[0]
                    } else {
                        "匿名用户"
                    }
                    
                    // 根据主题类型判断深色模式
                    val themeType = json.optString("theme_type", "light")
                    val isDarkMode = themeType == "dark"
                    
                    // 根据深色/浅色模式选择对应的颜色字段
                    val primaryColorKey = if (isDarkMode) "dark_text_color" else "light_text_color"
                    val backgroundColorKey = if (isDarkMode) "dark_background_color" else "light_background_color"
                    val surfaceColorKey = if (isDarkMode) "dark_card_color" else "light_card_color"
                    
                    val theme = CommunityTheme(
                        id = json.getString("id"),
                        name = json.optString("name", "未命名主题"),
                        author = author,
                        description = json.optString("description", "暂无描述"),
                        primaryColor = json.optString(primaryColorKey, "#000000"),
                        backgroundColor = json.optString(backgroundColorKey, "#FFFFFF"),
                        surfaceColor = json.optString(surfaceColorKey, "#F5F5F5"),
                        downloads = json.optInt("download_count", 0),
                        likes = json.optInt("zan", 0),
                        isDark = isDarkMode
                    )
                    
                    android.util.Log.d("ThemeDetail", "获取到主题：${theme.name}")
                    continuation.resumeWith(Result.success(theme))
                } catch (e: Exception) {
                    android.util.Log.e("ThemeDetail", "解析数据失败", e)
                    continuation.resumeWith(Result.success(null))
                }
            }
        })
    }
}

/**
 * 格式化数字
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
 * 更新主题的点赞计数
 */
private suspend fun updateLikeCount(themeId: String, isLiked: Boolean) {
    try {
        val user = SupabaseClient.currentUser ?: return
        
        // 获取当前点赞数
        val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/community_themes?select=zan&id=eq.$themeId"
        
        val getRequest = okhttp3.Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        val currentCount = kotlinx.coroutines.suspendCancellableCoroutine<Int> { continuation ->
            continuation.invokeOnCancellation {
                android.util.Log.d("LikeUpdate", "请求被取消")
            }
            
            SupabaseClient.client.newCall(getRequest).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    if (continuation.isActive) continuation.resumeWith(Result.success(0))
                }
                
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonArray = org.json.JSONArray(responseBody ?: "[]")
                        val currentCount = if (jsonArray.length() > 0) {
                            jsonArray.getJSONObject(0).optInt("zan", 0)
                        } else {
                            0
                        }
                        if (continuation.isActive) continuation.resumeWith(Result.success(currentCount))
                    } catch (e: Exception) {
                        if (continuation.isActive) continuation.resumeWith(Result.success(0))
                    }
                }
            })
        }
        
        // 更新点赞数
        val updateUrl = "${SupabaseClient.SUPABASE_URL}/rest/v1/community_themes?id=eq.$themeId"
        
        val json = JSONObject()
        json.put("zan", currentCount + (if (isLiked) 1 else -1))
        
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        
        val updateRequest = okhttp3.Request.Builder()
            .url(updateUrl)
            .patch(body)
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .build()
        
        SupabaseClient.client.newCall(updateRequest).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                android.util.Log.e("LikeUpdate", "更新点赞失败", e)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    android.util.Log.e("LikeUpdate", "更新点赞失败：${response.code}")
                } else {
                    android.util.Log.d("LikeUpdate", "点赞更新成功")
                }
            }
        })
    } catch (e: Exception) {
        android.util.Log.e("LikeUpdate", "更新点赞异常", e)
    }
}

/**
 * 增加主题的下载计数
 */
private suspend fun incrementDownloadCount(themeId: String) {
    try {
        val user = SupabaseClient.currentUser ?: return
        
        // 1. 获取当前下载数
        val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/community_themes?select=download_count&id=eq.$themeId"
        
        val getRequest = okhttp3.Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        val currentCount = suspendCancellableCoroutine<Int> { continuation ->
            SupabaseClient.client.newCall(getRequest).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    if (continuation.isActive) continuation.resume(0)
                }
                
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonArray = org.json.JSONArray(responseBody ?: "[]")
                        val currentCount = if (jsonArray.length() > 0) {
                            jsonArray.getJSONObject(0).optInt("download_count", 0)
                        } else {
                            0
                        }
                        if (continuation.isActive) continuation.resume(currentCount)
                    } catch (e: Exception) {
                        if (continuation.isActive) continuation.resume(0)
                    }
                }
            })
        }
        
        // 2. 更新下载数
        val updateUrl = "${SupabaseClient.SUPABASE_URL}/rest/v1/community_themes?id=eq.$themeId"
        
        val json = JSONObject()
        json.put("download_count", currentCount + 1)
        
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        
        val updateRequest = okhttp3.Request.Builder()
            .url(updateUrl)
            .patch(body)
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .build()
        
        SupabaseClient.client.newCall(updateRequest).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("ThemeApply", "Failed to update download count", e)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!response.isSuccessful) {
                    Log.e("ThemeApply", "Failed to update download count: ${response.code}")
                } else {
                    Log.d("ThemeApply", "Download count updated successfully")
                }
            }
        })
    } catch (e: Exception) {
        Log.e("ThemeApply", "Failed to update download count", e)
    }
}
