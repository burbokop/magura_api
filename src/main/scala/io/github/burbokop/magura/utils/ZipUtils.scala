package io.github.burbokop.magura.utils

import java.io.{ByteArrayInputStream, File, FileOutputStream, IOException, InputStream}
import java.util.zip.{ZipEntry, ZipInputStream}
import scala.annotation.tailrec

object ZipUtils {
  case class Error(message: String) extends Exception(message)

  def unzipToFolder(inputStream: InputStream, outputPath: String): Either[Error, String] = try {
    @tailrec
    def iterator(zipEntry: Option[ZipEntry], zipStream: ZipInputStream, buffer: Array[Byte]): Unit = {
      if (zipEntry.isDefined) {
        val currentPath = outputPath + File.separator + zipEntry.get.getName
        if (zipEntry.get.isDirectory) {
          new File(currentPath).mkdirs();
        } else {
          val currentFile = new File(currentPath);
          new File(currentFile.getParent()).mkdirs();
          val fos = new FileOutputStream(currentFile);
          val len: Int = zipStream.read(buffer);
          @tailrec
          def nestedIterator(len: Int): Unit = {
            if (len > 0) {
              fos.write(buffer, 0, len)
              nestedIterator(zipStream.read(buffer))
            }
          }
          nestedIterator(len)
          fos.close()
        }
        iterator(Option(zipStream.getNextEntry), zipStream, buffer)
      }
    }

    val outputFolder = new File(outputPath);
    if (!outputFolder.exists()) {
      outputFolder.mkdirs();
    }
    val zipStream = new ZipInputStream(inputStream);
    val buffer = new Array[Byte](1024)
    Option(zipStream.getNextEntry()).map { mainEntry =>
      iterator(Some(mainEntry), zipStream, buffer)
      zipStream.closeEntry()
      zipStream.close()
      Right(mainEntry.getName)
    } getOrElse {
      Left(Error("empty archive"))
    }
  } catch {
    case e: IOException => Left(Error(s"IO exception: ${e.getMessage}"))
  }
}
