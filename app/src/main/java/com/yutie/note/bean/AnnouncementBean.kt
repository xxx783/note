package com.yutie.note.bean

import java.text.SimpleDateFormat
import java.util.*

/**
 * 公告数据类
 */
data class AnnouncementBean(
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val isImportant: Boolean = false,
    val publisherId: String = "",
    val publisherEmail: String = "",
    val createdAt: String = ""
) {
    /**
     * 格式化的发布时间
     */
    val formattedTime: String
        get() {
            return try {
                // Supabase 返回的格式可能是：
                // 1. 2026-04-12T09:40:11.209851+00:00（带时区偏移）
                // 2. 2026-04-12T09:40:11.209Z（UTC）
                // 先标准化处理
                val normalizedTime = createdAt
                    .replace("+00:00", "Z")
                    .replace("+0000", "Z")
                
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
                
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                outputFormat.timeZone = TimeZone.getDefault()
                
                val date = inputFormat.parse(normalizedTime)
                if (date != null) {
                    outputFormat.format(date)
                } else {
                    // 如果解析失败，直接返回原字符串
                    createdAt
                }
            } catch (e: Exception) {
                // 解析失败返回原字符串
                createdAt
            }
        }
}
