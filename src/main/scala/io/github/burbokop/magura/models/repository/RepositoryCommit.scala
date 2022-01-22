package io.github.burbokop.magura.models.repository

import play.api.libs.json.Json

case class RepositoryCommit(hash: String)

object RepositoryCommit {
  implicit val format = Json.format[RepositoryCommit]
}