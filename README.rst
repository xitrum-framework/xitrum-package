This plugin adds task "xitrum-package" to your SBT project to collect all
dependency .jar files for standalone Scala programs. Compared to "one-jar"
solutions, the .jar files are left "as is".

Usage
-----

Add to project/plugins.sbt:

::

  addSbtPlugin("tv.cntt" % "xitrum-plugin" % "1.4")

Run:

::

  sbt xitrum-package

All dependency .jar files will be copied to target/xitrum/lib directory.
