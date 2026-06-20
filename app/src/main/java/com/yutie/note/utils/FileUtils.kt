package com.yutie.note.utils

import android.content.Context
import android.net.Uri
import java.io.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 文件操作工具类
 * 提供文件读写、导出、导入等功能
 */
object FileUtils {
    
    /**
     * 导出笔记到文件
     * @param context 上下文
     * @param fileName 文件名
     * @param content 内容
     * @return 是否成功
     */
    fun exportToFile(context: Context, fileName: String, content: String): Boolean {
        return try {
            val file = File(context.getExternalFilesDir(null), fileName)
            val writer = FileWriter(file)
            writer.use {
                it.write(content)
                it.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 从文件导入内容
     * @param context 上下文
     * @param uri 文件 URI
     * @return 文件内容
     */
    fun importFromFile(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
                val builder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line).append("\n")
                }
                builder.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查文件是否存在
     */
    fun isFileExists(filePath: String): Boolean {
        return File(filePath).exists()
    }
}
