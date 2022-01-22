package io.github.burbokop.magura.utils

import scala.annotation.tailrec


class FlagProvider(args: Array[String]) {
  def stringFlag(name: String): Option[String] = {
    @tailrec
    def iterator(args: Array[String]): Option[String] = {
      if(args.length > 1) {
        if(args.head == name) {
          Some(args.tail.head)
        } else {
          iterator(args.tail)
        }
      } else {
        None
      }
    }
    iterator(args)
  }

  def boolFlag(name: String): Boolean = args.contains(name)

  def charFlag(name: String): Option[Char] = {
    val string: Option[String] = stringFlag(name)
    if (string.isDefined) {
      if(string.get.length > 0) Some(string.get.head) else None
    } else {
      None
    }
  }
}
