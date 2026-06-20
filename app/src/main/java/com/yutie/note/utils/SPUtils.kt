package com.yutie.note.utils

import android.content.Context
import android.content.SharedPreferences
import com.yutie.note.constant.AppConstants

/**
 * SharedPreferences 工具类
 * 用于存储应用配置信息
 */
class SPUtils private constructor(context: Context) {
    
    private val sp: SharedPreferences = context.getSharedPreferences(
        AppConstants.SP_FILE_NAME, 
        Context.MODE_PRIVATE
    )
    
    companion object {
        @Volatile
        private var instance: SPUtils? = null
        
        fun getInstance(context: Context): SPUtils {
            return instance ?: synchronized(this) {
                instance ?: SPUtils(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // 主题模式
    fun getThemeMode(): Int {
        return sp.getInt(AppConstants.KEY_THEME_MODE, AppConstants.THEME_MODE_FOLLOW_SYSTEM)
    }
    
    fun setThemeMode(mode: Int) {
        sp.edit().putInt(AppConstants.KEY_THEME_MODE, mode).apply()
    }
    
    // 是否加密
    fun isEncryptEnabled(): Boolean {
        return sp.getBoolean(AppConstants.KEY_IS_ENCRYPT, false)
    }
    
    fun setEncryptEnabled(enabled: Boolean) {
        sp.edit().putBoolean(AppConstants.KEY_IS_ENCRYPT, enabled).apply()
    }
    
    // 密码
    fun getPassword(): String {
        return sp.getString(AppConstants.KEY_PASSWORD, "") ?: ""
    }
    
    fun setPassword(password: String) {
        sp.edit().putString(AppConstants.KEY_PASSWORD, password).apply()
    }
    
    // 最后备份时间
    fun getLastBackupTime(): Long {
        return sp.getLong(AppConstants.KEY_LAST_BACKUP_TIME, 0)
    }
    
    fun setLastBackupTime(time: Long) {
        sp.edit().putLong(AppConstants.KEY_LAST_BACKUP_TIME, time).apply()
    }
    
    // 清除所有数据
    fun clearAll() {
        sp.edit().clear().apply()
    }
}
