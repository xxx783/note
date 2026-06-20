package com.yutie.note.ui

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
 * 发布公告页面 - 仅管理员可用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishAnnouncementScreen(
    navController: NavController
) {
    val context = LocalContext.current
    @Suppress("UNUSED_VARIABLE") val activity = context as? Activity
    @Suppress("UNUSED_VARIABLE") val scope = rememberCoroutineScope()
    
    // 当前主题配置
    var currentThemeConfig by remember { mutableStateOf(ThemeConfig()) }
    
    LaunchedEffect(Unit) {
        currentThemeConfig = ThemeManager.getThemeConfig(context)
    }
    
    // 检查是否为管理员
    val user = SupabaseClient.currentUser
    if (user?.email != "makabaka204@gmail.com") {
        // 非管理员显示提示
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = context.getString(R.string.str_admin_only),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        return
    }
    
    // 公告表单
    PublishAnnouncementForm(
        currentThemeConfig = currentThemeConfig,
        onBackClick = { navController.popBackStack() },
        onSubmitSuccess = {
            Toast.makeText(
                context,
                context.getString(R.string.str_publish_success),
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    )
}

/**
 * 发布公告表单
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishAnnouncementForm(
    @Suppress("UNUSED_PARAMETER") currentThemeConfig: ThemeConfig,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 表单状态
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTitleBar(
            title = context.getString(R.string.str_publish_announcement_title),
            showBackButton = true,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 管理员提示
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = context.getString(R.string.str_admin_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 公告标题
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(context.getString(R.string.str_announcement_title_label)) },
            placeholder = { Text(context.getString(R.string.str_announcement_title_hint)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Title,
                    contentDescription = null
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 公告内容
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text(context.getString(R.string.str_announcement_content_label)) },
            placeholder = { Text(context.getString(R.string.str_announcement_content_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            maxLines = 10,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 重要公告开关
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isImportant) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = context.getString(R.string.str_important_announcement),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isImportant) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = context.getString(R.string.str_important_announcement_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isImportant) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }
                Switch(
                    checked = isImportant,
                    onCheckedChange = { isImportant = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = if (isImportant) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        checkedTrackColor = if (isImportant) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 提交按钮
        Button(
            onClick = {
                if (title.isBlank()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.str_fill_title),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                if (content.isBlank()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.str_fill_content),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                
                isSubmitting = true
                scope.launch {
                    try {
                        val success = publishAnnouncement(
                            context = context,
                            title = title,
                            content = content,
                            isImportant = isImportant
                        )
                        
                        if (success) {
                            onSubmitSuccess()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.str_publish_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.str_publish_failed) + "：" + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    } finally {
                        isSubmitting = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(context.getString(R.string.str_publishing))
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.str_publish),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 发布公告到 Supabase gonggao 表
 */
private suspend fun publishAnnouncement(
    @Suppress("UNUSED_PARAMETER") context: android.content.Context,
    title: String,
    content: String,
    isImportant: Boolean
): Boolean {
    @Suppress("UNUSED_VARIABLE") val user = SupabaseClient.currentUser ?: throw Exception("用户未登录")
    
    // 先刷新 Token，避免过期
    println("=== 刷新 Token ===")
    val tokenRefreshed = suspendCancellableCoroutine<Boolean> { continuation ->
        SupabaseClient.refreshToken { success, _ ->
            if (continuation.isActive) continuation.resume(success)
        }
    }
    
    if (!tokenRefreshed) {
        println("Token 刷新失败，尝试使用旧 Token")
    }
    
    // 获取最新的 Token
    val updatedUser = SupabaseClient.currentUser ?: throw Exception("用户未登录")
    
    val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/gonggao"
    
    println("=== 发布公告 ===")
    println("URL: $url")
    println("User ID: ${updatedUser.id}")
    println("User Email: ${updatedUser.email}")
    
    val json = JSONObject()
    json.put("title", title)
    json.put("content", content)
    json.put("is_important", isImportant)
    json.put("publisher_id", updatedUser.id)
    json.put("publisher_email", updatedUser.email)
    // 不传 created_at，让数据库自动设置
    
    println("Request JSON: $json")
    
    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    
    val request = okhttp3.Request.Builder()
        .url(url)
        .post(body)
        .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${updatedUser.token}")
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "return=representation")
        .build()
    
    return suspendCancellableCoroutine { continuation ->
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                println("=== 发布失败 - 网络错误 ===")
                e.printStackTrace()
                if (continuation.isActive) continuation.resume(false)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val success = response.isSuccessful
                val responseBody = response.body?.string()
                
                println("=== 发布响应 ===")
                println("Success: $success")
                println("Response Code: ${response.code}")
                println("Response Body: $responseBody")
                
                if (!success) {
                    println("发布失败！HTTP ${response.code}")
                    println("错误信息：$responseBody")
                }
                
                if (continuation.isActive) continuation.resume(success)
            }
        })
    }
}
