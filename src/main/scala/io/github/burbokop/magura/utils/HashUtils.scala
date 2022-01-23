package io.github.burbokop.magura.utils

import java.security.{MessageDigest, DigestInputStream}
import java.io.{File, FileInputStream}

import java.math.BigInteger

object HashUtils {
  class StringImplicits(str: String) {
    def hash(algorithm: String, len: Int) =
      new BigInteger(1, MessageDigest.getInstance(algorithm).digest(str.getBytes("UTF-8"))).toString(len)

    def sha256(): String = hash("SHA-256", 32)
    def md5(): String = hash("MD5", 16)
  }
  object StringImplicits { implicit def apply(str: String) = new StringImplicits(str) }

  class FileImplicits(file: File) {
    def contentHash(algorithm: String): String = {
      val buffer = new Array[Byte](8192)
      val algo = MessageDigest.getInstance(algorithm)

      val dis = new DigestInputStream(new FileInputStream(file), algo)
      try { while (dis.read(buffer) != -1) { } } finally { dis.close() }

      algo.digest.map("%02x".format(_)).mkString
    }
    def contentMd5: String = contentHash("MD5")
  }
  object FileImplicits { implicit def apply(file: File) = new FileImplicits(file) }
}
