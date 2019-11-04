package com.danielsan.natapi.helpers

import java.nio.charset.StandardCharsets
import java.util.Base64

import com.typesafe.config.{Config, ConfigFactory}
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import javax.crypto.Cipher

object Crypto {
  private implicit val conf: Config = ConfigFactory.load()

  private val key = new SecretKeySpec(conf.getString("secret.key").getBytes, "AES")
  private val iv = new IvParameterSpec(conf.getString("secret.iv").getBytes)

  private val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
  cipher.init(Cipher.ENCRYPT_MODE, key, iv)

  private val decipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
  decipher.init(Cipher.DECRYPT_MODE, key, iv)

  private val encoder = Base64.getEncoder
  private val decoder = Base64.getDecoder

  def encrypt(message: String): String = encoder.encodeToString(cipher.doFinal(message.getBytes("UTF-8")))
  def decrypt(message: String): String = new String(decipher.doFinal(decoder.decode(message)), StandardCharsets.UTF_8)
}
