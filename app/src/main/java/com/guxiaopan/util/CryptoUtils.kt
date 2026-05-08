package com.guxiaopan.util

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

/**
 * RSA加密工具类
 * 用于授权码验证
 */
object CryptoUtils {

    // 内嵌公钥（Base64编码）- 实际使用时替换为真实公钥
    private const val PUBLIC_KEY_BASE64 = """
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0Z3VS5JJcds3xfn/ygWe
BmD3L2W8cR8VPKbXn7pJY9RqFkLpW8s3mX5Q2zL8bR5cX8kP3wM7vN2qR8tY6mK
fD7xQ4nW8vL3pR5cX9kM2vN8bR5cX3wP7mK2qR8tY6mKfD7xQ4nW8vL3pR5cX9kM
PLACEHOLDER_PUBLIC_KEY_REPLACE_WITH_REAL_ONE_IN_PRODUCTION
"""

    private var cachedPublicKey: PublicKey? = null

    /**
     * 获取公钥
     */
    private fun getPublicKey(): PublicKey {
        cachedPublicKey?.let { return it }
        val keyBytes = Base64.decode(PUBLIC_KEY_BASE64.trim().replace("\n", ""), Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val pubKey = keyFactory.generatePublic(keySpec)
        cachedPublicKey = pubKey
        return pubKey
    }

    /**
     * 验证签名
     * @param data 原始数据
     * @param signatureBase64 Base64编码的签名
     * @return 验证是否通过
     */
    fun verifySignature(data: String, signatureBase64: String): Boolean {
        return try {
            val publicKey = getPublicKey()
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(publicKey)
            sig.update(data.toByteArray(Charsets.UTF_8))
            val signatureBytes = Base64.decode(signatureBase64, Base64.DEFAULT)
            sig.verify(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 解析授权码
     * 授权码格式：Base64(JSON { phone, days, expiry, signature })
     * @return 解析结果 Pair(是否验证通过, 授权信息Map)
     */
    fun parseAuthCode(authCode: String): Pair<Boolean, Map<String, String>> {
        return try {
            val json = String(Base64.decode(authCode, Base64.DEFAULT), Charsets.UTF_8)
            val parts = json.split(",").map { it.trim() }
            val map = mutableMapOf<String, String>()
            for (part in parts) {
                val kv = part.split(":", limit = 2)
                if (kv.size == 2) {
                    val key = kv[0].trim().replace("\"", "")
                    val value = kv[1].trim().replace("\"", "")
                    map[key] = value
                }
            }
            val phone = map["phone"] ?: return Pair(false, emptyMap())
            val days = map["days"] ?: return Pair(false, emptyMap())
            val expiry = map["expiry"] ?: return Pair(false, emptyMap())
            val signature = map["signature"] ?: return Pair(false, emptyMap())

            val dataToVerify = "$phone$days$expiry"
            val valid = verifySignature(dataToVerify, signature)
            Pair(valid, map)
        } catch (e: Exception) {
            Pair(false, emptyMap())
        }
    }
}