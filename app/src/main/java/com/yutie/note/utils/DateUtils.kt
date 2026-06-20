package com.yutie.note.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期时间工具类
 * 提供时间格式化、转换等功能
 */
object DateUtils {
    
    /**
     * 格式化时间戳
     * @param timestamp 时间戳
     * @param pattern 格式模板，默认 yyyy-MM-dd HH:mm:ss
     */
    fun formatTimestamp(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.CHINA)
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * 获取相对时间描述
     * @param timestamp 时间戳
     */
    fun getRelativeTimeDesc(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "刚刚"
            diff < 3600_000 -> "${diff / 60_000}分钟前"
            diff < 86400_000 -> "${diff / 3600_000}小时前"
            diff < 604800_000 -> "${diff / 86400_000}天前"
            else -> formatTimestamp(timestamp, "yyyy-MM-dd")
        }
    }
    
    /**
     * 判断是否是今天
     */
    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        
        return today.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }
    
    /**
     * 比较两个时间戳的大小
     * @return 正数表示 time1 晚于 time2，负数表示 time1 早于 time2，0 表示相等
     */
    fun compareTime(time1: Long, time2: Long): Int {
        return time1.compareTo(time2)
    }
}
