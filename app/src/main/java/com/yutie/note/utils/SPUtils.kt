package com.yutie.note.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.yutie.note.constant.AppConstants

/**
 * SharedPreferences 工具类
 * 使用 EncryptedSharedPreferences 加密敏感数据
 */
class SPUtils private constructor(context: Context) {
    
    private val sp: SharedPreferences = createEncryptedSharedPreferences(context)
    
    companion object {
        @Volatile
        private var instance: SPUtils? = null
        
        fun getInstance(context: Context): SPUtils {
            return instance ?: synchronized(this) {
                instance ?: SPUtils(context.applicationContext).also { instance = it }
            }
        }
        
        private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                
                EncryptedSharedPreferences.create(
                    context,
                    AppConstants.SP_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("SPUtils", "Failed to create EncryptedSharedPreferences, falling back to plain text: ${e.message}")
                context.getSharedPreferences(
                    AppConstants.SP_FILE_NAME,
                    Context.MODE_PRIVATE
                )
            }
        }
    }
    
    fun getThemeMode(): Int {
        return sp.getInt(AppConstants.KEY_THEME_MODE, AppConstants.THEME_MODE_FOLLOW_SYSTEM)
    }
    
    fun setThemeMode(mode: Int) {
        sp.edit().putInt(AppConstants.KEY_THEME_MODE, mode).apply()
    }
    
    fun isEncryptEnabled(): Boolean {
        return sp.getBoolean(AppConstants.KEY_IS_ENCRYPT, false)
    }
    
    fun setEncryptEnabled(enabled: Boolean) {
        sp.edit().putBoolean(AppConstants.KEY_IS_ENCRYPT, enabled).apply()
    }
    
    fun getPassword(): String {
        return sp.getString(AppConstants.KEY_PASSWORD, "") ?: ""
    }
    
    fun setPassword(password: String) {
        sp.edit().putString(AppConstants.KEY_PASSWORD, password).apply()
    }
    
    fun getLastBackupTime(): Long {
        return sp.getLong(AppConstants.KEY_LAST_BACKUP_TIME, 0)
    }
    
    fun setLastBackupTime(time: Long) {
        sp.edit().putLong(AppConstants.KEY_LAST_BACKUP_TIME, time).apply()
    }
    
    fun clearAll() {
        sp.edit().clear().apply()
    }
}
