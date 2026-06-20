package com.yutie.note.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Supabase API 工具类
 * 通过 REST API 连接 Supabase，支持 SSL Pinning
 */
object SupabaseClient {
    
    const val SUPABASE_URL = "https://hgmpejfrhfiitpmyoieq.supabase.co"
    const val SUPABASE_ANON_KEY = "sb_publishable_tYv8QXhk3RXZ2SW_5itwIw_sZmbiC3m"
    
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    private lateinit var sp: SharedPreferences
    private const val SP_NAME = "user_config"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_TOKEN = "user_token"
    private const val KEY_USER_REFRESH_TOKEN = "user_refresh_token"
    
    val client: OkHttpClient by lazy {
        createSecureOkHttpClient()
    }
    
    data class User(
        val id: String,
        val email: String,
        val token: String,
        val refreshToken: String = ""
    )
    
    data class AuthResponse(
        val user: User?,
        val error: String?
    )
    
    var currentUser: User? = null
        get() {
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
    
    fun init(context: Context) {
        sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 创建带有 SSL Pinning 的安全 OkHttpClient
     */
    private fun createSecureOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(sslPinningInterceptor())
            .build()
    }
    
    /**
     * SSL Pinning 拦截器
     * 验证服务器证书指纹
     */
    private fun sslPinningInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            
            val certs = response.handshake?.peerCertificates
            if (certs != null && !verifyCertificatePins(certs)) {
                throw SecurityException("SSL certificate pinning failed")
            }
            
            response
        }
    }
    
    /**
     * 验证证书指纹
     */
    private fun verifyCertificatePins(certificates: List<Certificate>): Boolean {
        val allowedFingerprints = listOf(
            "SHA256:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        )
        
        for (cert in certificates) {
            if (cert is X509Certificate) {
                val fingerprint = getCertificateFingerprint(cert)
                if (allowedFingerprints.contains(fingerprint)) {
                    return true
                }
            }
        }
        return false
    }
    
    /**
     * 获取证书 SHA256 指纹
     */
    private fun getCertificateFingerprint(cert: X509Certificate): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val encoded = digest.digest(cert.encoded)
        return "SHA256:" + android.util.Base64.encodeToString(encoded, android.util.Base64.DEFAULT).trim()
    }
    
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
    
    fun logout() {
        currentUser = null
        sp.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return currentUser != null
    }
    
    private fun saveUser(user: User) {
        sp.edit().apply {
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_TOKEN, user.token)
            putString(KEY_USER_REFRESH_TOKEN, user.refreshToken)
            apply()
        }
    }
    
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
