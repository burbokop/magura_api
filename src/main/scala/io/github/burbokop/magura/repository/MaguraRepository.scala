package io.github.burbokop.magura.repository

import io.github.burbokop.magura.api.Generator.{DefaultOptions, Options, repositoryName}
import io.github.burbokop.magura.api.{GeneratorDistributor, MaguraFile}
import io.github.burbokop.magura.models.meta.{RepositoryMetaData, RepositoryVersion}
import io.github.burbokop.magura.utils.ZipUtils
import io.github.burbokop.magura.utils.FileUtils./

import java.io.File
import scala.annotation.tailrec

case class MaguraRepository(
                             provider: String,
                             user: String,
                             name: String,
                             version: Option[String],
                             builder: Option[String]
                           )

object MaguraRepository {
  case class Error(message: String) extends Exception(message)
  case class UndefinedProvider(provider: String) extends Exception(s"undefined provider: $provider")

  def fromString(string: String): Either[Throwable, MaguraRepository] = {
    val error = MaguraRepository.Error(s"repo should be {provider}:{user}.{repo}.{version | commit | branch (optional)}:{builder (optional)} but got '$string'")
    val parts = string.split(':')
    if(parts.length > 1) {
      val provider = parts(0)
      val repo = parts(1)
      val builder = if (parts.length == 3) Some(parts(2)) else None
      val repoParts = repo.split('.')
      if(repoParts.length > 1) {
        val user = repoParts(0)
        val repoName = repoParts(1)
        val commitOrBranch = if (repoParts.length == 3) Some(repoParts(2)) else None
        Right(MaguraRepository(provider, user, repoName, commitOrBranch, builder))
      } else Left(error)
    } else Left(error)
  }

  val metaFileName = "meta.json"


  def get(
           builderDistributor: GeneratorDistributor,
           repository: MaguraRepository,
           cacheFolder: String,
           optionsSet: Set[Options] = Set(new DefaultOptions())
         ): (GeneratorDistributor, Either[Throwable, RepositoryMetaData]) = {
    val repoFolder = new File(s"$cacheFolder${/}${repository.user}${/}${repository.name}")
    val metaFile = s"$repoFolder${/}$metaFileName"

    def genEntryFolder(repoEntry: String) = s"$repoFolder${/}$repoEntry"
    def genBuildFolder(repoEntry: String, options: Options) = s"$repoFolder${/}build_${options.hashName()}_$repoEntry"


    val branchResult = builderDistributor.repositoryProvider(repository.provider)
      .map(repoProvider => {
        val version = repository.version.getOrElse(repoProvider.defaultBranchName())

        repoProvider.commitOrHead(repository.user, repository.name, version).fold(
          err => (builderDistributor, Left(err)),
          desiredCommit => {
            val meta = RepositoryMetaData.fromJsonFileDefault(metaFile)
            if(meta.currentCommit != desiredCommit.hash) {
              repoProvider.download(repository.user, repository.name, version, repoFolder)
                .fold(err => (builderDistributor, Left(err)), repoEntry => {
                  val entryFolder = genEntryFolder(repoEntry)
                  val buildPaths: Map[String, Options] =
                    optionsSet.map(options => (genBuildFolder(repoEntry, options), options)).toMap

                  println(s"optionsSet: $optionsSet")
                  println(s"entryFolder: $entryFolder")
                  println(s"buildPaths: $buildPaths")

                  builderDistributor
                    .proceed(
                      RepositoryMetaData.fromFolder(new File(cacheFolder), metaFileName, 3),
                      entryFolder,
                      buildPaths,
                      repository.builder.map(MaguraFile.fromBuilder)
                    )
                    .fold[(GeneratorDistributor, Either[Throwable, RepositoryMetaData])](
                      err => (builderDistributor, Left(err)),
                      newGeneratorDistributor => {
                        newGeneratorDistributor.lastGeneration.map { generation =>
                          if(generation.changed) {
                            (newGeneratorDistributor, meta.withVersion(RepositoryVersion(
                              desiredCommit.hash,
                              repoEntry,
                              entryFolder,
                              buildPaths.lastOption.map(_._1),
                              buildPaths,
                              generation.generatorName
                            )).writeJsonToFile(metaFile, pretty = true))
                          } else {
                            (newGeneratorDistributor, Right(meta))
                          }
                        } getOrElse {
                          (builderDistributor, Right(meta))
                        }
                      })
                })
            } else (builderDistributor, Right(meta))
          })
      }).getOrElse((builderDistributor, Left(UndefinedProvider(repository.provider))))



    branchResult._2.map(meta => {
      //@tailrec
      def iterator(currentDistributor: GeneratorDistributor, versions: List[RepositoryVersion], acc: RepositoryMetaData): (GeneratorDistributor, Either[Throwable, RepositoryMetaData]) = {
        val res = versions.headOption.map({ version =>
          val buildPaths: Map[String, Options] = {
            println(s"optionsSet: $optionsSet diff verOpts: ${version.buildPaths.values.toSet} = ${optionsSet.diff(version.buildPaths.values.toSet)}")

            optionsSet
              .diff(version.buildPaths.values.toSet)
              .map(options => (genBuildFolder(version.entry, options), options))
              .toMap
          }

          (currentDistributor
            .proceed(
              RepositoryMetaData.fromFolder(new File(cacheFolder), metaFileName, 3),
              version.entryPath,
              buildPaths,
              repository.builder.map(MaguraFile.fromBuilder)
            ).fold[(GeneratorDistributor, Either[Throwable, RepositoryMetaData])](err => (currentDistributor, Left(err)), newGeneratorDistributor => {
            newGeneratorDistributor.lastGeneration.map { generation =>
              if (generation.changed) {
                (newGeneratorDistributor, meta
                  .withBuildPaths(version.commit, buildPaths)
                  .writeJsonToFile(metaFile, true))
              } else (newGeneratorDistributor, Right(meta))
            } getOrElse {
              (newGeneratorDistributor, Right(meta))
            }
          }) match {
            case (distro: GeneratorDistributor, meta: Either[Throwable, RepositoryMetaData]) => meta.fold(err => (distro, Left(err)), iterator(distro, versions.tail, _))
          })
        })
          .getOrElse((currentDistributor, Right(acc)))
        println(s"iter versions: $versions, acc: $acc -> $res")
        res
      }

      iterator(branchResult._1, meta.versions, meta)
    }).fold(err => (branchResult._1, Left(err)), v => v)
  }

  def get(
           builderDistributor: GeneratorDistributor,
           repos: List[MaguraRepository],
           cacheFolder: String,
         ): (GeneratorDistributor, Either[Throwable, List[RepositoryMetaData]]) = {
    @tailrec
    def iterator(currentDistributor: GeneratorDistributor, repos: List[MaguraRepository], acc: List[RepositoryMetaData]): (GeneratorDistributor, Either[Throwable, List[RepositoryMetaData]]) = {
      if(repos.length > 0) {
        val getRes = MaguraRepository.get(builderDistributor, repos.head, cacheFolder)
        getRes._2 match {
          case Left(value) => (getRes._1, Left(value))
          case Right(value) => iterator(getRes._1, repos.tail, acc :+ value)
        }
      } else {
        (currentDistributor, Right(acc))
      }
    }
    iterator(builderDistributor, repos, List())
  }
}
