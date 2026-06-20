package com.yutie.note.model

import org.json.JSONObject

/**
 * AI 模型数据类
 */
data class AIModel(
    val id: String,
    val name: String,
    val inputPrice: Double, // 输入价格 $/1K Tokens
    val outputPrice: Double, // 输出价格 $/1K Tokens
    val cacheReadPrice: Double = 0.0, // 缓存读取价格
    val cacheWritePrice: Double = 0.0, // 缓存创建价格
    val isAuto: Boolean = false, // 是否是自动路由
    val description: String = "",
    val enabled: Boolean = true, // 是否启用
    val sort: Int = 0 // 排序
) {
    companion object {
        fun fromJson(json: JSONObject): AIModel {
            return AIModel(
                id = json.optString("id", ""),
                name = json.optString("name", ""),
                inputPrice = json.optDouble("input_price", 0.0),
                outputPrice = json.optDouble("output_price", 0.0),
                cacheReadPrice = json.optDouble("cache_read_price", 0.0),
                cacheWritePrice = json.optDouble("cache_write_price", 0.0),
                isAuto = json.optBoolean("is_auto", false),
                description = json.optString("description", ""),
                enabled = json.optBoolean("enabled", true),
                sort = json.optInt("sort", 0)
            )
        }
    }
}

/**
 * AI 模型配置
 */
object AIModelConfig {
    private var modelsCache: List<AIModel> = emptyList()
    
    val models: List<AIModel>
        get() = modelsCache.filter { it.enabled }.sortedBy { it.sort }
    
    fun setModels(models: List<AIModel>) {
        modelsCache = models
    }
    
    fun getModelById(id: String): AIModel? {
        return models.find { it.id == id }
    }
    
    fun getAutoModel(): AIModel {
        return models.find { it.isAuto } ?: models.firstOrNull() ?: AIModel(
            id = "auto",
            name = "🚀 全自动模型路由",
            inputPrice = 0.0,
            outputPrice = 0.0,
            isAuto = true,
            description = "智能选择最优模型，自动调优到质量最好、速度最快最稳定的模型"
        )
    }
}
