package io.github.burbokop.magura.api

import io.github.burbokop.magura.repository.MaguraRepository
import io.github.burbokop.magura.utils.YamlReadable
import org.yaml.snakeyaml.Yaml

import java.io.{FileInputStream, IOException, InputStream}
import scala.collection.mutable
import scala.collection.JavaConverters.{collectionAsScalaIterableConverter, mapAsScalaMapConverter}
/*
 * FOR SCALA 1.13 USE THIS
 * import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}
 */

case class MaguraFile(
                       builder: String,
                       connector: String,
                       dependencies: List[MaguraRepository]
                     )



object MaguraFile extends YamlReadable[MaguraFile] {
  case class Error(message: String) extends Exception(message)

  def fromBuilder(builder: String): MaguraFile = MaguraFile(builder, "", List())

  override def fromObject(map: mutable.Map[String, Any]): Either[Throwable, MaguraFile] = {
    val builder = map.get("builder").map(_.toString)
    val connector = map.get("connector").map(_.toString)
    val dependencies = map.get("dependencies").map(
      _.asInstanceOf[java.util.ArrayList[String]].asScala.toList.map(MaguraRepository.fromString)
    ).getOrElse(List())
    if(builder.isEmpty && connector.isEmpty) {
      Left(MaguraFile.Error("builder or/and connector must be set"))
    } else {
      (dependencies.partition(_.isLeft) match {
        case (Nil,  ints) => Right(for(Right(i) <- ints) yield i)
        case (strings, _) => Left(for(Left(s) <- strings) yield s)
      }).fold(e => Left(e.reduce((a: Throwable, b: Throwable) => MaguraFile.Error(a.getMessage + ", " + b.getMessage))), { repos =>
        Right(MaguraFile(
          builder.getOrElse(""),
          connector.getOrElse(""),
          repos
        ))
      })
    }
  }
}