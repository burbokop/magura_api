package io.github.burbokop.magura.utils

import play.api.libs.json.{JsError, JsSuccess, JsValue, Json, Reads}

object JsonUtils {
  final case class HttpStatusException(statusCode: Int, message: String) extends Exception(message)

  final case class EmptyBodyException(message: String) extends Exception(message)

  final case class UnsuccessfulResponseException(url: String, message: String) extends Exception(s"url: $url, massage: $message")

  final case class JsonParseException(message: String, jsError: JsError, body: String) extends Exception(message)

  final case class JsonValidationException(message: String, jsError: JsError) extends Exception(message)

  def validateEitherThrowable[A](value: JsValue)(implicit jsonReads: Reads[A]) =
    value.validate[A] match {
      case s: JsSuccess[A] => Right(s.get)
      case jsError: JsError => Left(JsonValidationException(jsError.toString, jsError))
    }

  def deserializeEither[A](url: String, data: Either[String, String])(implicit jsonReads: Reads[A]): Either[String, A] =
    deserializeEitherThrowable(url, data).left.map(_.getMessage)

  def deserializeEitherThrowable[A](url: String, data: Either[String, String])(implicit jsonReads: Reads[A]): Either[Throwable, A] =
    data.fold(
      error =>
        Left(UnsuccessfulResponseException(url, error)),
      data =>
        if(data.isEmpty) {
          Left(EmptyBodyException("data is empty"))
        } else {
          Json.parse(data).validate[A] match {
            case s: JsSuccess[A] => Right(s.get)
            case jsError: JsError => Left(JsonParseException(jsError.toString, jsError, data))
          }
        }
    )

}
