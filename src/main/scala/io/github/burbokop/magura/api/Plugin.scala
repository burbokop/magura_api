package io.github.burbokop.magura.api

import io.github.burbokop.magura.repository.RepositoryProvider


trait Plugin {
  def name(): String
  def repositoryProviders(): Map[String, RepositoryProvider]
  def generators(): Map[String, Generator]
  def generatorField(maguraFile: MaguraFile): String
  final def newDistributor(): GeneratorDistributor =
    new GeneratorDistributor(generators(), repositoryProviders(), generatorField)
}