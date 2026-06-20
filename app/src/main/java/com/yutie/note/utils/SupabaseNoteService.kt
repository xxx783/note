package com.yutie.note.utils

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Supabase 笔记同步服务
 * 实现笔记数据与云端 notes 表的同步
 */
object SupabaseNoteService {
    
    private const val SUPABASE_URL = "https://hgmpejfrhfiitpmyoieq.supabase.co"
    private const val SUPABASE_ANON_KEY = "sb_publishable_tYv8QXhk3RXZ2SW_5itwIw_sZmbiC3m"
    
    private val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    
    /**
     * 云端笔记数据类
     */
    data class CloudNote(
        val id: Long,
        val user_id: String,
        val title: String,
        val content: String,
        val create_time: String,
        val update_time: String,
        val is_deleted: Boolean
    )
    
    /**
     * 同步响应
     */
    data class SyncResponse(
        val notes: List<CloudNote>?,
        val error: String?,
        val success: Boolean
    )
    
    /**
     * 上传笔记到云端（新增）
     * 返回：success, cloudId 或 errorMessage
     */
    fun uploadNote(note: com.yutie.note.bean.NoteBean, callback: (Boolean, String?) -> Unit, isRetry: Boolean = false) {
        val user = SupabaseClient.currentUser
        if (user == null) {
            mainHandler.post { callback(false, "未登录") }
            return
        }

        val url = "$SUPABASE_URL/rest/v1/notes"

        val json = JSONObject()
        json.put("user_id", user.id)
        json.put("title", note.title)
        json.put("content", note.content)
        json.put("create_time", formatTimestamp(note.createTime))
        json.put("update_time", formatTimestamp(note.modifyTime))
        json.put("is_deleted", note.isDeleted)

        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "return=representation")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post { callback(false, "网络错误：${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val responseCode = response.code
                mainHandler.post {
                    if (response.isSuccessful) {
                        try {
                            // 解析返回的云端 ID
                            if (responseBody != null && responseBody.isNotEmpty()) {
                                val jsonArray = org.json.JSONArray(responseBody)
                                if (jsonArray.length() > 0) {
                                    val jsonObj = jsonArray.getJSONObject(0)
                                    val cloudId = jsonObj.getLong("id")
                                    callback(true, cloudId.toString())
                                } else {
                                    callback(false, "返回数据为空")
                                }
                            } else {
                                callback(false, "返回数据为空")
                            }
                        } catch (e: Exception) {
                            callback(false, "解析错误：${e.message}")
                        }
                    } else if (responseCode == 401 && !isRetry) {
                        // Token 过期，尝试刷新（只重试一次）
                        val errorBody = responseBody ?: ""
                        if (errorBody.contains("JWT expired")) {
                            SupabaseClient.refreshToken { success, error ->
                                if (success) {
                                    // 刷新成功，重新上传（标记为已重试）
                                    uploadNote(note, callback, isRetry = true)
                                } else {
                                    callback(false, "Token 过期且刷新失败：$error")
                                }
                            }
                        } else {
                            callback(false, "HTTP $responseCode: $errorBody")
                        }
                    } else {
                        callback(false, "HTTP $responseCode: $responseBody")
                    }
                }
            }
        })
    }

    /**
     * Upsert 笔记（更新或插入）
     * 使用 cloudId 进行更新，如果不存在则插入
     * 返回：success, cloudId 或 errorMessage
     */
    fun upsertNote(note: com.yutie.note.bean.NoteBean, callback: (Boolean, String?) -> Unit, isRetry: Boolean = false) {
        val user = SupabaseClient.currentUser
        if (user == null) {
            mainHandler.post { callback(false, "未登录") }
            return
        }

        val json = JSONObject()
        json.put("user_id", user.id)
        json.put("title", note.title)
        json.put("content", note.content)
        json.put("create_time", formatTimestamp(note.createTime))
        json.put("update_time", formatTimestamp(note.modifyTime))
        json.put("is_deleted", note.isDeleted)

        val body = json.toString().toRequestBody(mediaType)

        // 使用云端 ID进行 upsert
        val cloudId = note.cloudId
        val request = if (cloudId != null && cloudId > 0) {
            // 有云端 ID，使用 PATCH 更新（只更新提供的字段）
            // Supabase PATCH 格式：/rest/v1/notes?id=eq.{cloudId}
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/notes?id=eq.${cloudId}")
                .patch(body)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${user.token}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build()
        } else {
            // 没有云端 ID，使用 POST 新增
            Request.Builder()
                .url("$SUPABASE_URL/rest/v1/notes")
                .post(body)
                .addHeader("apikey", SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer ${user.token}")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build()
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post { callback(false, "网络错误：${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val responseCode = response.code
                mainHandler.post {
                    if (response.isSuccessful) {
                        // PATCH 成功返回更新后的数据（数组格式）
                        try {
                            if (responseBody != null && responseBody.isNotEmpty()) {
                                val jsonArray = org.json.JSONArray(responseBody)
                                if (jsonArray.length() > 0) {
                                    val jsonObj = jsonArray.getJSONObject(0)
                                    val returnedCloudId = jsonObj.getLong("id")
                                    callback(true, returnedCloudId.toString())
                                } else {
                                    callback(true, cloudId.toString())
                                }
                            } else {
                                callback(true, cloudId.toString())
                            }
                        } catch (e: Exception) {
                            callback(true, cloudId.toString())
                        }
                    } else if (responseCode == 401 && !isRetry) {
                        // Token 过期，尝试刷新（只重试一次）
                        val errorBody = responseBody ?: ""
                        if (errorBody.contains("JWT expired")) {
                            SupabaseClient.refreshToken { success, error ->
                                if (success) {
                                    // 刷新成功，重新上传（标记为已重试）
                                    upsertNote(note, callback, isRetry = true)
                                } else {
                                    callback(false, "Token 过期且刷新失败：$error")
                                }
                            }
                        } else {
                            callback(false, "HTTP $responseCode: $errorBody")
                        }
                    } else {
                        callback(false, "HTTP $responseCode: $responseBody")
                    }
                }
            }
        })
    }
    
    /**
     * 从云端彻底删除笔记
     */
    fun deleteNoteFromCloud(cloudId: Long, callback: ((Boolean, String?) -> Unit)? = null) {
        val user = SupabaseClient.currentUser
        if (user == null) {
            mainHandler.post { callback?.invoke(false, "未登录") }
            return
        }

        val url = "$SUPABASE_URL/rest/v1/notes?id=eq.$cloudId"

        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post { 
                    callback?.invoke(false, "网络错误：${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                mainHandler.post {
                    if (response.isSuccessful) {
                        callback?.invoke(true, null)
                    } else {
                        val responseBody = response.body?.string()
                        callback?.invoke(false, "HTTP ${response.code}: $responseBody")
                    }
                }
            }
        })
    }
    
    /**
     * 从云端下载笔记
     */
    fun downloadNotes(callback: (SyncResponse) -> Unit, isRetry: Boolean = false) {
        val user = SupabaseClient.currentUser
        if (user == null) {
            mainHandler.post { callback(SyncResponse(null, "未登录", false)) }
            return
        }

        val url = "$SUPABASE_URL/rest/v1/notes?user_id=eq.${user.id}&is_deleted=eq.false&order=create_time.desc"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post { callback(SyncResponse(null, "网络错误：${e.message}", false)) }
            }

            override fun onResponse(call: Call, response: Response) {
                mainHandler.post {
                    if (response.isSuccessful) {
                        try {
                            val responseBody = response.body?.string() ?: "[]"
                            val notes = parseNotes(responseBody)
                            callback(SyncResponse(notes, null, true))
                        } catch (e: Exception) {
                            callback(SyncResponse(null, "解析失败：${e.message}", false))
                        }
                    } else if (response.code == 401 && !isRetry) {
                        // Token 过期，尝试刷新（只重试一次）
                        val errorBody = response.body?.string() ?: ""
                        if (errorBody.contains("JWT expired")) {
                            SupabaseClient.refreshToken { success, error ->
                                if (success) {
                                    // 刷新成功，重新下载（标记为已重试）
                                    downloadNotes(callback, isRetry = true)
                                } else {
                                    callback(SyncResponse(null, "Token 过期且刷新失败：$error", false))
                                }
                            }
                        } else {
                            callback(SyncResponse(null, "下载失败：${response.code} - $errorBody", false))
                        }
                    } else {
                        val errorBody = response.body?.string() ?: "无错误详情"
                        callback(SyncResponse(null, "下载失败：${response.code} - $errorBody", false))
                    }
                }
            }
        })
    }
    
    /**
     * 删除云端笔记（软删除）
     */
    fun deleteNote(noteId: Long, callback: (Boolean, String?) -> Unit) {
        val user = SupabaseClient.currentUser
        if (user == null) {
            mainHandler.post { callback(false, "未登录") }
            return
        }
        
        val url = "$SUPABASE_URL/rest/v1/notes?id=eq.$noteId"
        
        val json = JSONObject()
        json.put("is_deleted", true)
        json.put("update_time", formatTimestamp(System.currentTimeMillis()))
        
        val body = json.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .patch(body)
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer ${user.token}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post { callback(false, "网络错误：${e.message}") }
            }
            
            override fun onResponse(call: Call, response: Response) {
                mainHandler.post {
                    if (response.isSuccessful) {
                        callback(true, null)
                    } else {
                        callback(false, "删除失败：${response.code}")
                    }
                }
            }
        })
    }
    
    /**
     * 清除所有云端笔记（软删除）
     * 将当前用户的所有云端笔记标记为 is_deleted = true
     */
    fun clearAllCloudNotes(callback: (Boolean, String?) -> Unit) {
        val user = SupabaseClient.currentUser
        if (user == null) {
            mainHandler.post { callback(false, "未登录") }
            return
        }
        
        // 先查询所有云端笔记
        downloadNotes(callback = { response ->
            if (!response.success) {
                mainHandler.post { callback(false, "查询云端笔记失败") }
                return@downloadNotes
            }
            
            val cloudNotes = response.notes ?: emptyList()
            if (cloudNotes.isEmpty()) {
                mainHandler.post { callback(true, "云端没有笔记") }
                return@downloadNotes
            }
            
            var successCount = 0
            var failCount = 0
            var completedCount = 0
            
            cloudNotes.forEach { cloudNote ->
                // 更新每条笔记为已删除
                val url = "$SUPABASE_URL/rest/v1/notes?id=eq.${cloudNote.id}"
                
                val json = JSONObject()
                json.put("is_deleted", true)
                json.put("update_time", formatTimestamp(System.currentTimeMillis()))
                
                val body = json.toString().toRequestBody(mediaType)
                
                val request = Request.Builder()
                    .url(url)
                    .patch(body)
                    .addHeader("apikey", SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer ${user.token}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        synchronized(this) {
                            failCount++
                            completedCount++
                            checkComplete()
                        }
                    }
                    
                    override fun onResponse(call: Call, response: Response) {
                        synchronized(this) {
                            if (response.isSuccessful) {
                                successCount++
                            } else {
                                failCount++
                            }
                            completedCount++
                            checkComplete()
                        }
                    }
                    
                    private fun checkComplete() {
                        if (completedCount >= cloudNotes.size) {
                            mainHandler.post {
                                if (failCount == 0) {
                                    callback(true, "成功清除 $successCount 条云端笔记")
                                } else {
                                    callback(false, "清除完成：成功 $successCount，失败 $failCount")
                                }
                            }
                        }
                    }
                })
            }
        })
    }
    
    /**
     * 解析云端笔记列表
     */
    private fun parseNotes(jsonString: String): List<CloudNote> {
        val notes = mutableListOf<CloudNote>()
        val jsonArray = org.json.JSONArray(jsonString)
        
        for (i in 0 until jsonArray.length()) {
            try {
                val json = jsonArray.getJSONObject(i)
                notes.add(
                    CloudNote(
                        id = json.getLong("id"),
                        user_id = json.getString("user_id"),
                        title = json.getString("title"),
                        content = json.getString("content"),
                        create_time = json.getString("create_time"),
                        update_time = json.getString("update_time"),
                        is_deleted = json.getBoolean("is_deleted")
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return notes
    }
    
    /**
     * 格式化时间戳
     */
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(timestamp))
    }
    
    /**
     * 同步所有笔记到云端
     * 策略：有 cloudId 的用 upsert 更新，没有的用 upload 新增
     */
    fun syncAllNotes(notes: List<com.yutie.note.bean.NoteBean>, onComplete: (SyncResult, Map<Long, Long>) -> Unit) {
        val user = SupabaseClient.currentUser
        if (user == null) {
            mainHandler.post { onComplete(SyncResult(false, "未登录", 0, 0), emptyMap()) }
            return
        }

        if (notes.isEmpty()) {
            mainHandler.post { onComplete(SyncResult(true, "没有需要同步的笔记", 0, 0), emptyMap()) }
            return
        }

        var successCount = 0
        var failCount = 0
        var completedCount = 0
        val errorMessages = mutableListOf<String>()
        // 记录本地ID到云端ID的映射
        val idMapping = mutableMapOf<Long, Long>()

        fun checkComplete() {
            if (completedCount >= notes.size) {
                val message = if (failCount > 0) {
                    "同步完成：成功 $successCount，失败 $failCount\n错误: ${errorMessages.firstOrNull() ?: ""}"
                } else {
                    "同步完成：成功 $successCount"
                }
                mainHandler.post {
                    onComplete(SyncResult(successCount > 0, message, successCount, failCount), idMapping)
                }
            }
        }

        notes.forEach { note ->
            if (note.cloudId != null && note.cloudId > 0) {
                // 有云端 ID，使用 upsert 更新
                upsertNote(note, callback = { success, result ->
                    synchronized(this) {
                        if (success && result != null) {
                            successCount++
                            idMapping[note.id] = result.toLongOrNull() ?: note.cloudId
                        } else {
                            failCount++
                            errorMessages.add("笔记 [${note.title}] 更新失败：$result")
                        }
                        completedCount++
                        checkComplete()
                    }
                }, isRetry = false)
            } else {
                // 没有云端 ID，使用 upload 新增
                uploadNote(note, callback = { success, result ->
                    synchronized(this) {
                        if (success && result != null) {
                            successCount++
                            // 记录本地 ID 到云端 ID 的映射
                            val cloudId = result.toLongOrNull()
                            if (cloudId != null) {
                                idMapping[note.id] = cloudId
                            }
                        } else {
                            failCount++
                            errorMessages.add("笔记 [${note.title}] 上传失败：$result")
                        }
                        completedCount++
                        checkComplete()
                    }
                }, isRetry = false)
            }
        }
    }

    /**
     * 同步结果数据类
     */
    data class SyncResult(
        val success: Boolean,
        val message: String,
        val successCount: Int,
        val failCount: Int
    )
}
