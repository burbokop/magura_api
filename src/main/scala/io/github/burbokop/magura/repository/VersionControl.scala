package io.github.burbokop.magura.repository

import io.github.burbokop.magura.models.vcontrol.{VersionControlBranch, VersionControlRelease}

trait VersionControl {
  def getBranch(user: String, repo: String, branch: String): Either[Throwable, VersionControlBranch]
  def downloadRepositoryZip(user: String, repo: String, ref: String): Either[Throwable, Array[Byte]]
  def getRepositoryReleases(user: String, repo: String): Either[Throwable, List[VersionControlRelease]]
}
