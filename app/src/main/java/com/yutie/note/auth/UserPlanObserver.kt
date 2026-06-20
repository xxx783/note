package com.yutie.note.auth

import android.app.Application
import androidx.lifecycle.LifecycleObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户计划观察者
 * 使用 StateFlow 暴露当前用户的 plan，供全局观察
 */
class UserPlanObserver(private val application: Application) : LifecycleObserver {
    
    // 使用 StateFlow 暴露当前 plan
    private val _currentPlan = MutableStateFlow("free")
    val currentPlan: StateFlow<String> = _currentPlan.asStateFlow()
    
    // 协程作用域
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // 缓存时间：5 分钟
    private var lastRefreshTime: Long = 0L
    private val CACHE_DURATION = 5 * 60 * 1000L
    
    /**
     * 初始化，在 Application 中调用
     */
    fun init() {
        // 监听登录状态变化，登录后自动刷新 plan
        scope.launch {
            // 延迟一下，等待 SupabaseClient 初始化
            kotlinx.coroutines.delay(500)
            refreshIfLoggedIn()
        }
    }
    
    /**
     * 刷新用户计划（从网络获取）
     */
    fun refresh() {
        scope.launch {
            try {
                val plan = com.yutie.note.utils.FeatureGuard.getCurrentPlan(forceRefresh = true)
                _currentPlan.value = plan
                lastRefreshTime = System.currentTimeMillis()
            } catch (e: Exception) {
                e.printStackTrace()
                // 失败时保持原值
            }
        }
    }
    
    /**
     * 如果已登录，则刷新计划
     */
    private fun refreshIfLoggedIn() {
        if (com.yutie.note.utils.SupabaseClient.isLoggedIn()) {
            refresh()
        } else {
            _currentPlan.value = "free"
        }
    }
    
    /**
     * 登录成功后调用
     */
    fun onLoginSuccess() {
        refresh()
    }
    
    /**
     * 登出后调用
     */
    fun onLogout() {
        _currentPlan.value = "free"
        lastRefreshTime = 0L
    }
    
    /**
     * 检查是否需要刷新（缓存过期）
     */
    fun checkAndRefresh() {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - lastRefreshTime) > CACHE_DURATION) {
            refresh()
        }
    }
    
    /**
     * 获取当前 plan（同步方法，返回缓存值）
     */
    fun getCurrentPlanSync(): String {
        return _currentPlan.value
    }
    
    /**
     * 检查是否为 Pro 或 APEX（同步方法，返回缓存值）
     */
    fun isProOrApexSync(): Boolean {
        val plan = getCurrentPlanSync()
        return plan == "pro" || plan == "apex"
    }
    
    /**
     * 检查是否为 APEX（同步方法，返回缓存值）
     */
    fun isApexSync(): Boolean {
        return getCurrentPlanSync() == "apex"
    }
}
