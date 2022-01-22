package io.github.burbokop.magura.utils

import org.yaml.snakeyaml.Yaml


import scala.collection.JavaConverters.{collectionAsScalaIterableConverter, mapAsScalaMapConverter}
/*
 * FOR SCALA 1.13 USE THIS
 * import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}
 */

import java.io.{FileInputStream, IOException, InputStream}
import scala.collection.mutable

object YamlReadable {
  case class Error(message: String) extends Exception(message)
}

trait YamlReadable[T] {
  type YamlObject[V] = mutable.Map[String, V]
  type JObject[V] = java.util.Map[String, V]
  type JList[V] = java.util.ArrayList[V]

  def fromObject(map: YamlObject[Any]): Either[Throwable, T]

  def fromYaml(inputStream: InputStream): Either[Throwable, T] =
    fromObject(Option(new Yaml().load(inputStream).asInstanceOf[JObject[Any]].asScala).getOrElse(mutable.Map()))

  def fromYaml(path: String): Either[Throwable, T] = try {
    fromYaml(new FileInputStream(path))
  } catch {
    case e: IOException => Left(YamlReadable.Error(e.getMessage))
  }
}
