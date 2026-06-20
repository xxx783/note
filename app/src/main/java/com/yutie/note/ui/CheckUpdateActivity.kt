package com.yutie.note.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.yutie.note.R
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.utils.SupabaseClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import kotlin.coroutines.resume

/**
 * 版本数据类
 */
data class VersionInfo(
    val id: Long = 0,
    val version: String = "",
    val updateLog: String = "",
    val updateTime: String = "",
    val downloadUrl: String = ""
) {
    val formattedTime: String
        get() = updateTime
}

/**
 * 检查更新页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckUpdateScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 当前版本
    val currentVersion = remember {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    // 状态
    var isLoading by remember { mutableStateOf(true) }
    var isChecking by remember { mutableStateOf(false) }
    var versions by remember { mutableStateOf<List<VersionInfo>>(emptyList()) }
    var latestVersion by remember { mutableStateOf<VersionInfo?>(null) }
    var hasUpdate by remember { mutableStateOf(false) }
    
    // 加载版本信息
    LaunchedEffect(Unit) {
        loadVersions { versionList ->
            versions = versionList
            isLoading = false
            
            // 找到最新版本
            if (versionList.isNotEmpty()) {
                val latest = versionList[0] // 已经是按 ID 倒序排列
                latestVersion = latest
                
                // 比较版本号
                hasUpdate = compareVersions(latest.version, currentVersion) > 0
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        CustomTitleBar(
            title = context.getString(R.string.str_check_update_title),
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
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 当前版本信息
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = context.getString(R.string.str_current_version_card),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currentVersion,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 检查更新按钮
                item {
                    Button(
                        onClick = {
                            isChecking = true
                            scope.launch {
                                loadVersions { versionList ->
                                    versions = versionList
                                    if (versionList.isNotEmpty()) {
                                        val latest = versionList[0]
                                        latestVersion = latest
                                        hasUpdate = compareVersions(latest.version, currentVersion) > 0
                                    }
                                    isChecking = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isChecking
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(context.getString(R.string.str_checking))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(context.getString(R.string.str_check_update_btn))
                        }
                    }
                }
                
                // 最新版本提示
                if (latestVersion != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (hasUpdate) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
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
                                        imageVector = if (hasUpdate) Icons.Default.CheckCircle else Icons.Default.Done,
                                        contentDescription = null,
                                        tint = if (hasUpdate) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (hasUpdate) context.getString(R.string.str_new_version_found) else context.getString(R.string.str_latest_version),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasUpdate) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        if (hasUpdate) {
                                            Text(
                                                text = context.getString(R.string.str_latest_version_hint) + latestVersion!!.version,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 更新日志列表
                if (versions.isNotEmpty()) {
                    item {
                        Text(
                            text = context.getString(R.string.str_update_logs),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(versions) { version ->
                        VersionCard(
                            version = version,
                            isLatest = version.id == latestVersion?.id,
                            onDownloadClick = {
                                if (version.downloadUrl.isNotEmpty() && version.downloadUrl.startsWith("http")) {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(version.downloadUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.str_unable_to_open_link) + e.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.str_no_download_link),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                } else {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Update,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = context.getString(R.string.str_no_update_records),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 版本卡片
 */
