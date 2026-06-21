package com.yutie.note.utils

import com.yutie.note.model.AIModel
import com.yutie.note.model.AIModelConfig
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * AI 服务类
 */
class AIService {
    
    companion object {
        private var apiBaseUrl: String = "https://api.siliconflow.cn/v1"
        
        // AI 提示词配置
        data class AIPrompts(
            val polishPrompt: String,
            val summarizePrompt: String,
            val translatePrompt: String,
            val grammarPrompt: String,
            val formatPrompt: String
        )
        
        private var currentPrompts: AIPrompts? = null
        
        // 从云端获取 AI 配置
        suspend fun getAIConfigFromCloud(): Triple<String?, String?, AIPrompts?> {
            return suspendCancellableCoroutine { continuation ->
                val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/api_config?select=*&limit=1"
                
                println("=== 获取 AI 配置 ===")
                println("URL: $url")
                println("当前用户：${SupabaseClient.currentUser?.email}")
                println("用户 Token: ${if (SupabaseClient.currentUser?.token != null) "有" else "无"}")
                
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer ${SupabaseClient.currentUser?.token ?: ""}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                        println("=== 获取 AI 配置失败 - 网络错误 ===")
                        e.printStackTrace()
                        if (continuation.isActive) {
                            continuation.resume(Triple(null, null, null))
                        }
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        try {
                            val responseBody = response.body?.string()
                            println("=== 获取 AI 配置响应 ===")
                            println("响应码：${response.code}")
                            println("响应体：$responseBody")
                            
                            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                                val jsonArray = JSONArray(responseBody)
                                if (jsonArray.length() > 0) {
                                    val config = jsonArray.getJSONObject(0)
                                    val apiKey = config.optString("api_key", "").trim()
                                    val baseUrl = config.optString("api_base_url", "https://api.siliconflow.cn/v1").trim()
                                    val prompts = AIPrompts(
                                        polishPrompt = config.optString("polish_prompt", "你是一位专业的文字润色专家。请优化用户提供的文本，使其更加流畅、优美、专业。保持原意不变，只改进表达方式。直接返回润色后的文本，不要解释。"),
                                        summarizePrompt = config.optString("summarize_prompt", "你是一位专业的摘要生成专家。请为用户提供的文本生成简洁明了的摘要，提取核心要点，保持逻辑清晰。直接返回摘要，不要解释。"),
                                        translatePrompt = config.optString("translate_prompt", "你是一位专业的翻译专家。请将用户提供的文本翻译成中文（如果原文是中文则翻译成英文）。保持专业术语准确，语句流畅。直接返回翻译结果，不要解释。"),
                                        grammarPrompt = config.optString("grammar_prompt", "你是一位专业的语法检查专家。请检查用户提供的文本中的语法错误、拼写错误、标点符号错误等，并提供修改建议。格式：先列出错误，然后给出修改后的完整文本。"),
                                        formatPrompt = config.optString("format_prompt", "你是一位专业的 Markdown 格式专家。请优化用户提供的文本的 Markdown 格式，使其更加规范、美观、易读。直接返回格式化后的文本，不要解释。")
                                    )
                                    println("API Key: ${if (apiKey.isNotEmpty()) "有" else "无"}")
                                    if (continuation.isActive) {
                                        continuation.resume(Triple(apiKey, baseUrl, prompts))
                                    }
                                } else {
                                    println("api_config 表为空")
                                    if (continuation.isActive) {
                                        continuation.resume(Triple(null, null, null))
                                    }
                                }
                            } else {
                                println("请求失败：${response.code}")
                                if (continuation.isActive) {
                                    continuation.resume(Triple(null, null, null))
                                }
                            }
                        } catch (e: Exception) {
                            println("解析响应失败：${e.message}")
                            e.printStackTrace()
                            if (continuation.isActive) {
                                continuation.resume(Triple(null, null, null))
                            }
                        }
                    }
                })
            }
        }
        
        // 从云端获取 AI 模型列表
        suspend fun loadAIModelsFromCloud(): Boolean {
            return suspendCancellableCoroutine { continuation ->
                val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/ai_models?select=*&order=sort.asc"
                
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
                    .addHeader("Authorization", "Bearer ${SupabaseClient.currentUser?.token ?: ""}")
                    .addHeader("Content-Type", "application/json")
                    .build()
                
                SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }
                    
                    override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                        try {
                            val responseBody = response.body?.string()
                            if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                                val jsonArray = JSONArray(responseBody)
                                val models = mutableListOf<AIModel>()
                                for (i in 0 until jsonArray.length()) {
                                    val json = jsonArray.getJSONObject(i)
                                    models.add(AIModel.fromJson(json))
                                }
                                AIModelConfig.setModels(models)
                                if (continuation.isActive) {
                                    continuation.resume(true)
                                }
                            } else {
                                if (continuation.isActive) {
                                    continuation.resume(false)
                                }
                            }
                        } catch (e: Exception) {
                            if (continuation.isActive) {
                                continuation.resume(false)
                            }
                        }
                    }
                })
            }
        }
    }
    
    private val client = OkHttpClient()
    private var apiKey: String? = null
    private var apiBaseUrl: String = "https://api.siliconflow.cn/v1"
    
    /**
     * 使用硬编码配置初始化
     */
    suspend fun initWithConfig(key: String, baseUrl: String) {
        apiKey = key
        apiBaseUrl = baseUrl
    }
    
    /**
     * 初始化 AI 服务，从云端获取配置
     */
    suspend fun init(): Boolean {
        val config = getAIConfigFromCloud()
        apiKey = config.first
        apiBaseUrl = config.second ?: "https://api.siliconflow.cn/v1"
        currentPrompts = config.third
        return apiKey != null
    }
    
    /**
     * 简单的聊天方法（带系统提示词）
     */
    suspend fun chat(
        model: AIModel,
        message: String,
        systemPrompt: String? = null
    ): Result<String> {
        if (apiKey == null) {
            return Result.failure(Exception("未初始化 AI 服务"))
        }

        return suspendCancellableCoroutine { continuation ->
            val url = "$apiBaseUrl/chat/completions"

            val requestBody = JSONObject().apply {
                put("model", model.id)
                put("messages", JSONArray().apply {
                    if (!systemPrompt.isNullOrBlank()) {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                    }
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", message)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 2048)
            }
            
            val request = Request.Builder()
                .url(url)
                .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer ${apiKey ?: ""}")
                .addHeader("Content-Type", "application/json")
                .build()
            
            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(e))
                    }
                }
                
                override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                    val responseBody = response.body?.string()
                    
                    try {
                        if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                            val jsonResponse = JSONObject(responseBody)
                            val choices = jsonResponse.getJSONArray("choices")
                            if (choices.length() > 0) {
                                val message = choices.getJSONObject(0).getJSONObject("message")
                                val content = message.getString("content")
                                if (continuation.isActive) {
                                    continuation.resume(Result.success(content))
                                }
                            } else {
                                if (continuation.isActive) {
                                    continuation.resume(Result.failure(Exception("No choices in response")))
                                }
                            }
                        } else {
                            if (continuation.isActive) {
                                continuation.resume(Result.failure(Exception("API request failed: ${response.code}")))
                            }
                        }
                    } catch (e: Exception) {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(e))
                        }
                    }
                }
            })
        }
    }
    
    /**
     * AI 工具类型
     */
    enum class AIToolType {
        POLISH,      // 润色
        SUMMARIZE,   // 总结
        TRANSLATE,   // 翻译
        GRAMMAR,     // 语法检查
        FORMAT       // 格式转换
    }
    
    /**
     * 获取 AI 工具的系统提示词（从云端配置或默认值）
     */
    private fun getSystemPrompt(toolType: AIToolType): String {
        return when (toolType) {
            AIToolType.POLISH -> currentPrompts?.polishPrompt ?: "你是一位专业的文字润色专家。请优化用户提供的文本，使其更加流畅、优美、专业。保持原意不变，只改进表达方式。直接返回润色后的文本，不要解释。"
            AIToolType.SUMMARIZE -> currentPrompts?.summarizePrompt ?: "你是一位专业的摘要生成专家。请为用户提供的文本生成简洁明了的摘要，提取核心要点，保持逻辑清晰。直接返回摘要，不要解释。"
            AIToolType.TRANSLATE -> currentPrompts?.translatePrompt ?: "你是一位专业的翻译专家。请将用户提供的文本翻译成中文（如果原文是中文则翻译成英文）。保持专业术语准确，语句流畅。直接返回翻译结果，不要解释。"
            AIToolType.GRAMMAR -> currentPrompts?.grammarPrompt ?: "你是一位专业的语法检查专家。请检查用户提供的文本中的语法错误、拼写错误、标点符号错误等，并提供修改建议。格式：先列出错误，然后给出修改后的完整文本。"
            AIToolType.FORMAT -> currentPrompts?.formatPrompt ?: "你是一位专业的 Markdown 格式专家。请优化用户提供的文本的 Markdown 格式，使其更加规范、美观、易读。直接返回格式化后的文本，不要解释。"
        }
    }
    
    /**
     * 调用 AI API
     */
    private suspend fun callAI(
        model: AIModel,
        systemPrompt: String,
        userContent: String
    ): Result<String> = suspendCancellableCoroutine { continuation ->
        val url = "$apiBaseUrl/chat/completions"
        
        val requestBody = JSONObject().apply {
            put("model", model.id)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", systemPrompt)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userContent)
                })
            })
            put("temperature", 0.7)
            put("max_tokens", 2048)
        }
        
        val request = Request.Builder()
            .url(url)
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .addHeader("Authorization", "Bearer ${apiKey ?: ""}")
            .addHeader("Content-Type", "application/json")
            .build()
        
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(e))
                }
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val responseBody = response.body?.string()
                
                try {
                    if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                        val jsonResponse = JSONObject(responseBody)
                        val choices = jsonResponse.getJSONArray("choices")
                        if (choices.length() > 0) {
                            val message = choices.getJSONObject(0).getJSONObject("message")
                            val content = message.getString("content")
                            if (continuation.isActive) {
                                continuation.resume(Result.success(content))
                            }
                        } else {
                            if (continuation.isActive) {
                                continuation.resume(Result.failure(Exception("No choices in response")))
                            }
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception("API request failed: ${response.code}")))
                        }
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(e))
                    }
                }
            }
        })
    }
    
    /**
     * 智能润色
     */
    suspend fun polishText(model: AIModel, text: String): Result<String> {
        if (apiKey == null) {
            if (!init()) {
                return Result.failure(Exception("Failed to get API key from cloud"))
            }
        }
        return callAI(model, getSystemPrompt(AIToolType.POLISH), text)
    }
    
    /**
     * 内容总结
     */
    suspend fun summarizeText(model: AIModel, text: String): Result<String> {
        if (apiKey == null) {
            if (!init()) {
                return Result.failure(Exception("Failed to get API key from cloud"))
            }
        }
        return callAI(model, getSystemPrompt(AIToolType.SUMMARIZE), text)
    }
    
    /**
     * 智能翻译
     */
    suspend fun translateText(model: AIModel, text: String): Result<String> {
        if (apiKey == null) {
            if (!init()) {
                return Result.failure(Exception("Failed to get API key from cloud"))
            }
        }
        return callAI(model, getSystemPrompt(AIToolType.TRANSLATE), text)
    }
    
    /**
     * 语法检查
     */
    suspend fun checkGrammar(model: AIModel, text: String): Result<String> {
        if (apiKey == null) {
            if (!init()) {
                return Result.failure(Exception("Failed to get API key from cloud"))
            }
        }
        return callAI(model, getSystemPrompt(AIToolType.GRAMMAR), text)
    }
    
    /**
     * 格式转换
     */
    suspend fun formatText(model: AIModel, text: String): Result<String> {
        if (apiKey == null) {
            if (!init()) {
                return Result.failure(Exception("Failed to get API key from cloud"))
            }
        }
        return callAI(model, getSystemPrompt(AIToolType.FORMAT), text)
    }
}
