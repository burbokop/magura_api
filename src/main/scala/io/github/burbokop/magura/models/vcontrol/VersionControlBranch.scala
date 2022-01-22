package io.github.burbokop.magura.models.vcontrol

import play.api.libs.json.Json

case class VersionControlBranch(
                         commit: VersionControlCommit
                       )

object VersionControlBranch {
  implicit val format = Json.format[VersionControlBranch]
}