@Composable
fun VersionCard(
    version: VersionInfo,
    isLatest: Boolean,
    @Suppress("UNUSED_PARAMETER") onDownloadClick: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(isLatest) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 版本号和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLatest) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "最新",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = context.getString(R.string.str_version_label) + " ${version.version}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
            
            // 更新时间
            Text(
                text = context.getString(R.string.str_update_time) + version.formattedTime + " (UTC+8)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // 展开的更新内容
            if (isExpanded) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        // 更新日志
                        if (version.updateLog.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = context.getString(R.string.str_update_content),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = version.updateLog,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        
                        // 下载按钮
                        if (version.downloadUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val uri = Uri.parse(version.downloadUrl)
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "无法打开下载链接：${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(context.getString(R.string.str_download_update))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 比较版本号
 * @return >0 表示 v1 > v2, <0 表示 v1 < v2, =0 表示相等
 */
fun compareVersions(v1: String, v2: String): Int {
    val parts1 = v1.split(".").map { it.toIntOrNull() ?: 0 }
    val parts2 = v2.split(".").map { it.toIntOrNull() ?: 0 }
    
    val maxLength = maxOf(parts1.size, parts2.size)
    
    for (i in 0 until maxLength) {
        val num1 = parts1.getOrElse(i) { 0 }
        val num2 = parts2.getOrElse(i) { 0 }
        
        if (num1 != num2) {
            return num1 - num2
        }
    }
    
    return 0
}

/**
 * 加载版本列表（支持内测版本检查）
 */
private suspend fun loadVersions(
    onLoadComplete: (List<VersionInfo>) -> Unit
) {
    val currentUser = SupabaseClient.currentUser
    val userEmail = currentUser?.email ?: ""
    
    println("=== 加载版本信息 ===")
    println("当前用户邮箱：$userEmail")
    
    // 第一步：检查用户是否在内测名单中
    var isBetaUser = false
    if (userEmail.isNotEmpty()) {
        isBetaUser = checkIsBetaUser(userEmail)
        println("是否为内测用户：$isBetaUser")
    }
    
    // 第二步：根据内测状态查询版本表
    val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/version"
    
    // 构建查询参数：如果不是内测用户，只查询 is_beta = false 或 null 的版本
    val queryParams = if (!isBetaUser) {
        // 非内测用户：查询 is_beta = false 或 is_beta = null 的版本
        "?or=(is_beta.eq.false,is_beta.is.null)"
    } else {
        // 内测用户：查询所有版本（包括 is_beta = true 的版本）
        ""
    }
    
    val fullUrl = url + queryParams
    
    println("查询 URL: $fullUrl")
    println("内测用户：$isBetaUser")
    
    val request = okhttp3.Request.Builder()
        .url(fullUrl)
        .get()
        .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
        .addHeader("Content-Type", "application/json")
        .addHeader("Prefer", "order=id.desc") // 按 ID 倒序，最新的在前
        .build()
    
    val response = suspendCancellableCoroutine<okhttp3.Response?> { continuation ->
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                println("=== 加载版本 - 网络错误 ===")
                e.printStackTrace()
                if (continuation.isActive) continuation.resume(null)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (continuation.isActive) continuation.resume(response)
            }
        })
    }
    
    response?.use {
        if (it.isSuccessful) {
            val bodyString = it.body?.string()
            println("Response Body: $bodyString")
            
            if (!bodyString.isNullOrBlank()) {
                try {
                    val jsonArray = JSONArray(bodyString)
                    println("版本数量：${jsonArray.length()}")
                    val list = mutableListOf<VersionInfo>()
                    
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val versionInfo = VersionInfo(
                            id = json.optLong("id", 0),
                            version = json.optString("version", "").trim(),
                            updateLog = json.optString("jl", "").trim(),
                            updateTime = json.optString("update_time", "").trim(),
                            downloadUrl = json.optString("download_url", "").trim()
                        )
                        list.add(versionInfo)
                        println("版本 $i: ${versionInfo.version}")
                    }
                    
                    onLoadComplete(list)
                    return
                } catch (e: Exception) {
                    println("=== 加载版本 - JSON 解析错误 ===")
                    e.printStackTrace()
                }
            }
        } else {
            println("=== 加载版本 - 请求失败 ===")
            println("Response Code: ${it.code}")
        }
        
        // 如果网络请求失败，返回一个包含备用下载链接的列表
        println("=== 使用备用下载链接 ===")
        val fallbackList = listOf(
            VersionInfo(
                id = 999L,
                version = "3.0",
                updateLog = "• 添加了 AI 功能\n• 修复了一些已知问题",
                updateTime = "17:08:17",
                downloadUrl = "https://up.mcplay123.dpdns.org/简约笔记 2.0.apk"
            )
        )
        onLoadComplete(fallbackList)
    }
}

/**
 * 检查用户是否在内测名单中
 */
private suspend fun checkIsBetaUser(email: String): Boolean {
    val currentUser = SupabaseClient.currentUser
    var userToken = currentUser?.token ?: ""
    
    // 如果 Token 为空或需要刷新，先刷新 Token
    if (userToken.isEmpty()) {
        println("=== Token 为空，跳过检查 ===")
        return false
    }
    
    // 先刷新 Token，确保有效
    println("=== 刷新 Token ===")
    val tokenRefreshed = suspendCancellableCoroutine<Boolean> { continuation ->
        SupabaseClient.refreshToken { success, error ->
            if (continuation.isActive) continuation.resume(success)
        }
    }
    
    if (!tokenRefreshed) {
        println("Token 刷新失败，尝试使用旧 Token")
    }
    
    // 获取最新的 Token
    val updatedUser = SupabaseClient.currentUser
    userToken = updatedUser?.token ?: ""
    
    return suspendCancellableCoroutine { continuation ->
        val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/beta_user"
        // 使用正确的查询格式：filter 在前，select 在后
        val fullUrl = "$url?email=eq.$email&select=id"
        
        println("=== 检查内测用户 ===")
        println("查询 URL: $fullUrl")
        println("用户 Token: ${if (userToken.isNotEmpty()) "有" else "无"}")
        
        val request = okhttp3.Request.Builder()
            .url(fullUrl)
            .get()
            .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer $userToken") // 添加用户 Token
            .addHeader("Content-Type", "application/json")
            .build()
        
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                println("=== 检查内测用户 - 网络错误 ===")
                e.printStackTrace()
                if (continuation.isActive) continuation.resume(false)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                val responseCode = response.code
                val countHeader = response.header("Content-Range")
                
                println("=== 内测用户检查结果 ===")
                println("响应码：$responseCode")
                println("响应内容：$responseBody")
                println("Content-Range: $countHeader")
                
                // 检查响应
                val isBeta = if (response.isSuccessful) {
                    if (!responseBody.isNullOrBlank()) {
                        try {
                            val jsonArray = JSONArray(responseBody)
                            println("返回记录数：${jsonArray.length()}")
                            if (jsonArray.length() > 0) {
                                println("✓ 找到内测用户记录")
                            } else {
                                println("✗ 未找到内测用户记录")
                            }
                            jsonArray.length() > 0 // 如果有记录，说明是内测用户
                        } catch (e: Exception) {
                            println("解析响应失败：${e.message}")
                            false
                        }
                    } else {
                        println("✗ 响应为空")
                        false
                    }
                } else {
                    println("请求失败：$responseCode")
                    false
                }
                
                if (continuation.isActive) continuation.resume(isBeta)
            }
        })
    }
}
