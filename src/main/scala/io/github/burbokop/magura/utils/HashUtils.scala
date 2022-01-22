package io.github.burbokop.magura.utils

import java.math.BigInteger
import java.security.MessageDigest

object HashUtils {
  class StringImplicits(str: String) {
    def hash(algorithm: String, len: Int) =
      new BigInteger(1, MessageDigest.getInstance(algorithm).digest(str.getBytes("UTF-8"))).toString(len)

    def sha256(): String = hash("SHA-256", 32)
    def md5(): String = hash("MD5", 16)
  }
  object StringImplicits {
    implicit def apply(str: String) = new StringImplicits(str)
  }
}
