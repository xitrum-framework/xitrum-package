This plugin adds task ``xitrum-package`` to your `SBT <http://www.scala-sbt.org/>`_
project to collect all dependency .jar files for standalone Scala programs. Compared to
`one-jar <http://www.scala-sbt.org/release/docs/Community/Community-Plugins#one-jar-plugins>`_
solutions, the .jar files are left "as is".

Supported SBT versions: 0.13, 0.12 (SBT 0.12 is supported up to xitrum-sbt-plugin 1.4)

xitrum-sbt-plugin is used in `Scala web framework Xitrum <http://ngocdaothanh.github.io/xitrum/>`_.

Usage
-----

Add to project/plugins.sbt:

::

  addSbtPlugin("tv.cntt" % "xitrum-plugin" % "1.5")

Add to build.sbt:

::

  XitrumPlugin.copy("dirToCopy", "fileToCopy")

Run:

::

  sbt xitrum-package

All dependency .jar files and .jar files generated from your project
will be copied to ``target/xitrum`` directory.

::

  project/
    plugins.sbt

  src/
    ...

  dirToCopy/      <-- Directory to copy
    file1
    file2

  fileToCopy      <-- File to copy

  target/
    xitrum/
      lib/        <-- .jar files will be collected here
        dep1.jar
        dep2.jar
        project.jar

      dirToCopy/  <-- Directory will be copied here
        file1
        file2

      fileToCopy  <-- File will be copied here

Note that even when you don't need to copy anything, you have to write:

::

  XitrumPlugin.copy()

Multiple-module project
-----------------------

If your SBT project has
`many modules (subprojects) <http://www.scala-sbt.org/0.13.0/docs/Getting-Started/Multi-Project.html>`_
and you only want to ``xitrum-package`` several of them, you can skip the
subproject you want using ``XitrumPlugin.skip``:

::

  val sharedSettings = ...

  lazy val module1 = Project(
    id = "module1",
    base = file("module1"),
    settings = sharedSettings ++ Seq(
      name := "module1"
    ) ++ XitrumPlugin.skip
  )

  lazy val app = Project(
    id = "main-app",
    base = file("main-app"),
    settings = sharedSettings ++ Seq(
      name := "main-app"
    ) ++ XitrumPlugin.copy("bin", "config", "public")
  ).dependsOn(module1)

Example: https://github.com/ngocdaothanh/xitrum-multimodule-demo

Boot script
-----------

You should create a shell script to start your Scala program like this:

* `runner.sh <https://github.com/ngocdaothanh/xitrum-new/blob/master/bin/runner>`_ (for *nix)
* `runner.bat <https://github.com/ngocdaothanh/xitrum-new/blob/master/bin/runner.bat>`_ (for Windows)

Run example:

::

  runner.sh my_package.MyMainClass
