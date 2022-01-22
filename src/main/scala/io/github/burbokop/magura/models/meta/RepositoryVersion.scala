package io.github.burbokop.magura.models.meta

import io.github.burbokop.magura.api.Generator.{DefaultOptions, Options}
import play.api.libs.json.Json

case class RepositoryVersion(
                              commit: String,
                              entry: String,
                              entryPath: String,
                              activeBuildPath: Option[String],
                              buildPaths: Map[String, Options],
                              builder: String
                            ) {
  def defaultBuildPath() =
    buildPaths
      .find(arg => arg._2.isInstanceOf[DefaultOptions])
      .map(_._1)
      .orElse(buildPaths.headOption.map(_._1))

  def withBuildPaths(buildPaths: Map[String, Options]) =
    RepositoryVersion(
      commit,
      entry,
      entryPath,
      buildPaths.lastOption.map(_._1).orElse(activeBuildPath),
      this.buildPaths ++ buildPaths,
      builder
    )
}

object RepositoryVersion {
  implicit val format = Json.format[RepositoryVersion]
}