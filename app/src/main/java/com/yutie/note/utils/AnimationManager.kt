package com.yutie.note.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore 扩展属性
val Context.animationDataStore: DataStore<Preferences> by preferencesDataStore(name = "animation_settings")

/**
 * 动画配置数据类
 */
data class AnimationConfig(
    val speed: Float = 1.0f,              // 动画速度 (0.5x - 2.0x)
    val transitionType: String = "fade",  // 过渡类型：fade, slide, scale
    val springStiffness: Float = 50f,     // 弹性系数 (20-100)
    val enabled: Boolean = true           // 是否启用动画
)

/**
 * 动画管理器
 * 负责保存和读取用户的动画配置
 */
object AnimationManager {
    
    // Preferences keys
    private val SPEED_KEY = floatPreferencesKey("animation_speed")
    private val TRANSITION_TYPE_KEY = stringPreferencesKey("transition_type")
    private val SPRING_STIFFNESS_KEY = floatPreferencesKey("spring_stiffness")
    private val ENABLED_KEY = booleanPreferencesKey("animation_enabled")
    
    /**
     * 获取动画配置
     */
    suspend fun getAnimationConfig(context: Context): AnimationConfig {
        return context.animationDataStore.data
            .map { preferences ->
                AnimationConfig(
                    speed = preferences[SPEED_KEY] ?: 1.0f,
                    transitionType = preferences[TRANSITION_TYPE_KEY] ?: "fade",
                    springStiffness = preferences[SPRING_STIFFNESS_KEY] ?: 50f,
                    enabled = preferences[ENABLED_KEY] ?: true
                )
            }.first()
    }
    
    /**
     * 保存动画配置
     */
    suspend fun saveAnimationConfig(context: Context, config: AnimationConfig) {
        context.animationDataStore.edit { preferences ->
            preferences[SPEED_KEY] = config.speed
            preferences[TRANSITION_TYPE_KEY] = config.transitionType
            preferences[SPRING_STIFFNESS_KEY] = config.springStiffness
            preferences[ENABLED_KEY] = config.enabled
        }
    }
    
    /**
     * 获取动画速度
     */
    fun getSpeedMultiplier(config: AnimationConfig): Float {
        return config.speed.coerceIn(0.5f, 2.0f)
    }
    
    /**
     * 获取 Spring 动画的 stiffness 值
     */
    fun getSpringStiffness(config: AnimationConfig): Float {
        // 将用户的 20-100 映射到 Spring 的 stiffness 范围
        return config.springStiffness.coerceIn(20f, 100f)
    }
    
    /**
     * 是否启用动画
     */
    fun isAnimationEnabled(config: AnimationConfig): Boolean {
        return config.enabled
    }
}
