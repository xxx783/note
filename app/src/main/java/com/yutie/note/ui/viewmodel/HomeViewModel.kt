package com.yutie.note.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yutie.note.bean.NoteBean
import com.yutie.note.bean.CategoryBean
import com.yutie.note.db.AppDatabase
import com.yutie.note.utils.EncryptUtils
import com.yutie.note.utils.SPUtils
import com.yutie.note.utils.SupabaseNoteService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 主页面 ViewModel
 * 处理笔记列表相关业务逻辑
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getInstance(application)
    private val noteDao = database.noteDao()
    private val categoryDao = database.categoryDao()
    private val spUtils = SPUtils.getInstance(application)
    
    // 当前选中的分类 ID（0 表示全部分类）
    private val _currentCategoryId = MutableStateFlow(0L)
    val currentCategoryId: StateFlow<Long> = _currentCategoryId.asStateFlow()
    
    // 搜索关键词
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    // 所有笔记
    val allNotes: StateFlow<List<NoteBean>> = combine(
        noteDao.getAllNotes(),
        _currentCategoryId,
        _searchQuery
    ) { notes, categoryId, query ->
        var filteredNotes = notes
        
        // 按分类筛选
        if (categoryId > 0) {
            filteredNotes = filteredNotes.filter { it.categoryId == categoryId }
        }
        
        // 按关键词搜索
        if (query.isNotBlank()) {
            filteredNotes = filteredNotes.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.content.contains(query, ignoreCase = true)
            }
        }
        
        filteredNotes
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // 所有分类
    val allCategories: StateFlow<List<CategoryBean>> = categoryDao.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    // 是否启用加密
    val isEncryptEnabled: StateFlow<Boolean> = flow {
        emit(SPUtils.getInstance(getApplication()).isEncryptEnabled())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    /**
     * 设置当前分类
     */
    fun setCurrentCategoryId(categoryId: Long) {
        _currentCategoryId.value = categoryId
    }
    
    /**
     * 设置搜索关键词
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    /**
     * 删除笔记（本地软删除）
     */
    fun deleteNote(note: NoteBean) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }
    
    /**
     * 批量删除笔记（本地软删除）
     */
    fun deleteNotes(ids: List<Long>) {
        viewModelScope.launch {
            noteDao.deleteNotes(ids)
        }
    }
    
    /**
     * 从云端彻底删除笔记
     */
    fun deleteNoteFromCloud(note: NoteBean) {
        viewModelScope.launch {
            try {
                // 1. 从云端删除（使用回调等待完成）
                if (note.cloudId != null) {
                    println("开始删除云端笔记，cloudId=${note.cloudId}")
                    SupabaseNoteService.deleteNoteFromCloud(note.cloudId) { success, errorMsg ->
                        if (success) {
                            println("云端删除成功，开始删除本地数据库")
                            // 2. 云端删除成功后，从本地数据库彻底删除
                            viewModelScope.launch {
                                noteDao.deleteNoteById(note.id)
                                println("本地数据库删除完成")
                            }
                        } else {
                            // 删除失败，显示提示
                            println("云端删除失败：$errorMsg")
                        }
                    }
                } else {
                    println("没有云端 ID，直接删除本地数据")
                    // 没有云端 ID，直接删除本地
                    noteDao.deleteNoteById(note.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("删除异常：${e.message}")
            }
        }
    }
    
    /**
     * 批量从云端彻底删除笔记
     */
    fun deleteNotesFromCloud(ids: List<Long>) {
        viewModelScope.launch {
            try {
                // 获取要删除的笔记
                val allNotesList = noteDao.getAllNotesList()
                val notesToDelete = allNotesList.filter { it.id in ids }
                
                // 统计需要删除的云端笔记数量
                val cloudNotes = notesToDelete.filter { it.cloudId != null }
                
                println("批量删除：共${notesToDelete.size}篇笔记，其中${cloudNotes.size}篇有云端数据")
                
                if (cloudNotes.isEmpty()) {
                    // 没有云端笔记，直接删除本地
                    println("没有云端笔记，直接删除本地")
                    noteDao.deleteNotes(ids)
                    return@launch
                }
                
                // 用于跟踪删除进度
                var successCount = 0
                var failCount = 0
                
                // 从云端删除
                cloudNotes.forEach { note ->
                    println("开始删除云端笔记 [id=${note.id}, cloudId=${note.cloudId}]")
                    SupabaseNoteService.deleteNoteFromCloud(note.cloudId!!) { success, errorMsg ->
                        if (success) {
                            successCount++
                            println("云端删除成功 [${note.id}], 进度：$successCount/$cloudNotes.size")
                        } else {
                            failCount++
                            println("云端删除失败 [${note.id}]: $errorMsg")
                        }
                        
                        // 检查是否所有云端笔记都处理完成
                        if (successCount + failCount == cloudNotes.size) {
                            println("所有云端删除完成，成功$successCount 失败$failCount，开始删除本地数据库")
                            // 所有云端删除完成，删除本地数据库
                            viewModelScope.launch {
                                noteDao.deleteNotes(ids)
                                println("本地数据库删除完成")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("批量删除异常：${e.message}")
            }
        }
    }
    
    /**
     * 更新笔记置顶状态
     */
    fun toggleTopStatus(note: NoteBean) {
        viewModelScope.launch {
            noteDao.updateTopStatus(note.id, if (note.isTop == 1) 0 else 1)
        }
    }
    
    /**
     * 插入分类
     */
    suspend fun insertCategory(category: CategoryBean): Long {
        return categoryDao.insertCategory(category)
    }
    
    /**
     * 删除分类
     */
    fun deleteCategory(category: CategoryBean) {
        viewModelScope.launch {
            // 先删除该分类下的所有笔记
            noteDao.deleteNotesByCategory(category.id)
            // 再删除分类
            categoryDao.deleteCategory(category)
        }
    }
}
