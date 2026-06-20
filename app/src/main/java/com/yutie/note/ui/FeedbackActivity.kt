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
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.SupabaseClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.time.LocalTime
import kotlin.coroutines.resume

/**
 * 反馈页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    navController: NavController
) {
    val context = LocalContext.current
    @Suppress("UNUSED_VARIABLE") val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
    // 表单状态
    var feedbackContent by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // 获取当前用户
    val user = SupabaseClient.currentUser
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTitleBar(
            title = stringResource(R.string.str_feedback_title),
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 说明卡片
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Feedback,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.str_feedback_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 反馈内容输入框
        OutlinedTextField(
            value = feedbackContent,
            onValueChange = { feedbackContent = it },
            label = { Text(stringResource(R.string.str_feedback_content)) },
            placeholder = { Text(stringResource(R.string.str_feedback_content_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            maxLines = 8,
            keyboardOptions = KeyboardOptions.Default,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 邮箱输入框（选填）
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.str_feedback_email)) },
            placeholder = { Text(stringResource(R.string.str_feedback_email_hint)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            maxLines = 1,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            supportingText = {
                Text(
                    text = if (user != null) "已使用您的登录邮箱" else "建议填写邮箱，方便我们回复您",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 提交按钮
        Button(
            onClick = {
                if (feedbackContent.isBlank()) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.str_feedback_fill_content),
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                
                isSubmitting = true
                scope.launch {
                    try {
                        val success = submitFeedback(
                            context = context,
                            feedbackContent = feedbackContent,
                            email = email.ifBlank { null }
                        )
                        
                        if (success) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.str_feedback_submit_success),
                                Toast.LENGTH_LONG
                            ).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.str_feedback_submit_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.str_feedback_submit_failed) + "：" + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        e.printStackTrace()
                    }
                    
                    isSubmitting = false
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
                Text(stringResource(R.string.str_submitting))
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.str_submit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 提交反馈到 Supabase user_fk 表 - 所有用户（包括未登录）都可以提交
 */
private suspend fun submitFeedback(
    @Suppress("UNUSED_PARAMETER") context: android.content.Context,
    feedbackContent: String,
    email: String?
): Boolean {
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
    val user = SupabaseClient.currentUser
    
    val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/user_fk"
    
    println("=== 提交反馈 ===")
    println("URL: $url")
    println("User ID: ${user?.id ?: "未登录"}")
    println("反馈内容：$feedbackContent")
    println("邮箱：$email")
    
    val json = JSONObject()
    json.put("nr", feedbackContent)
    
    // 如果有邮箱，就提交邮箱
    if (email != null) {
        json.put("email", email)
    } else if (user != null) {
        // 如果用户已登录，使用登录邮箱
        json.put("email", user.email)
    }
    
    // 添加当前时间
    val currentTime = LocalTime.now()
    val timeString = currentTime.toString().substring(0, 8) // HH:mm:ss
    json.put("time", timeString)
    
    println("Request JSON: $json")
    
    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    
    val request = okhttp3.Request.Builder()
        .url(url)
        .post(body)
        .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "return=representation")
        .build()
    
    return suspendCancellableCoroutine { continuation ->
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                println("=== 反馈提交 - 网络错误 ===")
                e.printStackTrace()
                if (continuation.isActive) continuation.resume(false)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseCode = response.code
                val responseBody = response.body?.string()
                
                println("=== 反馈提交 - 响应结果 ===")
                println("Success: ${response.isSuccessful}")
                println("Response Code: $responseCode")
                println("Response Body: $responseBody")
                
                if (continuation.isActive) {
                    continuation.resume(response.isSuccessful)
                }
            }
        })
    }
}
