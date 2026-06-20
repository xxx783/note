package com.yutie.note.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.yutie.note.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * 全局权限管理类
 * 用于检查用户是否为 Pro 或 APEX 用户，并控制功能访问
 */
object FeatureGuard {
    
    // 缓存时间：5 分钟
    private const val CACHE_DURATION = 5 * 60 * 1000L
    
    // 缓存的用户计划
    private var cachedPlan: String? = null
    private var cacheTime: Long = 0L
    
    // 升级 URL
    private const val UPGRADE_URL = "https://your-website.com/upgrade"
    
    /**
     * 获取当前用户计划（带缓存）
     * @param forceRefresh 是否强制刷新，忽略缓存
     */
    suspend fun getCurrentPlan(forceRefresh: Boolean = false): String {
        val currentTime = System.currentTimeMillis()
        
        // 检查缓存是否有效
        if (!forceRefresh && cachedPlan != null && (currentTime - cacheTime) < CACHE_DURATION) {
            return cachedPlan!!
        }
        
        // 从网络获取
        return withContext(Dispatchers.IO) {
            try {
                val user = SupabaseClient.currentUser
                if (user == null) {
                    "free"
                } else {
                    // 查询 user_profiles 表
                    val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/user_profiles?id=eq.${user.id}"
                    val request = okhttp3.Request.Builder()
                        .url(url)
                        .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
                        .addHeader("Authorization", "Bearer ${user.token}")
                        .build()
                    
                    val response = SupabaseClient.client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (body != null && org.json.JSONArray(body).length() > 0) {
                            val jsonObj = org.json.JSONArray(body).getJSONObject(0)
                            val plan = jsonObj.optString("plan", "free")
                            cachedPlan = plan
                            cacheTime = currentTime
                            plan
                        } else {
                            "free"
                        }
                    } else {
                        "free"
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                "free"
            }
        }
    }
    
    /**
     * 检查是否为 Pro 或 APEX 用户
     */
    suspend fun isProOrApex(): Boolean {
        val plan = getCurrentPlan()
        return plan == "pro" || plan == "apex"
    }
    
    /**
     * 检查是否为 APEX 用户
     */
    suspend fun isApex(): Boolean {
        val plan = getCurrentPlan()
        return plan == "apex"
    }
    
    /**
     * 显示升级对话框
     * @param context 上下文
     * @param featureName 需要升级才能使用的功能名称
     */
    fun showUpgradeDialog(context: Context, featureName: String) {
        AlertDialog.Builder(context)
            .setTitle("需要升级到 Pro")
            .setMessage("功能\"$featureName\"需要 Pro 或 APEX 账户才能使用。\n\n升级到 Pro 可享受：\n• 精美官方主题\n• 自定义主题颜色\n• 更多功能等你来发现！\n\n升级到 APEX 可享受：\n• 自定义动画效果\n• 社区主题分享\n• 云端主题同步")
            .setPositiveButton("了解详情") { _, _ ->
                // 打开升级页面
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(UPGRADE_URL))
                context.startActivity(intent)
            }
            .setNegativeButton("稍后", null)
            .show()
    }
    
    /**
     * 清除缓存（用于重新登录后）
     */
    fun clearCache() {
        cachedPlan = null
        cacheTime = 0L
    }
}
