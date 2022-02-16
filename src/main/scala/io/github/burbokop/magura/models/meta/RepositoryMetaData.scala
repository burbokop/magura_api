package io.github.burbokop.magura.models.meta

import io.github.burbokop.magura.api.Generator.Options
import io.github.burbokop.magura.utils.FileUtils
import io.github.burbokop.magura.utils.JsonUtils.JsonParseException
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.io.{File, FileInputStream, FileOutputStream, InputStream}


object RepositoryMetaData {
  implicit val jsonFormat = Json.format[RepositoryMetaData]

  val empty = RepositoryMetaData("", Nil)

  def fromFolder(f: File, name: String, maxLevel: Int = Int.MaxValue): List[RepositoryMetaData] =
    FileUtils.recursiveListFiles(f, maxLevel).filter(item => item.isFile && item.getName == name).map { item =>
      fromJsonFile(item).toOption
    }
      .filter(_.isDefined)
      .map(_.get)
      .toList

  def fromJsonStream(inputStream: InputStream): Either[Throwable, RepositoryMetaData] =
    Json.parse(inputStream).validate[RepositoryMetaData] match {
      case s: JsSuccess[RepositoryMetaData] => Right(s.get)
      case jsError: JsError => Left(JsonParseException(jsError.toString, jsError, inputStream.toString))
    }

  def fromJsonFile(path: String): Either[Throwable, RepositoryMetaData] =
    try {
      fromJsonStream(new FileInputStream(path))
    } catch {
      case e: Throwable => Left(e)
    }

  def fromJsonFile(file: File): Either[Throwable, RepositoryMetaData] =
    fromJsonFile(file.getPath)

  /**
   * @param path
   * @param default
   * @return default if file not found. else either
   */
  def fromJsonFileDefault(path: String, default: RepositoryMetaData = empty): Either[Throwable, RepositoryMetaData] =
    fromJsonFile(path)
      .fold[Either[Throwable, RepositoryMetaData]]({
        case _: java.io.FileNotFoundException => Right(default)
        case other => Left(other)
      }, Right(_))

  def fromJsonFileDefault(file: File): Either[Throwable, RepositoryMetaData] =
    fromJsonFileDefault(file.getPath)

}

case class RepositoryMetaData(
                               currentCommit: String,
                               versions: List[RepositoryVersion]
                             ) {
  def toJson(pretty: Boolean = false): String =
    if (pretty) Json.prettyPrint(Json.toJson(this))
    else Json.stringify(Json.toJson(this))

  def writeJsonToFile(path: String, pretty: Boolean = false): Either[Throwable, RepositoryMetaData] =
    try {
      new File(path).getParentFile.mkdirs()
      new FileOutputStream(path).write(toJson(pretty).toArray.map(_.toByte))
      Right(this)
    } catch {
      case e: Throwable => Left(e)
    }

  def latestVersion(): Option[RepositoryVersion] =
    versions.find(_.commit == currentCommit)

  def withVersion(version: RepositoryVersion): RepositoryMetaData =
    RepositoryMetaData(version.commit, this.versions :+ version)

  def withBuildPaths(commit: String, buildPaths: Map[String, Options]): RepositoryMetaData =
    RepositoryMetaData(currentCommit, versions.map { version => if(version.commit == commit) version.withBuildPaths(buildPaths) else version })
}
