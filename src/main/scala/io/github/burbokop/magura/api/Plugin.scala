package io.github.burbokop.magura.api

import io.github.burbokop.magura.repository.RepositoryProvider

import scala.collection.mutable


trait Plugin {
  def name(): String
  def repositoryProviders(): Map[String, RepositoryProvider]
  def generators(): Map[String, Generator]
  def generatorField(maguraFile: MaguraFile): String
  final def newDistributor(): GeneratorDistributor =
    new GeneratorDistributor(generators(), repositoryProviders(), generatorField)
}

  trait TC[T] {
    def func1(): T
  }

object Plugin {

  var list = mutable.MutableList[TC[_]]()

  implicit val a = new TC[Plugin] {
    override def func1(): Plugin = ???
  }
  list += a

  val f = newSerializer[Plugin](() => fdfdf)
  val f = newDeserializer[Plugin](() => fdfdf)
}


val a: ClassName = getSerialiser("org.fgfgfg.fgfg.ClassName")("file")