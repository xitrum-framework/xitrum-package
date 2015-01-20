This plugin adds task ``xitrum-package`` to your `SBT <http://www.scala-sbt.org/>`_
project to collect all dependency .jar files for standalone Scala programs.

Compared to
`one-jar <http://www.scala-sbt.org/release/docs/Community/Community-Plugins#one-jar-plugins>`_
solutions, xitrum-package is faster. It doesn't merge the .jar files together,
the .jar files are left "as is".

xitrum-package is used in `Scala web framework Xitrum <http://xitrum-framework.github.io/>`_.

Alternatively, you can also use `sbt-pack <https://github.com/xerial/sbt-pack>`_.

Usage
-----

Supported SBT versions: 0.13.x

Suppose your project looks like this:

::

  build.sbt

  project/
    plugins.sbt

  src/
    ...

  dirToCopy/      <-- Directory you want to copy to the packaged directory
    file1
    file2

  fileToCopy      <-- File you want to copy to the packaged directory

Add xitrum-package to your project
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Add to ``project/plugins.sbt``:

::

  addSbtPlugin("tv.cntt" % "xitrum-package" % "1.9")

Add to build.sbt:

::

  XitrumPackage.copy("dirToCopy", "fileToCopy")

Do the packaging
~~~~~~~~~~~~~~~~

Run:

::

  sbt xitrum-package

All dependency .jar files and .jar files generated from your project will be
copied to directory ``target/xitrum``:

::

  target/
    xitrum/
      lib/        <-- Dependency .jar files are collected here
        dep1.jar
        dep2.jar
        yourProject.jar

      dirToCopy/  <-- The specified directory is copied here
        file1
        file2

      fileToCopy  <-- The specified file is copied here

Note that even when you don't need to copy anything, you have to write in
build.sbt:

::

  XitrumPackage.copy()

Multimodule project
-------------------

If your SBT project has
`many modules (subprojects) <http://www.scala-sbt.org/0.13.5/docs/Getting-Started/Multi-Project.html>`_
and you want to only ``xitrum-package`` several of them, you can use ``XitrumPackage.skip``:

::

  // Shared settings used by many modules
  val sharedSettings = ...

  // For the default root project
  override lazy val settings = super.settings ++ XitrumPackage.skip

  // If you want to skip this module
  lazy val module1 = Project(
    id = "module1",
    base = file("module1"),
    settings = sharedSettings ++ Seq(
      name := "module1"
    ) ++ XitrumPackage.skip
  )

  // If you want to xitrum-package this module
  lazy val app = Project(
    id = "main-app",
    base = file("main-app"),
    settings = sharedSettings ++ Seq(
      name := "main-app"
    ) ++ XitrumPackage.copy("bin", "config", "public")
  ).dependsOn(module1)

Example: https://github.com/xitrum-framework/xitrum-multimodule-demo

Boot script
-----------

You should create a shell script to start your Scala program like this:

* `runner.sh <https://github.com/xitrum-framework/xitrum-new/blob/master/script/runner>`_ (for *nix)
* `runner.bat <https://github.com/xitrum-framework/xitrum-new/blob/master/script/runner.bat>`_ (for Windows)

Run example:

::

  runner.sh my_package.MyMainClass
