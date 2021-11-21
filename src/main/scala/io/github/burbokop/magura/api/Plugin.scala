package io.github.burbokop.magura.api

import java.util.ServiceLoader
import scala.jdk.CollectionConverters.{
  CollectionHasAsScala,
  IteratorHasAsScala,
  IterableHasAsJava
}

import java.lang.module.ModuleFinder
import java.nio.file.Paths


object Plugin {
  def load(layer: ModuleLayer): List[Plugin] =
    ServiceLoader
      .load(layer, classOf[Plugin])
      .iterator
      .asScala
      .toList


  def loadFromPath(dir: String): List[Plugin] = {
    val moduleFinder = ModuleFinder.of(Paths.get(dir))
    val plugins = moduleFinder.findAll().asScala.map(_.descriptor.name)
    val pluginsConfiguration = ModuleLayer.boot.configuration.resolve(
      moduleFinder,
      ModuleFinder.of(),
      plugins.asJavaCollection
    )

    load(
      ModuleLayer
        .boot
        .defineModulesWithOneLoader(pluginsConfiguration, ClassLoader.getSystemClassLoader)
    )
  }
}

trait Plugin {
  def init(): Int
  def name(): String
}