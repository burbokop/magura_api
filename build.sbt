import java.io.FileNotFoundException
import scala.io.Source

name := "magura_api"

version := "0.1"

scalaVersion := "2.13.7"



publishTo := Some("Sonatype Snapshots Nexus" at "https://oss.sonatype.org/content/repositories/snapshots")


credentials += {
  try {
    Credentials(
      "Sonatype Nexus Repository Manager",
      "io.github.burbokop.magura.api",
      Source.fromFile(Path.userHome / ".magura_api" / "user").mkString,
      Source.fromFile(Path.userHome / ".magura_api" / "password").mkString
    )
  } catch {
    case e: FileNotFoundException => {
      println(s"[warn] Maven publishing unavailable due to an exception: ${e.getMessage}")
      Credentials("", "", "", "")
    }
  }
}


