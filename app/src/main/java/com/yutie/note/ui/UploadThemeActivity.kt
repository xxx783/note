package com.yutie.note.ui

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.theme.ThemeConfig
import com.yutie.note.theme.ThemeManager
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.FeatureGuard
import com.yutie.note.utils.SupabaseClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import android.graphics.Color
import android.widget.Toast
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * 上传主题界面（APEX 功能）
 * 将自定义主题分享到社区
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadThemeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    @Suppress("UNUSED_VARIABLE") val activity = context as? Activity
    val scope = rememberCoroutineScope()
    
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
    
    // 上传表单状态
    var themeName by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    
    // 颜色选择器状态
    var showBackgroundColorPicker by remember { mutableStateOf(false) }
    var showSurfaceColorPicker by remember { mutableStateOf(false) }
    var showTextColorPicker by remember { mutableStateOf(false) }
    
    // 主题颜色（可以自定义）
    var themeBackgroundColor by remember { mutableStateOf(currentThemeConfig.backgroundColor) }
    var themeSurfaceColor by remember { mutableStateOf(currentThemeConfig.surfaceColor) }
    var themeTextColor by remember { mutableStateOf("#000000") } // 默认黑色文字
    var themeFontSize by remember { mutableStateOf(16) } // 默认 16sp
    var themeFontWeight by remember { mutableStateOf("normal") } // 默认正常粗细
    var themeIsDark by remember { mutableStateOf(currentThemeConfig.isDarkMode) }
    
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
            featureName = "上传主题",
            currentPlan = "free",
            onUpgradeClick = {
                FeatureGuard.showUpgradeDialog(context, "上传主题")
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
            title = stringResource(R.string.str_share_theme_title),
            showBackButton = true,
            onBackClick = { navController.popBackStack() }
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // APEX 标识卡片
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = stringResource(R.string.str_apex_feature),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(R.string.str_share_your_theme),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 浅色模式配置
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
                        Text(
                            text = stringResource(R.string.str_light_mode_config),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = stringResource(R.string.str_customize_colors),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 颜色选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorPreviewBox(
                                label = "背景色",
                                colorHex = themeBackgroundColor,
                                onClick = { showBackgroundColorPicker = true }
                            )
                            
                            ColorPreviewBox(
                                label = "卡片色",
                                colorHex = themeSurfaceColor,
                                onClick = { showSurfaceColorPicker = true }
                            )
                            
                            ColorPreviewBox(
                                label = "文字色",
                                colorHex = themeTextColor,
                                onClick = { showTextColorPicker = true }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 字体大小
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.str_font_size, themeFontSize),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row {
                                Button(
                                    onClick = { if (themeFontSize > 12) themeFontSize -= 2 },
                                    modifier = Modifier.size(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("-", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { if (themeFontSize < 24) themeFontSize += 2 },
                                    modifier = Modifier.size(40.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+", fontSize = 18.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 字体粗细
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.str_font_weight),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Row {
                                listOf("normal", "bold").forEach { weight ->
                                    FilterChip(
                                        selected = themeFontWeight == weight,
                                        onClick = { themeFontWeight = weight },
                                        label = { Text(weight) },
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 深色模式配置
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
                        Text(
                            text = stringResource(R.string.str_dark_mode_config),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = stringResource(R.string.str_dark_mode_colors),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // 深色模式颜色选择
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ColorPreviewBox(
                                label = "背景色",
                                colorHex = "#121212",
                                onClick = { }
                            )
                            
                            ColorPreviewBox(
                                label = "卡片色",
                                colorHex = "#1E1E1E",
                                onClick = { }
                            )
                            
                            ColorPreviewBox(
                                label = "文字色",
                                colorHex = "#FFFFFF",
                                onClick = { }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = stringResource(R.string.str_dark_mode_auto),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // 主题名称输入
            item {
                OutlinedTextField(
                    value = themeName,
                    onValueChange = { themeName = it },
                    label = { Text(stringResource(R.string.str_theme_name)) },
                    placeholder = { Text(stringResource(R.string.str_theme_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            

            
            // 上传按钮
            item {
                Button(
                    onClick = {
                        if (themeName.isBlank()) {
                            Toast.makeText(context, "请输入主题名称", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        if (!SupabaseClient.isLoggedIn()) {
                            Toast.makeText(context, "请先登录", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isUploading = true
                        scope.launch {
                            try {
                                // 1. 检查用户已上传的主题数量
                                val uploadCount = checkUserUploadCount(context)
                                android.util.Log.d("UploadTheme", "当前上传数量：$uploadCount")
                                
                                if (uploadCount >= 10) {
                                    Toast.makeText(
                                        context,
                                        "您已上传了 10 个主题，达到上限",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    isUploading = false
                                    return@launch
                                }
                                
                                // 2. 上传主题到数据库
                                android.util.Log.d("UploadTheme", "开始上传主题：$themeName")
                                val success = uploadThemeToSupabase(
                                    context = context,
                                    name = themeName,
                                    backgroundColor = themeBackgroundColor,
                                    cardColor = themeSurfaceColor,
                                    textColor = themeTextColor,
                                    fontSize = themeFontSize,
                                    fontWeight = themeFontWeight,
                                    isDark = themeIsDark
                                )
                                
                                android.util.Log.d("UploadTheme", "上传结果：$success")
                                
                                if (success) {
                                    Toast.makeText(
                                        context,
                                        "主题上传成功！\n审核通过后将显示在社区主题中",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    navController.popBackStack()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "上传失败，请检查网络连接或稍后重试",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("UploadTheme", "上传异常", e)
                                Toast.makeText(
                                    context,
                                    "上传失败：${e.message ?: "未知错误"}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            
                            isUploading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isUploading
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("上传中...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.str_upload_to_community),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            // 提示信息
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = stringResource(R.string.str_upload_notice),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "1. 上传的主题需要经过审核才能显示\n2. 请确保主题名称和描述健康向上\n3. 禁止上传侵犯他人权益的主题\n4. 每个 APEX 用户最多可上传 10 个主题",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    
    // 颜色选择器弹窗
    if (showBackgroundColorPicker) {
        ColorPickerDialog(
            currentColor = themeBackgroundColor,
            onColorSelected = { themeBackgroundColor = it },
            onDismiss = { showBackgroundColorPicker = false }
        )
    }
    
    if (showSurfaceColorPicker) {
        ColorPickerDialog(
            currentColor = themeSurfaceColor,
            onColorSelected = { themeSurfaceColor = it },
            onDismiss = { showSurfaceColorPicker = false }
        )
    }
    
    if (showTextColorPicker) {
        ColorPickerDialog(
            currentColor = themeTextColor,
            onColorSelected = { themeTextColor = it },
            onDismiss = { showTextColorPicker = false }
        )
    }
}

/**
 * 颜色预览框
 */
@Composable
fun ColorPreviewBox(
    label: String,
    colorHex: String,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(hexToComposeColor(colorHex))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
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
 * 检查用户已上传的主题数量
 */
private suspend fun checkUserUploadCount(@Suppress("UNUSED_PARAMETER") context: Context): Int {
    return suspendCancellableCoroutine { continuation ->
        val user = SupabaseClient.currentUser ?: run {
            continuation.resume(0)
            return@suspendCancellableCoroutine
        }
        
        val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/community_themes?select=id&user_id=eq.${user.id}"
        
        val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "count=exact")
            .build()
        
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                if (continuation.isActive) continuation.resume(0)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val countHeader = response.header("Content-Range")
                val count = if (countHeader != null) {
                    val parts = countHeader.split("/")
                    if (parts.size > 1 && parts[1] != "*") {
                        parts[1].toIntOrNull() ?: 0
                    } else {
                        0
                    }
                } else {
                    0
                }
                if (continuation.isActive) continuation.resume(count)
            }
        })
    }
}

/**
 * 上传主题到 Supabase
 */
private suspend fun uploadThemeToSupabase(
    @Suppress("UNUSED_PARAMETER") context: Context,
    name: String,
    backgroundColor: String,
    cardColor: String,
    textColor: String,
    fontSize: Int,
    fontWeight: String,
    isDark: Boolean
): Boolean {
    return suspendCancellableCoroutine { continuation ->
        val user = SupabaseClient.currentUser ?: run {
            android.util.Log.e("UploadTheme", "用户未登录")
            if (continuation.isActive) continuation.resume(false)
            return@suspendCancellableCoroutine
        }
        
        val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/community_themes"
        
        val json = JSONObject()
        json.put("user_id", user.id)
        json.put("user_email", user.email)
        json.put("name", name)
        // 浅色模式配置
        json.put("light_background_color", backgroundColor)
        json.put("light_card_color", cardColor)
        json.put("light_text_color", textColor)
        json.put("light_font_size", fontSize)
        json.put("light_font_weight", fontWeight)
        // 深色模式配置（自动生成）
        json.put("dark_background_color", "#121212")
        json.put("dark_card_color", "#1E1E1E")
        json.put("dark_text_color", "#FFFFFF")
        json.put("dark_font_size", fontSize)
        json.put("dark_font_weight", fontWeight)
        
        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        
        android.util.Log.d("UploadTheme", "请求 URL: $url")
        android.util.Log.d("UploadTheme", "请求数据：${json.toString()}")
        
        val request = okhttp3.Request.Builder()
            .url(url)
            .post(body)
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=minimal")
            .build()
        
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                android.util.Log.e("UploadTheme", "网络请求失败", e)
                if (continuation.isActive) continuation.resume(false)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseCode = response.code
                val responseBody = response.body?.string()
                
                android.util.Log.d("UploadTheme", "===== 上传响应 =====")
                android.util.Log.d("UploadTheme", "响应码：$responseCode")
                android.util.Log.d("UploadTheme", "响应头：${response.headers}")
                android.util.Log.d("UploadTheme", "响应体：$responseBody")
                android.util.Log.d("UploadTheme", "===================")
                
                if (!response.isSuccessful) {
                    android.util.Log.e("UploadTheme", "上传失败 - HTTP $responseCode: $responseBody")
                }
                
                val success = response.isSuccessful
                if (continuation.isActive) continuation.resume(success)
            }
        })
    }
}
