package com.yutie.note.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yutie.note.bean.NoteBean
import com.yutie.note.db.AppDatabase
import com.yutie.note.utils.DateUtils
import com.yutie.note.utils.EncryptUtils
import com.yutie.note.utils.SPUtils
import com.yutie.note.utils.SupabaseClient
import com.yutie.note.utils.SupabaseNoteService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * 笔记编辑页面 ViewModel
 * 处理笔记编辑相关业务逻辑
 */
class NoteEditViewModel(
    application: Application
) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getInstance(application)
    private val noteDao = database.noteDao()
    private val spUtils = SPUtils.getInstance(application)
    
    // 当前编辑的笔记 ID（0 表示新建）- 需要在外部设置
    private var noteId: Long = 0L
    
    // 标题
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()
    
    // 内容
    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()
    
    // 选中的分类 ID
    private val _selectedCategoryId = MutableStateFlow(0L)
    val selectedCategoryId: StateFlow<Long> = _selectedCategoryId.asStateFlow()
    
    // 是否置顶
    private val _isTop = MutableStateFlow(false)
    val isTop: StateFlow<Boolean> = _isTop.asStateFlow()
    
    // 是否加密
    private val _isEncrypt = MutableStateFlow(false)
    val isEncrypt: StateFlow<Boolean> = _isEncrypt.asStateFlow()
    
    // 保存提示
    private val _showSaveHint = MutableStateFlow(false)
    val showSaveHint: StateFlow<Boolean> = _showSaveHint.asStateFlow()
    
    // 自动保存 Job
    private var autoSaveJob: Job? = null
    
    /**
     * 设置笔记 ID 并加载
     */
    fun setNoteId(id: Long) {
        // 每次都要重置数据并加载，避免 ViewModel 复用导致数据不更新
        noteId = id
        if (noteId > 0) {
            // 加载已有笔记
            loadNoteImmediate()
        } else {
            // 新建笔记，清空数据
            _title.value = ""
            _content.value = ""
            _selectedCategoryId.value = 0L
            _isTop.value = false
            _isEncrypt.value = false
        }
    }
    
    /**
     * 立即同步加载笔记（阻塞式，确保数据立即可用）
     */
    private fun loadNoteImmediate() {
        // 在 IO 线程同步执行数据库查询
        val note = runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            noteDao.getNoteById(noteId)
        }
        note?.let {
            _title.value = it.title
            _content.value = it.content
            _selectedCategoryId.value = it.categoryId
            _isTop.value = it.isTop == 1
            _isEncrypt.value = it.isEncrypt == 1
        }
    }
    
    /**
     * 加载笔记（用于外部调用，确保数据加载完成）
     */
    suspend fun loadNote(noteId: Long): NoteBean? {
        return with(kotlinx.coroutines.Dispatchers.IO) {
            noteDao.getNoteById(noteId)
        }
    }
    
    /**
     * 设置标题
     */
    fun setTitle(newTitle: String) {
        _title.value = newTitle
        scheduleAutoSave()
    }
    
    /**
     * 设置内容
     */
    fun setContent(newContent: String) {
        _content.value = newContent
        scheduleAutoSave()
    }
    
    /**
     * 设置分类
     */
    fun setSelectedCategoryId(categoryId: Long) {
        _selectedCategoryId.value = categoryId
    }
    
    /**
     * 切换置顶状态
     */
    fun toggleTop() {
        _isTop.value = !_isTop.value
    }
    
    /**
     * 切换加密状态
     */
    fun toggleEncrypt() {
        _isEncrypt.value = !_isEncrypt.value
    }
    
    /**
     * 安排自动保存
     */
    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(3000) // 3 秒后自动保存
            saveNote(showHint = false)
        }
    }
    
    /**
     * 保存笔记
     * 使用本地自增ID，云端ID单独记录
     */
    fun saveNote(showHint: Boolean = true) {
        viewModelScope.launch {
            val titleText = _title.value.trim()
            val contentText = _content.value.trim()

            // 验证输入
            if (titleText.isEmpty()) {
                // 标题为空，不保存
                return@launch
            }

            // 处理加密
            val finalContent = if (_isEncrypt.value && contentText.isNotEmpty()) {
                val password = spUtils.getPassword()
                if (password.isNotEmpty()) {
                    EncryptUtils.encrypt(contentText, password)
                } else {
                    contentText
                }
            } else {
                contentText
            }

            if (noteId > 0) {
                // 更新现有笔记
                val existingNote = with(kotlinx.coroutines.Dispatchers.IO) {
                    noteDao.getNoteById(noteId)
                }
                val note = NoteBean(
                    id = noteId,
                    cloudId = existingNote?.cloudId,
                    title = titleText,
                    content = finalContent,
                    createTime = existingNote?.createTime ?: System.currentTimeMillis(),
                    modifyTime = System.currentTimeMillis(),
                    categoryId = _selectedCategoryId.value,
                    isTop = if (_isTop.value) 1 else 0,
                    isEncrypt = if (_isEncrypt.value) 1 else 0,
                    isDraft = existingNote?.isDraft ?: true
                )
                noteDao.updateNote(note)

                // 同步到云端 - 使用 upsert（更新或插入）
                if (SupabaseClient.isLoggedIn()) {
                    SupabaseNoteService.upsertNote(note, callback = { success, result ->
                        if (success && result != null) {
                            // 更新云端 ID（result 是 cloudId 的字符串形式）
                            viewModelScope.launch {
                                val cloudId = result.toLongOrNull() ?: note.cloudId
                                val updatedNote = note.copy(cloudId = cloudId, isDraft = false)
                                noteDao.updateNote(updatedNote)
                            }
                        }
                    }, isRetry = false)
                }
            } else {
                // 新建笔记：先保存到本地获取自增ID
                val note = NoteBean(
                    id = 0, // 让数据库自动生成
                    cloudId = null,
                    title = titleText,
                    content = finalContent,
                    createTime = System.currentTimeMillis(),
                    modifyTime = System.currentTimeMillis(),
                    categoryId = _selectedCategoryId.value,
                    isTop = if (_isTop.value) 1 else 0,
                    isEncrypt = if (_isEncrypt.value) 1 else 0,
                    isDraft = true
                )

                // 插入本地数据库，获取自增ID
                val newId = noteDao.insertNote(note)
                noteId = newId // 更新当前编辑的ID

                // 如果已登录，同步到云端
                if (SupabaseClient.isLoggedIn()) {
                    val noteWithId = note.copy(id = newId)
                    SupabaseNoteService.uploadNote(noteWithId, callback = { success, result ->
                        if (success && result != null) {
                            // 更新云端 ID 和草稿状态
                            viewModelScope.launch {
                                val cloudId = result.toLongOrNull()
                                if (cloudId != null) {
                                    val syncedNote = noteWithId.copy(cloudId = cloudId, isDraft = false)
                                    noteDao.updateNote(syncedNote)
                                }
                            }
                        }
                    }, isRetry = false)
                }
            }

            if (showHint) {
                _showSaveHint.value = true
                delay(2000)
                _showSaveHint.value = false
            }
        }
    }
    
    /**
     * 删除笔记
     */
    fun deleteNote() {
        if (noteId > 0) {
            viewModelScope.launch {
                noteDao.getNoteById(noteId)?.let { note ->
                    noteDao.deleteNote(note)
                }
            }
        }
    }
    
    /**
     * 获取相对时间描述
     */
    fun getRelativeTimeDesc(timestamp: Long): String {
        return DateUtils.getRelativeTimeDesc(timestamp)
    }
    
    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}
