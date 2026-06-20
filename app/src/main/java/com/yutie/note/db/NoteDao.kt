package com.yutie.note.db

import androidx.room.*
import com.yutie.note.bean.NoteBean
import com.yutie.note.bean.CategoryBean
import kotlinx.coroutines.flow.Flow

/**
 * 笔记数据访问对象
 * 定义笔记表的增删改查操作
 */
@Dao
interface NoteDao {
    
    // 插入笔记
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteBean): Long
    
    // 批量插入
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteBean>)
    
    // 更新笔记
    @Update
    suspend fun updateNote(note: NoteBean)
    
    // 删除笔记
    @Delete
    suspend fun deleteNote(note: NoteBean)
    
    // 根据 ID 删除
    @Query("DELETE FROM note_table WHERE id = :id")
    suspend fun deleteNoteById(id: Long)
    
    // 批量删除
    @Query("DELETE FROM note_table WHERE id IN (:ids)")
    suspend fun deleteNotes(ids: List<Long>)
    
    // 根据分类删除
    @Query("DELETE FROM note_table WHERE categoryId = :categoryId")
    suspend fun deleteNotesByCategory(categoryId: Long)
    
    // 获取所有笔记（按修改时间倒序）
    @Query("SELECT * FROM note_table ORDER BY isTop DESC, modifyTime DESC")
    fun getAllNotes(): Flow<List<NoteBean>>
    
    // 根据分类获取笔记
    @Query("SELECT * FROM note_table WHERE categoryId = :categoryId ORDER BY isTop DESC, modifyTime DESC")
    fun getNotesByCategory(categoryId: Long): Flow<List<NoteBean>>
    
    // 搜索笔记（模糊查询标题和内容）
    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :keyword || '%' OR content LIKE '%' || :keyword || '%' ORDER BY modifyTime DESC")
    fun searchNotes(keyword: String): Flow<List<NoteBean>>
    
    // 根据 ID 获取笔记
    @Query("SELECT * FROM note_table WHERE id = :id")
    suspend fun getNoteById(id: Long): NoteBean?
    
    // 获取置顶笔记数量
    @Query("SELECT COUNT(*) FROM note_table WHERE isTop = 1")
    suspend fun getTopNotesCount(): Int
    
    // 更新置顶状态
    @Query("UPDATE note_table SET isTop = :isTop WHERE id = :id")
    suspend fun updateTopStatus(id: Long, isTop: Int)
    
    // 更新同步状态
    @Query("UPDATE note_table SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: Int)
    
    // 获取所有笔记（非 Flow 版本，用于导出）
    @Query("SELECT * FROM note_table ORDER BY modifyTime DESC")
    suspend fun getAllNotesList(): List<NoteBean>
    
    // 获取所有草稿笔记（未同步到云端）
    @Query("SELECT * FROM note_table WHERE isDraft = 1 ORDER BY modifyTime DESC")
    suspend fun getDraftNotes(): List<NoteBean>
    
    // 更新草稿状态
    @Query("UPDATE note_table SET isDraft = :isDraft WHERE id = :id")
    suspend fun updateDraftStatus(id: Long, isDraft: Boolean)
}
