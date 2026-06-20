package com.yutie.note.utils

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

/**
 * Supabase 管理类
 * 使用官方 SDK，支持自动刷新 token
 */
object SupabaseManager {

    private const val SUPABASE_URL = "https://hgmpejfrhfiitpmyoieq.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_tYv8QXhk3RXZ2SW_5itwIw_sZmbiC3m"

    private var client: SupabaseClient? = null

    /**
     * 初始化 Supabase 客户端
     * 配置自动刷新 token、持久化会话
     */
    fun init(context: Context) {
        client = createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth) {
                // 关键配置：自动刷新 token（Supabase SDK 2.x 默认开启）
            }
            install(Postgrest)
        }
    }

    /**
     * 获取 Supabase 客户端
     */
    fun getClient(): SupabaseClient? = client

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return client?.auth?.currentUserOrNull() != null
    }

    /**
     * 获取当前用户
     */
    fun getCurrentUser() = client?.auth?.currentUserOrNull()

    /**
     * 登录
     */
    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            client?.auth?.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 注册
     */
    suspend fun register(email: String, password: String): Result<Unit> {
        return try {
            client?.auth?.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 退出登录
     */
    suspend fun logout() {
        try {
            client?.auth?.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 同步所有笔记到云端
     */
    suspend fun syncAllNotes(notes: List<com.yutie.note.bean.NoteBean>): Result<Pair<Int, Int>> {
        return withContext(Dispatchers.IO) {
            try {
                val user = getCurrentUser() ?: return@withContext Result.failure(Exception("未登录"))
                if (notes.isEmpty()) return@withContext Result.success(Pair(0, 0))

                var successCount = 0
                var failCount = 0

                notes.forEach { note ->
                    try {
                        val cloudNote = CloudNote(
                            user_id = user.id,
                            title = note.title,
                            content = note.content,
                            create_time = formatTimestamp(note.createTime),
                            update_time = formatTimestamp(note.modifyTime),
                            is_deleted = note.isDeleted
                        )
                        client?.from("notes")?.insert(cloudNote)
                        successCount++
                    } catch (e: Exception) {
                        failCount++
                    }
                }

                Result.success(Pair(successCount, failCount))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 从云端拉取笔记
     */
    suspend fun downloadNotes(): Result<List<CloudNote>> {
        return withContext(Dispatchers.IO) {
            try {
                val user = getCurrentUser() ?: return@withContext Result.failure(Exception("未登录"))

                val notes = client?.from("notes")
                    ?.select {
                        filter {
                            eq("user_id", user.id)
                            eq("is_deleted", false)
                        }
                    }
                    ?.decodeList<CloudNote>() ?: emptyList()

                Result.success(notes)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 格式化时间戳为 ISO 8601
     */
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(java.util.Date(timestamp))
    }
}

/**
 * 云端笔记数据类
 */
@Serializable
data class CloudNote(
    val id: Long? = null,
    val user_id: String,
    val title: String,
    val content: String,
    val create_time: String,
    val update_time: String,
    val is_deleted: Boolean = false
)
