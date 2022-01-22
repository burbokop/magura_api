package io.github.burbokop.magura.api

import io.github.burbokop.magura.repository.RepositoryProvider


trait Plugin {
  def name(): String
  def repositoryProviders(): Map[String, RepositoryProvider]
  def generators(): Map[String, Generator]
}