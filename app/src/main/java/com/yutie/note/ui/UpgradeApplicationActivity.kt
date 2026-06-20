package com.yutie.note.ui

import android.app.Activity
import android.provider.Settings
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
import com.yutie.note.utils.FeatureGuard
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * 升级申请页面
 * 允许 Free 和 Pro 用户申请升级到更高版本
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeApplicationScreen(
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
    
    // 获取当前用户计划
    var currentUserPlan by remember { mutableStateOf("free") }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        try {
            val plan = FeatureGuard.getCurrentPlan()
            currentUserPlan = plan
        } catch (e: Exception) {
            currentUserPlan = "free"
        } finally {
            isLoading = false
        }
    }
    
    // APEX 用户直接显示提示
    if (!isLoading && currentUserPlan == "apex") {
        ApexNoNeedUpgradeScreen(
            currentThemeConfig = currentThemeConfig,
            onBackClick = { navController.popBackStack() }
        )
        return
    }
    
    // 加载状态
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    // 申请表单
    UpgradeApplicationForm(
        currentThemeConfig = currentThemeConfig,
        currentUserPlan = currentUserPlan,
        onBackClick = { navController.popBackStack() },
        onSubmitSuccess = {
            Toast.makeText(
                context,
                "✓ 申请已提交，请等待审核结果",
                Toast.LENGTH_LONG
            ).show()
            navController.popBackStack()
        }
    )
}

/**
 * APEX 用户无需升级提示页面
 */
@Composable
fun ApexNoNeedUpgradeScreen(
    @Suppress("UNUSED_PARAMETER") currentThemeConfig: ThemeConfig,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        CustomTitleBar(
            title = "申请升级",
            showBackButton = true,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = context.getString(R.string.str_already_apex),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = context.getString(R.string.str_already_apex_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 28.sp
                )
            }
        }
    }
}

/**
 * 升级申请表单
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpgradeApplicationForm(
    @Suppress("UNUSED_PARAMETER") currentThemeConfig: ThemeConfig,
    currentUserPlan: String,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 表单状态
    var targetPlan by remember { mutableStateOf("pro") } // 默认申请 Pro
    var email by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    
    // 根据当前计划决定可选的目标计划
    val availablePlans = if (currentUserPlan == "free") {
        listOf("pro" to "Pro 版", "apex" to "APEX 版")
    } else {
        listOf("apex" to "APEX 版") // Pro 用户只能申请 APEX
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomTitleBar(
            title = context.getString(R.string.str_upgrade_application),
            showBackButton = true,
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 当前计划提示
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
                Text(
                    text = context.getString(R.string.str_current_plan),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = when (currentUserPlan) {
                        "free" -> "免费版"
                        "pro" -> "Pro 版"
                        else -> "未知"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = context.getString(R.string.str_upgrade_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 目标计划选择
        Text(
            text = context.getString(R.string.str_target_plan),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        availablePlans.forEach { (planValue, planLabel) ->
            FilterChip(
                selected = targetPlan == planValue,
                onClick = { targetPlan = planValue },
                label = { Text(planLabel) },
                modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                leadingIcon = if (targetPlan == planValue) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 邮箱输入
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(context.getString(R.string.str_contact_email)) },
            placeholder = { Text(context.getString(R.string.str_contact_email_hint)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = context.getString(R.string.str_email_optional),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 申请理由
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text(context.getString(R.string.str_upgrade_reason)) },
            placeholder = { Text(context.getString(R.string.str_upgrade_reason_hint)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 6,
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = context.getString(R.string.str_upgrade_reason_optional),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 提交按钮
        Button(
            onClick = {
                if (email.isBlank() && !SupabaseClient.isLoggedIn()) {
                    Toast.makeText(
                        context,
                        "请先登录或填写邮箱",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@Button
                }
                
                isSubmitting = true
                scope.launch {
                    try {
                        val success = submitUpgradeApplication(
                            context = context,
                            email = email,
                            targetPlan = targetPlan,
                            reason = reason
                        )
                        
                        if (success) {
                            onSubmitSuccess()
                        } else {
                            Toast.makeText(
                                context,
                                context.getString(R.string.str_submit_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.str_submit_failed) + "：" + e.message,
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
                Text(context.getString(R.string.str_submitting))
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = context.getString(R.string.str_submit),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

/**
 * 提交升级申请到 Supabase
 */
private suspend fun submitUpgradeApplication(
    context: android.content.Context,
    email: String,
    targetPlan: String,
    reason: String
): Boolean {
    val user = SupabaseClient.currentUser ?: throw Exception("用户未登录")
    
    // 获取设备 UUID
    val deviceUuid = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ANDROID_ID
    )
    
    val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/upgrade_applications"
    
    val json = JSONObject()
    json.put("user_id", user.id)
    json.put("email", email)
    json.put("target_plan", targetPlan)
    json.put("reason", reason)
    json.put("status", "pending")
    json.put("UUID(fs)", deviceUuid)  // 添加设备 UUID
    
    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
    
    val request = okhttp3.Request.Builder()
        .url(url)
        .post(body)
        .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
        .addHeader("Authorization", "Bearer ${user.token}")
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "return=representation")
        .build()
    
    return suspendCancellableCoroutine { continuation ->
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                if (continuation.isActive) continuation.resume(false)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val success = response.isSuccessful
                if (continuation.isActive) continuation.resume(success)
            }
        })
    }
}
