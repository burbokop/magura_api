package io.github.burbokop.magura.models.repository

import play.api.libs.json.Json

case class RepositoryRelease(
                          tagName: String,
                          name: Option[String]
                        )

object RepositoryRelease {
  implicit val format = Json.format[RepositoryRelease]
}