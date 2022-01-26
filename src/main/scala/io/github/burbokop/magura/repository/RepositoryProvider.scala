package io.github.burbokop.magura.repository

import io.github.burbokop.magura.models.repository.{RepositoryBranch, RepositoryCommit, RepositoryRelease}
import io.github.burbokop.magura.utils.{ReducedError, ZipUtils}

import java.io.{ByteArrayInputStream, File}

trait RepositoryProvider {
  def defaultBranchName(): String
  def branch(user: String, repo: String, branch: String): Either[Throwable, RepositoryBranch]
  def commit(user: String, repo: String, hash: String): Either[Throwable, RepositoryCommit]
  def repositoryReleases(user: String, repo: String): Either[Throwable, List[RepositoryRelease]]
  def downloadZip(user: String, repo: String, commitHash: String): Either[Throwable, Array[Byte]]
  def download(user: String, repo: String, commitHash: String, destinationFolder: File): Either[Throwable, String] =
    downloadZip(user, repo, commitHash)
      .fold(Left(_), data => ZipUtils.unzipToFolder(new ByteArrayInputStream(data), destinationFolder.getAbsolutePath))

  final def commitOrHead(user: String, repo: String, commitOrBranch: String): Either[Throwable, RepositoryCommit] =
    commit(user, repo, commitOrBranch)
      .fold(err0 => branch(user, repo, commitOrBranch)
        .fold(
          err1 => Left(ReducedError(err0 +: err1 +: Nil)),
          branch => Right(branch.head)
        ), Right(_)
      )
}
