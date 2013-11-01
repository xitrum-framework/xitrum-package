import java.io.File

import sbt._
import Keys._

// See https://github.com/sbt/sbt/issues/944
// Add option preserveExecutable to IO.copyFile and IO.copyDirectory
object PreserveExecutableIO {
  // http://stackoverflow.com/questions/5368724
  def copy(source: File, target: File) {
    if (source.isDirectory) {
      if (!target.exists) target.mkdir()

      source.list.foreach { child =>
        copy(new File(source, child), new File(target, child))
      }
    } else {
      IO.copyFile(source, target)
      if (source.canExecute) target.setExecutable(true)
    }
  }
}

object XitrumPackage extends Plugin {
  // Must be lazy to avoid null error
  // xitrumPackageNeedsPackageBin must be after xitrumPackageTask
  override lazy val settings = Seq(xitrumPackageTask, xitrumPackageNeedsPackageBin)

  val skipKey = SettingKey[Boolean]("xitrum-skip", "Do not package the current project (useful when you use SBT multiproject feature)")

  val copiesKey = SettingKey[Seq[String]]("xitrum-copies", "List of files and directories to copy")

  val skip = Seq(skipKey := true, copiesKey := Seq())

  def copy(fileNames: String*) = Seq(skipKey := false, copiesKey := fileNames)

  //----------------------------------------------------------------------------

  val xitrumPackageKey = TaskKey[Unit]("xitrum-package", "Packages to target/xitrum directory, ready for deploying to production server")

  // Must be lazy to avoid null error
  lazy val xitrumPackageTask = xitrumPackageKey <<=
    // dependencyClasspath: both internalDependencyClasspath and externalDependencyClasspath
    // internalDependencyClasspath ex: classes directories
    // externalDependencyClasspath ex: .jar files
    (dependencyClasspath in Runtime, baseDirectory, target,    crossTarget,  skipKey, copiesKey) map {
    (libs,                           baseDir,       targetDir, jarOutputDir, skip,    copyFileNames) =>
    try {
      if (!skip) doPackage(libs, baseDir, targetDir, jarOutputDir, copyFileNames)
    } catch {
      case e: Exception =>
        println("xitrum-package failed, reason:")
        e.printStackTrace()
    }
  }

  val xitrumPackageNeedsPackageBin = xitrumPackageKey <<= xitrumPackageKey.dependsOn(packageBin in Compile)

  //----------------------------------------------------------------------------

  private def doPackage(libs: Seq[Attributed[File]], baseDir: File, targetDir: File, jarOutputDir: File, copyFileNames: Seq[String]) {
    val packageDir = targetDir / "xitrum"
    deleteFileOrDirectory(packageDir)
    packageDir.mkdirs()

    // Copy dependencies to lib directory
    val libDir = packageDir / "lib"
    libs.foreach { lib =>
      val file = lib.data

      if (file.exists) {
        if (file.isDirectory) {
          // This dependency may be "classes" directory from SBT multimodule (multiproject)
          // http://www.scala-sbt.org/0.13.0/docs/Getting-Started/Multi-Project.html
          //
          // Ex:
          // /Users/ngoc/src/xitrum-multimodule-demo/module1/target/scala-2.10/classes
          // /Users/ngoc/src/xitrum-multimodule-demo/module1/target/scala-2.10/xitrum-multimodule-demo-module1_2.10-1.0-SNAPSHOT.jar
          if (file.name == "classes") {
            val upperDir = file / ".."
            (upperDir * "*.jar").get.foreach { f =>
              val fname = f.name
              if (!fname.endsWith("-sources.jar") && !fname.endsWith("-javadoc.jar"))
                IO.copyFile(f, libDir / f.name)
            }
          }
        } else {
          IO.copyFile(file, libDir / file.name)
        }
      }
    }

    // Copy .jar files created after running "sbt package" to lib directory
    // (see xitrumPackageNeedsPackageBin)
    (jarOutputDir * "*.jar").get.foreach { f => IO.copyFile(f, libDir / f.name) }

    copyFileNames.foreach { fName => doCopy(fName, baseDir, packageDir) }

    println("Packaged to " + packageDir)
  }

  private def deleteFileOrDirectory(file: File) {
    if (file.isDirectory) {
      val files = file.listFiles
      if (files != null) files.foreach { f => deleteFileOrDirectory(f) }
    }
    file.delete()
  }

  private def doCopy(fileName: String, baseDir: File, packageDir: File) {
    val from = baseDir / fileName
    if (!from.exists) return

    val to = packageDir / fileName
    PreserveExecutableIO.copy(from, to)
  }
}
