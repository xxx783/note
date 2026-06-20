package com.yutie.note.utils

import android.util.Base64
import com.yutie.note.constant.AppConstants
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具类
 * 提供简单的对称加密功能
 */
object EncryptUtils {
    
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    
    /**
     * 生成加密密钥
     * @param password 密码
     */
    fun generateKey(password: String): ByteArray {
        val keyBytes = password.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedKey = digest.digest(keyBytes)
        // AES 密钥长度为 16 字节
        return hashedKey.copyOf(AppConstants.ENCRYPT_KEY_LENGTH)
    }
    
    /**
     * 加密内容
     * @param content 原始内容
     * @param password 密码
     * @return Base64 编码的加密内容
     */
    fun encrypt(content: String, password: String): String {
        try {
            val key = generateKey(password)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = SecretKeySpec(key, AppConstants.ENCRYPT_ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val encryptedBytes = cipher.doFinal(content.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return content
        }
    }
    
    /**
     * 解密内容
     * @param encryptedContent Base64 编码的加密内容
     * @param password 密码
     * @return 解密后的原始内容
     */
    fun decrypt(encryptedContent: String, password: String): String {
        try {
            val key = generateKey(password)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = SecretKeySpec(key, AppConstants.ENCRYPT_ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey)
            val encryptedBytes = Base64.decode(encryptedContent, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return encryptedContent
        }
    }
    
    /**
     * 验证密码是否匹配
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }
    
    /**
     * 哈希密码（用于存储）
     */
    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
