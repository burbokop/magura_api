import xerial.sbt.Sonatype.GitHubHosting

name := "magura_api"

version := "0.1"

scalaVersion := "2.13.7"

publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"



sonatypeProfileName := "io.github.burbokop"
publishMavenStyle := true
licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))
sonatypeProjectHosting := Some(GitHubHosting("burbokop", "magura_api", "burbokop@gmail.com"))
homepage := Some(url("https://github.com/burbokop/magura_api"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/burbokop/magura_api"),
    "scm:git@github.com:burbokop/magura_api.git"
  )
)

developers := List(
  Developer(id="burbokop", name="Borys Boiko", email="burbokop@gmail.com", url=url("https://github.com/burbokop"))
)