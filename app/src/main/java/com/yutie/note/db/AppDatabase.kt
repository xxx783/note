package com.yutie.note.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yutie.note.bean.NoteBean
import com.yutie.note.bean.CategoryBean
import com.yutie.note.constant.AppConstants

/**
 * 应用数据库
 * 单例模式，管理数据库创建和升级
 */
@Database(
    entities = [NoteBean::class, CategoryBean::class],
    version = AppConstants.DB_VERSION,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun noteDao(): NoteDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                AppConstants.DB_NAME
            )
            .addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // 数据库创建时的初始化逻辑
                }
            })
            // 添加数据库迁移
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
        }
        
        // 数据库迁移：版本 1 -> 版本 2，添加 isDeleted 字段
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加 isDeleted 字段，默认值为 0（false）
                db.execSQL("ALTER TABLE note_table ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
            }
        }
        
        // 数据库迁移：版本 2 -> 版本 3，添加 cloudId 字段
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 添加 cloudId 字段，可为空
                db.execSQL("ALTER TABLE note_table ADD COLUMN cloudId INTEGER")
            }
        }
        
        // 数据库迁移：版本 3 -> 版本 4，重构主键为云端ID
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. 创建新表（使用云端ID作为主键）
                db.execSQL("""
                    CREATE TABLE note_table_new (
                        id INTEGER PRIMARY KEY NOT NULL,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        createTime INTEGER NOT NULL,
                        modifyTime INTEGER NOT NULL,
                        categoryId INTEGER NOT NULL DEFAULT 0,
                        isTop INTEGER NOT NULL DEFAULT 0,
                        isEncrypt INTEGER NOT NULL DEFAULT 0,
                        syncStatus INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        isDraft INTEGER NOT NULL DEFAULT 0
                    )
                """)

                // 2. 将旧数据迁移到新表（如果有 cloudId 就用 cloudId，否则用原 id 并标记为草稿）
                db.execSQL("""
                    INSERT INTO note_table_new
                    SELECT
                        CASE
                            WHEN cloudId IS NOT NULL THEN cloudId
                            ELSE id
                        END as id,
                        title,
                        content,
                        createTime,
                        modifyTime,
                        categoryId,
                        isTop,
                        isEncrypt,
                        syncStatus,
                        isDeleted,
                        CASE
                            WHEN cloudId IS NULL THEN 1
                            ELSE 0
                        END as isDraft
                    FROM note_table
                """)

                // 3. 删除旧表
                db.execSQL("DROP TABLE note_table")

                // 4. 重命名新表
                db.execSQL("ALTER TABLE note_table_new RENAME TO note_table")
            }
        }

        // 数据库迁移：版本 4 -> 版本 5，使用本地自增ID + 云端ID
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. 创建新表（使用本地自增ID作为主键，云端ID单独记录）
                db.execSQL("""
                    CREATE TABLE note_table_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cloudId INTEGER,
                        title TEXT NOT NULL,
                        content TEXT NOT NULL,
                        createTime INTEGER NOT NULL,
                        modifyTime INTEGER NOT NULL,
                        categoryId INTEGER NOT NULL DEFAULT 0,
                        isTop INTEGER NOT NULL DEFAULT 0,
                        isEncrypt INTEGER NOT NULL DEFAULT 0,
                        syncStatus INTEGER NOT NULL DEFAULT 0,
                        isDeleted INTEGER NOT NULL DEFAULT 0,
                        isDraft INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // 2. 将旧数据迁移到新表
                // 原来的 id 字段变成 cloudId，新的本地 id 自增生成
                db.execSQL("""
                    INSERT INTO note_table_new
                    (cloudId, title, content, createTime, modifyTime, categoryId, isTop, isEncrypt, syncStatus, isDeleted, isDraft)
                    SELECT
                        CASE WHEN id > 0 THEN id ELSE NULL END as cloudId,
                        title,
                        content,
                        createTime,
                        modifyTime,
                        categoryId,
                        isTop,
                        isEncrypt,
                        syncStatus,
                        isDeleted,
                        CASE WHEN id > 0 THEN 0 ELSE 1 END as isDraft
                    FROM note_table
                """)

                // 3. 删除旧表
                db.execSQL("DROP TABLE note_table")

                // 4. 重命名新表
                db.execSQL("ALTER TABLE note_table_new RENAME TO note_table")
            }
        }
    }
}
