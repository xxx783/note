package com.yutie.note.utils

import android.util.Base64
import com.yutie.note.constant.AppConstants
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具类
 * 提供安全的对称加密功能
 */
object EncryptUtils {
    
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    
    /**
     * 生成加密密钥
     * @param password 密码
     */
    fun generateKey(password: String): ByteArray {
        val keyBytes = password.toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedKey = digest.digest(keyBytes)
        return hashedKey.copyOf(AppConstants.ENCRYPT_KEY_LENGTH)
    }
    
    /**
     * 加密内容（使用 AES/GCM 模式）
     * @param content 原始内容
     * @param password 密码
     * @return Base64 编码的加密内容（包含 IV）
     */
    fun encrypt(content: String, password: String): String {
        try {
            val key = generateKey(password)
            val iv = generateIV()
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = SecretKeySpec(key, AppConstants.ENCRYPT_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
            
            val encryptedBytes = cipher.doFinal(content.toByteArray(Charsets.UTF_8))
            
            val combined = iv + encryptedBytes
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return content
        }
    }
    
    /**
     * 解密内容（使用 AES/GCM 模式）
     * @param encryptedContent Base64 编码的加密内容（包含 IV）
     * @param password 密码
     * @return 解密后的原始内容
     */
    fun decrypt(encryptedContent: String, password: String): String {
        try {
            val key = generateKey(password)
            val combined = Base64.decode(encryptedContent, Base64.DEFAULT)
            
            val iv = combined.copyOf(GCM_IV_LENGTH)
            val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)
            
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = SecretKeySpec(key, AppConstants.ENCRYPT_ALGORITHM)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return encryptedContent
        }
    }
    
    /**
     * 生成随机 IV（初始化向量）
     */
    private fun generateIV(): ByteArray {
        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)
        return iv
    }
    
    /**
     * 验证密码是否匹配
     */
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }
    
    /**
     * 哈希密码（用于存储）
     * 使用 SHA-256 加盐哈希
     */
    fun hashPassword(password: String): String {
        val salt = AppConstants.PASSWORD_SALT.toByteArray(Charsets.UTF_8)
        val saltedPassword = salt + password.toByteArray(Charsets.UTF_8)
        
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(saltedPassword)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
