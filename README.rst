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

  addSbtPlugin("tv.cntt" % "xitrum-plugin" % "1.4")

Run:

::

  sbt xitrum-package

All dependency .jar files and .jar files generated from your project
will be copied to ``target/xitrum/lib`` directory.

Boot script
-----------

You should create a shell script to start your Scala program like this:

* `runner.sh <https://github.com/ngocdaothanh/xitrum-new/blob/master/bin/runner>`_ (for *nix)
* `runner.bat <https://github.com/ngocdaothanh/xitrum-new/blob/master/bin/runner.bat>`_ (for Windows)

Run example:

::

  runner.sh my_package.MyMainClass
