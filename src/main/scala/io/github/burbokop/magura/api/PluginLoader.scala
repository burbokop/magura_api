package io.github.burbokop.magura.api

import com.googlecode.scalascriptengine.classloading.ClassLoaderConfig
import com.googlecode.scalascriptengine.{Config, ScalaScriptEngine, SourcePath}
import io.github.burbokop.magura.models.meta.RepositoryMetaData
import io.github.burbokop.magura.utils.FileUtils

import java.io.File

object PluginLoader {
  def defaultName = "plugin"
}

class PluginLoader extends Generator {
  def load(sourceDir: File): List[Plugin] = {
    val sourcePath = new SourcePath(FileUtils.recursiveListFiles(sourceDir).filter(_.getName.endsWith(".scala")).toSet)
    val cfg = Config(
      sourcePaths = List(sourcePath),
      classLoaderConfig = ClassLoaderConfig.Default.copy(enableClassRegistry = true)
    )
    val sse = ScalaScriptEngine.withoutRefreshPolicy(cfg, cfg.compilationClassPaths)
    sse.refresh
    sse.currentVersion.classLoader.withTypeOf[Plugin].map(_.newInstance)
  }

  override def proceed(
                        cache: List[RepositoryMetaData],
                        inputPath: String,
                        outputPath: String,
                        options: Generator.Options,
                        maguraFile: MaguraFile
                      ): Either[Throwable, Generator.Result] =
    Right(Generator.Result(false, load(new File(inputPath)).map(_.generators()).reduce(_ ++ _)))
}
