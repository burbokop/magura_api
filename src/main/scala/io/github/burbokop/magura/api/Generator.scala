package io.github.burbokop.magura.api

import io.github.burbokop.magura.api.Generator.Options
import io.github.burbokop.magura.models.meta.RepositoryMetaData
import io.github.burbokop.magura.repository.RepositoryProvider
import io.github.burbokop.magura.utils.ReflectUtils._
import play.api.libs.json.{Format, JsNull, JsObject, JsResult, JsString, JsValue, Reads, Writes}

import java.io.File
import scala.annotation.StaticAnnotation
import scala.collection.mutable
import scala.reflect.runtime
import scala.util.{Failure, Success, Try}

object Generator {
  case class Error(message: String) extends Exception(message)



  abstract class Options {
    def hashName(): String
  }

  object Options {
    case class FormatAttached(
                       serialization: Options => JsValue,
                       deserialization: JsValue => Either[Throwable, Options]
                     ) extends StaticAnnotation

    val writes = new Writes[Options] {
      override def writes(options: Options): JsValue = {
        val clazz = runtime.currentMirror.instanceType(options).toString
        val data = runtime.currentMirror.invokeAttachedMethod[Options, JsValue](clazz, options).getOrElse(JsNull)
        JsObject(
          Map("class" -> JsString(clazz))
            ++ (if(data == JsNull) Map() else Map("data" -> data))
        )
      }
    }

    val reads = new Reads[Options] {
      override def reads(value: JsValue): JsResult[Options] =
        JsResult.fromTry(
          value.validate[JsObject].map(obj =>
            obj.value.get("class").map(clazz =>
              runtime.currentMirror.invokeAttachedMethod[JsValue, Either[Throwable, Options]](clazz.as[String], obj.value.getOrElse("data", JsNull))
                .getOrElse(Left(Generator.Error(s"Deserialization function with signature JsValue => Either[Throwable, Options] not registered for: $clazz")))
            )
              .getOrElse(Left(Generator.Error(s"'class' field not found")))
          )
            .getOrElse(Left(Generator.Error(s"Json value must be an object")))
            .toTry
        )
    }

    implicit val jsonFormat = Format[Options](reads, writes)
  }

  @Options.FormatAttached(
    serialization = DefaultOptions.serialization,
    deserialization = DefaultOptions.deserialization
  )
  case class DefaultOptions() extends Options {
    override def hashName(): String = "default"
  }

  object DefaultOptions {
    def serialization(options: Options): JsValue = JsNull
    def deserialization(value: JsValue): Either[Throwable, Options] = Right(DefaultOptions())
  }

  def repositoryName(inputPath: String): String =
    new File(inputPath).getParentFile.getName

  case class Result(
                     changed: Boolean,
                     newGenerators: Map[String, Generator] = Map(),
                     newRepositoryProviders: Map[String, RepositoryProvider] = Map(),
                   )
}

trait Generator {
  def proceed(
               cache: List[RepositoryMetaData],
               inputPath: String,
               outputPath: String,
               options: Options,
               maguraFile: MaguraFile,
             ): Either[Throwable, Generator.Result]

}

