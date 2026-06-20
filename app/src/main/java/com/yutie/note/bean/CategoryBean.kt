package com.yutie.note.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 分类实体类
 * 对应数据库分类表
 */
@Entity(tableName = "category_table")
data class CategoryBean(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 分类名称
    val name: String,
    
    // 创建时间（时间戳）
    val createTime: Long = System.currentTimeMillis(),
    
    // 图标索引（用于选择不同图标）
    val iconIndex: Int = 0,
    
    // 排序索引
    val sortIndex: Int = 0
)
