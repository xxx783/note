package com.yutie.note.constant

/**
 * 应用常量类
 * 存放所有固定数值、标识等常量
 */
object AppConstants {
    
    // 笔记类型
    const val NOTE_TYPE_TEXT = 1
    const val NOTE_TYPE_IMAGE = 2
    
    // 数据库版本
    const val DB_VERSION = 5
    const val DB_NAME = "local_note.db"
    
    // SharedPreferences 文件名
    const val SP_FILE_NAME = "local_note_config"
    
    // 配置键
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_IS_ENCRYPT = "is_encrypt"
    const val KEY_PASSWORD = "password"
    const val KEY_LAST_BACKUP_TIME = "last_backup_time"
    
    // 主题模式
    const val THEME_MODE_FOLLOW_SYSTEM = 0
    const val THEME_MODE_LIGHT = 1
    const val THEME_MODE_DARK = 2
    
    // 请求码
    const val REQUEST_CODE_EDIT_NOTE = 1001
    const val REQUEST_CODE_CATEGORY_MANAGE = 1002
    const val REQUEST_CODE_SETTINGS = 1003
    const val REQUEST_CODE_EXPORT_FILE = 1004
    const val REQUEST_CODE_IMPORT_FILE = 1005
    
    // 加密相关
    const val ENCRYPT_ALGORITHM = "AES"
    const val ENCRYPT_KEY_LENGTH = 16
    const val PASSWORD_MIN_LENGTH = 6
    const val PASSWORD_SALT = "yutie_note_salt_2024"
    
    // 文件相关
    const val FILE_ENCODING = "UTF-8"
    const val FILE_EXTENSION_TXT = ".txt"
    const val FILE_EXTENSION_BACKUP = ".backup"
    
    // 时间格式
    const val DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss"
    const val DATE_FORMAT_DATE_ONLY = "yyyy-MM-dd"
    const val DATE_FORMAT_TIME_ONLY = "HH:mm:ss"
    
    // 自动保存间隔（毫秒）
    const val AUTO_SAVE_INTERVAL = 3000L
    
    // 最大输入字数
    const val MAX_NOTE_TITLE_LENGTH = 100
    const val MAX_NOTE_CONTENT_LENGTH = 10000
}
