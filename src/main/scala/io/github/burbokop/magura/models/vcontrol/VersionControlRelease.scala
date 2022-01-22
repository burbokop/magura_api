package io.github.burbokop.magura.models.vcontrol

import play.api.libs.json.Json

case class VersionControlRelease(
                          tagName: String,
                          name: Option[String]
                        )

object VersionControlRelease {
  implicit val format = Json.format[VersionControlRelease]
}