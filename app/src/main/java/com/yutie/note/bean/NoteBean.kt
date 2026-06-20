package com.yutie.note.bean

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 笔记实体类
 * 对应数据库笔记表（支持云端同步）
 * 使用本地自增ID作为主键，云端ID单独记录
 */
@Entity(tableName = "note_table")
data class NoteBean(
    // 本地自增ID（主键）
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 云端ID（同步后填充）
    val cloudId: Long? = null,

    // 标题
    val title: String,

    // 内容
    val content: String,

    // 创建时间（时间戳）
    val createTime: Long = System.currentTimeMillis(),

    // 修改时间（时间戳）
    val modifyTime: Long = System.currentTimeMillis(),

    // 分类 ID
    val categoryId: Long = 0,

    // 是否置顶（0：否，1：是）
    val isTop: Int = 0,

    // 是否加密（0：否，1：是）
    val isEncrypt: Int = 0,

    // 同步状态（0：未同步，1：已同步，2：待删除）
    val syncStatus: Int = 0,

    // 是否已删除（软删除）
    val isDeleted: Boolean = false,

    // 是否草稿（本地新建但未同步到云端）
    val isDraft: Boolean = true
)
