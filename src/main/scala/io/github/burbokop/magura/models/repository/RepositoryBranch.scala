package io.github.burbokop.magura.models.repository

import play.api.libs.json.Json

case class RepositoryBranch(
                         commit: RepositoryCommit
                       )

object RepositoryBranch {
  implicit val format = Json.format[RepositoryBranch]
}