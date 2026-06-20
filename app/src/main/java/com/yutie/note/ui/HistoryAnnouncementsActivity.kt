package com.yutie.note.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.bean.AnnouncementBean
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.SupabaseClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * 历史公告页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryAnnouncementsScreen(
    navController: NavController
) {
    val context = LocalContext.current
    @Suppress("UNUSED_VARIABLE") val scope = rememberCoroutineScope()
    
    // 公告列表
    var announcements by remember { mutableStateOf<List<AnnouncementBean>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        loadAnnouncements { list ->
            announcements = list
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTitleBar(
            title = stringResource(R.string.str_history_announcements),
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (announcements.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.str_no_announcements),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(announcements) { announcement ->
                    AnnouncementCard(announcement = announcement)
                }
            }
        }
    }
}

/**
 * 公告卡片 - 使用笔记卡片样式
 */
@Composable
fun AnnouncementCard(
    announcement: AnnouncementBean,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // 点击时的缩放动画状态
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                isExpanded = !isExpanded
                isPressed = false
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (announcement.isImportant) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "重要",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    Text(
                        text = announcement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.str_publish_time) + announcement.formattedTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isExpanded) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(animationSpec = tween(300))
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = announcement.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = stringResource(R.string.str_publisher) + if (announcement.publisherEmail.isNotEmpty()) announcement.publisherEmail else stringResource(R.string.str_admin),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 加载公告列表
 */
private suspend fun loadAnnouncements(
    onLoadComplete: (List<AnnouncementBean>) -> Unit
) {
    val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/gonggao"
    
    println("=== 历史公告 - 请求开始 ===")
    println("URL: $url")
    
    val request = okhttp3.Request.Builder()
        .url(url)
        .get()
        .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "order=created_at.desc") // 按时间倒序排列
        .build()
    
    println("请求头已添加：apikey")
    
    val response = suspendCancellableCoroutine<okhttp3.Response?> { continuation ->
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                println("=== 历史公告 - 网络错误 ===")
                e.printStackTrace()
                if (continuation.isActive) continuation.resume(null)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (continuation.isActive) continuation.resume(response)
            }
        })
    }
    
    response?.use {
        println("=== 历史公告 - 响应结果 ===")
        println("Success: ${it.isSuccessful}")
        println("Response Code: ${it.code}")
        
        val bodyString = it.body?.string()
        println("Response Body: $bodyString")
        
        if (it.isSuccessful) {
            if (!bodyString.isNullOrBlank()) {
                try {
                    val jsonArray = JSONArray(bodyString)
                    println("公告数量：${jsonArray.length()}")
                    val list = mutableListOf<AnnouncementBean>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val bean = AnnouncementBean(
                            id = json.optLong("id", 0),
                            title = json.optString("title", "系统公告"),
                            content = json.optString("content", ""),
                            isImportant = json.optBoolean("is_important", false),
                            publisherId = json.optString("publisher_id", ""),
                            publisherEmail = json.optString("publisher_email", ""),
                            createdAt = json.optString("created_at", "")
                        )
                        list.add(bean)
                        println("公告 $i: ${bean.title} - ${bean.formattedTime}")
                    }
                    
                    onLoadComplete(list)
                    return
                } catch (e: Exception) {
                    println("=== 历史公告 - JSON 解析错误 ===")
                    e.printStackTrace()
                }
            } else {
                println("=== 历史公告 - 响应体为空 ===")
            }
        } else {
            println("=== 历史公告 - 请求失败 ===")
            println("错误信息：$bodyString")
        }
        
        onLoadComplete(emptyList())
    }
}
