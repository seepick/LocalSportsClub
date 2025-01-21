package seepick.localsportsclub.service

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

object Encrypter {

    private const val ALGORITHM = "AES"
    private const val AES_KEY_LENGTH = 32
    private val password = "LSCPwd_${System.getProperty("user.name")}"
        .take(AES_KEY_LENGTH)
        .padEnd(AES_KEY_LENGTH, 'x')

    fun encrypt(plainText: String): String {
        val encryptedBytes = cipher(Cipher.ENCRYPT_MODE).doFinal(plainText.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(cipherText: String): String {
        val decryptedBytes = cipher(Cipher.DECRYPT_MODE).doFinal(Base64.getDecoder().decode(cipherText))
        return String(decryptedBytes)
    }

    private fun cipher(mode: Int): Cipher = Cipher.getInstance(ALGORITHM).apply {
        init(mode, key())
    }

    private fun key(): SecretKey = SecretKeySpec(password.toByteArray(), ALGORITHM)
}
