package io.github.burbokop.magura.repository

import io.github.burbokop.magura.models.repository.{RepositoryBranch, RepositoryRelease}
import io.github.burbokop.magura.utils.ZipUtils

import java.io.{ByteArrayInputStream, File}

trait RepositoryProvider {
  def defaultBranchName(): String
  def branch(user: String, repo: String, branch: String): Either[Throwable, RepositoryBranch]
  def repositoryReleases(user: String, repo: String): Either[Throwable, List[RepositoryRelease]]

  def downloadZip(user: String, repo: String, branch: String): Either[Throwable, Array[Byte]]

  def download(user: String, repo: String, branch: String, destinationFolder: File): Either[Throwable, String] =
    downloadZip(user, repo, branch)
      .fold(Left(_), data => ZipUtils.unzipToFolder(new ByteArrayInputStream(data), destinationFolder.getAbsolutePath))

}
