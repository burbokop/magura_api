package io.github.burbokop.magura.api

import io.github.burbokop.magura.api.Generator.Options
import io.github.burbokop.magura.api.GeneratorDistributor.Generation
import io.github.burbokop.magura.models.meta.RepositoryMetaData
import io.github.burbokop.magura.utils.EitherUtils.ThrowableListImplicits._
import io.github.burbokop.magura.models.meta.RepositoryMetaData

import java.io.File


object GeneratorDistributor{
  val maguraFileName = "magura.yaml"

  case class Generation(generatorName: String, changed: Boolean)
}

case class GeneratorDistributor(
                            generators: Map[String, Generator],
                            generatorField: MaguraFile => String,
                            lastGeneration: Option[Generation] = None,
                          ) {
  private def proceedWithMaguraFile(
                                     cache: List[RepositoryMetaData],
                                     inputFolder: String,
                                     outputWithOptions: Map[String, Options],
                                     maguraFile: MaguraFile,
  ): Either[Throwable, GeneratorDistributor] = {
    val generatorName = generatorField(maguraFile)
    generators.get(generatorName).map { generator =>
      outputWithOptions.map(arg => { val (path, options) = arg
        generator.proceed(cache, inputFolder, path, options, maguraFile)
      })
        .toList
        .reducedPartitionEither
        .map({ list =>
          new GeneratorDistributor(
            generators ++ list.map(_.modifications).reduce(_ ++ _),
            generatorField,
            Some(Generation(generatorName, list.find(_.changed).isDefined))
          )
        })
    } getOrElse {
      Left(Generator.Error(s"generator $generatorName not found"))
    }
  }

  def proceed(
               cache: List[RepositoryMetaData],
               inputFolder: String,
               outputWithOptions: Map[String, Options],
               maguraFile: Option[MaguraFile] = None
             ): Either[Throwable, GeneratorDistributor] =
    maguraFile.map { maguraFile =>
      proceedWithMaguraFile(cache, inputFolder, outputWithOptions, maguraFile)
    } getOrElse {
      MaguraFile.fromYaml(s"$inputFolder${File.separator}${GeneratorDistributor.maguraFileName}")
        .fold[Either[Throwable, GeneratorDistributor]](Left(_), { maguraFile =>
          proceedWithMaguraFile(cache, inputFolder, outputWithOptions, maguraFile)
        })
    }

  def withGenerator(name: String, generator: Generator) = GeneratorDistributor(generators ++ Map(name -> generator), generatorField)
}
