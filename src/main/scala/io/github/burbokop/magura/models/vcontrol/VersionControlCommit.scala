package io.github.burbokop.magura.models.vcontrol

import play.api.libs.json.Json

case class VersionControlCommit(
                       sha: String
                       )

object VersionControlCommit {
  implicit val format = Json.format[VersionControlCommit]
}