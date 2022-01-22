package io.github.burbokop.magura.utils

import java.io.{File, FileInputStream, FileNotFoundException, FileOutputStream, IOException}
import java.nio
import scala.annotation.tailrec
import scala.io.Source

object FileUtils {
  implicit def / = File.separator

  implicit class RichFile(val file : java.io.File) {
    def /(component: String): java.io.File =
      new File(s"${file.getPath}${FileUtils./}$component")
  }

    def pwd(): String = new java.io.File(".").getCanonicalPath

  @tailrec
  def normalizeFolder(folder: File): File =
    if (folder.isDirectory) folder
    else normalizeFolder(folder.getParentFile)

  def normalizeFolder(folder: String): String =
    normalizeFolder(new File(folder)).getAbsolutePath

  def recursiveListFiles(f: File, maxLevel: Int = Int.MaxValue): Array[File] = {
    if (maxLevel > 0) {
      val these = f.listFiles
      these ++ these.filter(_.isDirectory).flatMap(item => recursiveListFiles(item, maxLevel - 1))
    } else {
      Array()
    }
  }

  def copyFile(src: String, dest: String): Either[Throwable, Unit] = {
    try {
      val destDir = new File(dest).getParentFile
      if (!destDir.exists()) {
        destDir.mkdirs()
      }
      val inputChannel = new FileInputStream(src).getChannel
      val outputChannel = new FileOutputStream(dest).getChannel
      outputChannel.transferFrom(inputChannel, 0, inputChannel.size)
      inputChannel.close()
      Right()
    } catch {
      case e: IOException => Left(e)
    }
  }

  def recursiveCopyDirectory(src: String, dest: String): Either[Throwable, Unit] =
    try {
      recursiveListFiles(new File(src)).map { currentFile =>
        if(currentFile.isFile) {
          val sourcePath = nio.file.Paths.get(currentFile.getAbsolutePath)
          val destinationPath = nio.file.Paths.get(dest).resolve(nio.file.Paths.get(src).relativize(currentFile.toPath))
          try {
            nio.file.Files.createDirectories(destinationPath.getParent)
          } catch {
            case _: IOException =>
          }
          try {
            nio.file.Files.copy(
              sourcePath,
              destinationPath,
              nio.file.StandardCopyOption.COPY_ATTRIBUTES
            )
          } catch {
            case _: nio.file.FileAlreadyExistsException => Right()
          }
        }
      }
      Right()
    } catch {
      case e: IOException => Left(e)
    }

  def writeIfDifferent(path: String, data: String): Either[Throwable, Boolean] = {
    try {
      if (try {
        Source.fromFile(path).mkString != data
      } catch {
        case _: FileNotFoundException => true
      }) {
        new File(path).getParentFile.mkdirs()
        new FileOutputStream(path).write(data.toArray.map(_.toByte))
        Right(true)
      } else {
        Right(false)
      }
    } catch {
      case e: IOException => Left(e)
    }
  }

  def writeIfNotExist(path: String, data: String): Either[Throwable, Boolean] = {
    try {
      if (!new File(path).exists()) {
        new FileOutputStream(path).write(data.toArray.map(_.toByte))
        Right(true)
      } else {
        Right(false)
      }
    } catch {
      case e: IOException => Left(e)
    }
  }
}
