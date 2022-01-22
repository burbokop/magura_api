package io.github.burbokop.magura.api


trait Plugin {
  def name(): String
  def generators(): Map[String, Generator]
}