import xerial.sbt.Sonatype.GitHubHosting

import java.io.FileNotFoundException
import scala.io.Source

name := "magura_api"

version := "0.1.3"

scalaVersion := "2.13.7"

ThisBuild / organization := "io.github.burbokop"
ThisBuild / organizationName := "burbokop"
ThisBuild / organizationHomepage := Some(url("https://github.com/burbokop"))

publishTo := sonatypePublishToBundle.value
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"



sonatypeProfileName := organization.value
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

credentials += {
  try {
    val user = Source.fromFile(Path.userHome / ".magura_api" / "user").mkString.filterNot(_ == '\n')
    val pass = Source.fromFile(Path.userHome / ".magura_api" / "password").mkString.filterNot(_ == '\n')
    println(s"user: $user, pass: $pass")
    Credentials(
      "Sonatype Nexus Repository Manager",
      sonatypeCredentialHost.value,
      user,
      pass
    )
  } catch {
    case e: FileNotFoundException => {
      println(s"[warn] Maven publishing unavailable due to an exception: ${e.getMessage}")
      Credentials("", "", "", "")
    }
  }
}
