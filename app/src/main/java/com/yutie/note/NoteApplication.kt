package com.yutie.note

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import com.yutie.note.auth.UserPlanObserver
import com.yutie.note.bean.NoteBean
import com.yutie.note.db.AppDatabase
import com.yutie.note.utils.LanguageManager
import com.yutie.note.utils.SPUtils
import com.yutie.note.utils.SupabaseClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 应用 Application 类
 * 初始化全局配置
 */
class NoteApplication : Application() {
    
    // 全局的 UserPlanObserver 实例
    lateinit var userPlanObserver: UserPlanObserver
        private set
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // 初始化语言设置
        LanguageManager.init(this)
    }
    
    override fun onCreate() {
        super.onCreate()
        // 初始化全局配置
        instance = this
        // 初始化 Supabase 客户端
        SupabaseClient.init(this)
        // 初始化用户计划观察者
        userPlanObserver = UserPlanObserver(this)
        userPlanObserver.init()
        
        // 初始化默认笔记（仅在第一次启动时）
        initDefaultNote()
    }
    
    /**
     * 初始化默认 Markdown 语法示例笔记
     * 仅在数据库中不存在示例笔记时创建一次
     * 如果用户主动删除了示例笔记，则不再重新创建
     */
    private fun initDefaultNote() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getInstance(this@NoteApplication)
                val noteDao = database.noteDao()
                
                // 检查数据库中是否已存在示例笔记
                val existingNotes = noteDao.getAllNotesList()
                val hasExampleNote = existingNotes.any { 
                    it.title == "Markdown 语法示例" || it.content.contains("# 标题 1") 
                }
                
                // 如果不存在，创建示例笔记
                if (!hasExampleNote) {
                    val markdownExample = """# 标题 1
## 标题 2
### 标题 3

**粗体文本** 或 __粗体__
*斜体文本* 或 _斜体_
~~删除线~~

- 无序列表项 1
- 无序列表项 2

1. 有序列表项 1
2. 有序列表项 2

> 引用文本

https://example.com
链接内容：https://example.com

```
def quick_sort(arr):
    if len(arr) <= 1:
        return arr
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quick_sort(left) + middle + quick_sort(right)

# 测试示例
if __name__ == "__main__":
    test_array = [3, 6, 8, 10, 1, 2, 1]
    print(quick_sort(test_array))
```


| 表格 | 示例 |
|-----|------|
| 单元格 | 内容 |


自动识别链接：https://example.com
""".trimIndent()
                    
                    val exampleNote = NoteBean(
                        title = "Markdown 语法示例",
                        content = markdownExample,
                        createTime = System.currentTimeMillis(),
                        modifyTime = System.currentTimeMillis(),
                        categoryId = 0,
                        isTop = 0,
                        isEncrypt = 0,
                        syncStatus = 0,
                        isDeleted = false,
                        isDraft = true
                    )
                    
                    noteDao.insertNote(exampleNote)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    companion object {
        lateinit var instance: NoteApplication
            private set
    }
}
