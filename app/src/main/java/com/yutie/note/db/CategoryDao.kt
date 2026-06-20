package com.yutie.note.db

import androidx.room.*
import com.yutie.note.bean.CategoryBean
import kotlinx.coroutines.flow.Flow

/**
 * 分类数据访问对象
 * 定义分类表的增删改查操作
 */
@Dao
interface CategoryDao {
    
    // 插入分类
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryBean): Long
    
    // 更新分类
    @Update
    suspend fun updateCategory(category: CategoryBean)
    
    // 删除分类
    @Delete
    suspend fun deleteCategory(category: CategoryBean)
    
    // 根据 ID 删除
    @Query("DELETE FROM category_table WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)
    
    // 获取所有分类
    @Query("SELECT * FROM category_table ORDER BY sortIndex ASC, createTime DESC")
    fun getAllCategories(): Flow<List<CategoryBean>>
    
    // 根据 ID 获取分类
    @Query("SELECT * FROM category_table WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryBean?
    
    // 获取分类数量
    @Query("SELECT COUNT(*) FROM category_table")
    suspend fun getCategoriesCount(): Int
    
    // 检查分类名称是否存在
    @Query("SELECT COUNT(*) FROM category_table WHERE name = :name")
    suspend fun isCategoryNameExists(name: String): Boolean
    
    // 获取所有分类（非 Flow 版本）
    @Query("SELECT * FROM category_table ORDER BY sortIndex ASC")
    suspend fun getAllCategoriesList(): List<CategoryBean>
}
