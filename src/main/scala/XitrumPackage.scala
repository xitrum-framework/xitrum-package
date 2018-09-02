import sbt._
import Keys._

object CopyUtil {
  def copy(source: File, target: File) {
    // http://stackoverflow.com/questions/5368724
    if (source.isDirectory) {
      // Skip if source is a symlink that points to its ancestor directory:
      // https://github.com/xitrum-framework/xitrum-package/issues/12
      val abs    = source.getAbsolutePath
      val canAbs = source.getCanonicalFile.getCanonicalPath
      if (abs != canAbs && abs.startsWith(canAbs)) {
        println(s"Skip: $abs -> $canAbs")
        return
      }

      if (!target.exists) target.mkdir()

      source.list.foreach { child =>
        copy(new File(source, child), new File(target, child))
      }
    } else {
      IO.copyFile(source, target)
    }
  }
}

object XitrumPackage extends AutoPlugin {
  override def trigger = allRequirements

  // - Must be lazy to avoid null error
  // - xitrumPackageNeedsPackageBin must be after xitrumPackageTask
  override lazy val projectSettings = Seq(xitrumPackageTask, xitrumPackageNeedsPackageBin, projectJarsTask)

  val skipKey = SettingKey[Boolean](
    "xitrumSkip",
     "Do not package the current project (useful when you use SBT multiproject feature)"
  )

  val copiesKey = SettingKey[Seq[String]](
    "xitrumCopies",
    "List of files and directories to copy"
  )

  val skip = Seq(skipKey := true, copiesKey := Seq())

  def copy(fileNames: String*) = Seq(skipKey := false, copiesKey := fileNames)

  //----------------------------------------------------------------------------

  val xitrumPackageKey = TaskKey[Unit](
    "xitrumPackage",
    "Packages to target/xitrum directory, ready for deploying to production server",
    KeyRanks.ATask  // This task is listed with command "sbt tasks"
  )

  // Must be lazy to avoid null error
  lazy val xitrumPackageTask = xitrumPackageKey := {
    // dependencyClasspath: both internalDependencyClasspath and externalDependencyClasspath
    // internalDependencyClasspath ex: classes directories
    // externalDependencyClasspath ex: .jar files
    try {
      val skip = skipKey.value
      val libs = (dependencyClasspath in Runtime).value
      val projectJars = projectJarsKey.value
      val baseDir = baseDirectory.value
      val targetDir = target.value
      val jarOutputDir = crossTarget.value
      val copyFileNames = copiesKey.value
      if (!skip) doPackage(libs, projectJars, baseDir, targetDir, jarOutputDir, copyFileNames)
    } catch {
      case e: Exception =>
        println("xitrumPackage failed, reason:")
        e.printStackTrace()
    }
  }

  lazy val xitrumPackageNeedsPackageBin = (
    xitrumPackageKey := xitrumPackageKey.dependsOn(packageBin in Compile).value
  )

  //----------------------------------------------------------------------------

  private def doPackage(
    libs:          Seq[Attributed[File]],
    projectJars:   Seq[File],
    baseDir:       File,
    targetDir:     File,
    jarOutputDir:  File,
    copyFileNames: Seq[String]
  ) {
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
          // http://www.scala-sbt.org/0.13.5/docs/Getting-Started/Multi-Project.html
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

    // Copy .jar files of dependency modules/projects
    projectJars.foreach { f => IO.copyFile(f, libDir / f.name) }

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
    val file = new File(fileName)
    val from = if (file.isAbsolute) file else baseDir / fileName
    if (!from.exists) return

    val to = packageDir / file.name
    CopyUtil.copy(from, to)
  }

  // https://github.com/xerial/sbt-pack/blob/master/src/main/scala/xerial/sbt/pack/PackPlugin.scala

  val projectJarsKey = TaskKey[Seq[File]]("xitrumProjectJars")

  lazy val projectJarsTask = projectJarsKey := {
    Def.taskDyn {
      val libJars = getFromSelectedProjects(thisProjectRef.value, packageBin in Runtime, state.value)
      Def.task { libJars.value.map(_._1) }
    }.value
  }

  private def getFromSelectedProjects[T](contextProject: ProjectRef, targetTask: TaskKey[T], state: State): Task[Seq[(T, ProjectRef)]] = {
    val extracted = Project.extract(state)
    val structure = extracted.structure

    def transitiveDependencies(currentProject: ProjectRef): Seq[ProjectRef] = {
      // Traverse all dependent projects
      val children = Project
              .getProject(currentProject, structure)
              .toSeq
              .flatMap{ _.dependencies.map(_.project) }

      currentProject +: (children flatMap (transitiveDependencies(_)))
    }
    val projects = transitiveDependencies(contextProject).distinct
    projects.map(p => (Def.task { ((targetTask in p).value, p) }) evaluate structure.data).join
  }
}
