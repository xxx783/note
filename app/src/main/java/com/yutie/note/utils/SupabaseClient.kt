package com.yutie.note.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * Supabase API 工具类
 * 通过 REST API 连接 Supabase
 */
object SupabaseClient {
    
    // Supabase 项目 URL
    // 格式：https://xxxxx.supabase.co
    const val SUPABASE_URL = "https://hgmpejfrhfiitpmyoieq.supabase.co"
    
    // Supabase API Key (可公开访问的 anon key)
    const val SUPABASE_ANON_KEY = "sb_publishable_tYv8QXhk3RXZ2SW_5itwIw_sZmbiC3m"
    
    val client = OkHttpClient()
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 用户数据类
     */
    data class User(
        val id: String,
        val email: String,
        val token: String,
        val refreshToken: String = ""
    )
    
    /**
     * 登录/注册响应
     */
    data class AuthResponse(
        val user: User?,
        val error: String?
    )
    
    // SharedPreferences 存储用户信息
    private lateinit var sp: SharedPreferences
    private const val SP_NAME = "user_config"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_TOKEN = "user_token"
    private const val KEY_USER_REFRESH_TOKEN = "user_refresh_token"
    
    /**
     * 初始化（需要在 Application 中调用）
     */
    fun init(context: Context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 当前登录用户
     */
    var currentUser: User? = null
        get() {
            // 如果内存中没有，尝试从 SP 读取
            if (field == null) {
                val id = sp.getString(KEY_USER_ID, null) ?: return null
                val email = sp.getString(KEY_USER_EMAIL, "") ?: ""
                val token = sp.getString(KEY_USER_TOKEN, "") ?: ""
                val refreshToken = sp.getString(KEY_USER_REFRESH_TOKEN, "") ?: ""
                field = User(id, email, token, refreshToken)
            }
            return field
        }
        private set
    
    /**
     * 使用邮箱密码登录
     */
    fun login(email: String, password: String, callback: (AuthResponse) -> Unit) {
        val url = "$SUPABASE_URL/auth/v1/token?grant_type=password"
        
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)
        
        val body = json.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
            .addHeader("Content-Type", "application/json")
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    callback(AuthResponse(null, "网络错误：${e.message}"))
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                mainHandler.post {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val userJson = jsonResponse.getJSONObject("user")
                            val accessToken = jsonResponse.getString("access_token")
                            
                            val refreshToken = jsonResponse.optString("refresh_token", "")
                            
                            val user = User(
                                id = userJson.getString("id"),
                                email = userJson.getString("email"),
                                token = accessToken,
                                refreshToken = refreshToken
                            )
                            
                            // 保存用户信息
                            saveUser(user)
                            currentUser = user
                            
                            callback(AuthResponse(user, null))
                        } catch (e: Exception) {
                            callback(AuthResponse(null, "解析响应失败：${e.message}"))
                        }
                    } else {
                        val errorMsg = try {
                            JSONObject(responseBody ?: "").getString("msg") ?: "登录失败"
                        } catch (e: Exception) {
                            "登录失败：${response.code}"
                        }
                        callback(AuthResponse(null, errorMsg))
                    }
                }
            }
        })
    }
    
    /**
     * 注册新用户
     */
    fun register(email: String, password: String, callback: (AuthResponse) -> Unit) {
        val url = "$SUPABASE_URL/auth/v1/signup"
        
        val json = JSONObject()
        json.put("email", email)
        json.put("password", password)
        
        val body = json.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
            .addHeader("Content-Type", "application/json")
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    callback(AuthResponse(null, "网络错误：${e.message}"))
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                mainHandler.post {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val userJson = jsonResponse.getJSONObject("user")
                            
                            val user = User(
                                id = userJson.getString("id"),
                                email = userJson.getString("email"),
                                token = ""
                            )
                            
                            callback(AuthResponse(user, null))
                        } catch (e: Exception) {
                            callback(AuthResponse(null, "解析响应失败"))
                        }
                    } else {
                        val errorMsg = try {
                            JSONObject(responseBody ?: "").getString("msg") ?: "注册失败"
                        } catch (e: Exception) {
                            "注册失败：${response.code}"
                        }
                        callback(AuthResponse(null, errorMsg))
                    }
                }
            }
        })
    }
    
    /**
     * 登出
     */
    fun logout() {
        currentUser = null
        // 清除 SP 中的数据
        sp.edit().clear().apply()
    }
    
    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return currentUser != null
    }
    
    /**
     * 保存用户信息到 SharedPreferences
     */
    private fun saveUser(user: User) {
        sp.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_TOKEN, user.token)
            putString(KEY_USER_REFRESH_TOKEN, user.refreshToken)
            apply()
        }
    }
    
    /**
     * 使用 Refresh Token 刷新 Access Token
     */
    fun refreshToken(callback: (Boolean, String?) -> Unit) {
        val user = currentUser
        if (user == null || user.refreshToken.isEmpty()) {
            mainHandler.post { callback(false, "没有 Refresh Token") }
            return
        }
        
        val url = "$SUPABASE_URL/auth/v1/token?grant_type=refresh_token"
        
        val json = JSONObject()
        json.put("refresh_token", user.refreshToken)
        
        val body = json.toString().toRequestBody(mediaType)
        
        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("apikey", SUPABASE_ANON_KEY)
            .addHeader("Authorization", "Bearer $SUPABASE_ANON_KEY")
            .addHeader("Content-Type", "application/json")
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mainHandler.post {
                    callback(false, "网络错误：${e.message}")
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                
                mainHandler.post {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val accessToken = jsonResponse.getString("access_token")
                            val newRefreshToken = jsonResponse.optString("refresh_token", user.refreshToken)
                            
                            // 更新用户信息
                            val updatedUser = user.copy(
                                token = accessToken,
                                refreshToken = newRefreshToken
                            )
                            saveUser(updatedUser)
                            currentUser = updatedUser
                            
                            callback(true, null)
                        } catch (e: Exception) {
                            callback(false, "解析响应失败：${e.message}")
                        }
                    } else {
                        callback(false, "刷新失败：${response.code}")
                    }
                }
            }
        })
    }
}
