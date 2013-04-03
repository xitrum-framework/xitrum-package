This plugin adds task "xitrum-package" to your `SBT <http://www.scala-sbt.org/>`_ project to collect all
dependency .jar files for standalone Scala programs. Compared to "one-jar"
solutions, the .jar files are left "as is".

Compatible SBT versions: 0.12

Usage
-----

Add to project/plugins.sbt:

::

  addSbtPlugin("tv.cntt" % "xitrum-plugin" % "1.4")

Run:

::

  sbt xitrum-package

All dependency .jar files and .jar files generated from your project
will be copied to target/xitrum/lib directory.

start.sh
--------

You should create a shell script to start your Scala program like this:

::

  #!/bin/sh

  # You may need to customize memory config below to optimize for your environment
  JAVA_OPTS='-Xmx1024m -Xms256m -XX:MaxPermSize=128m -XX:+HeapDumpOnOutOfMemoryError -XX:+AggressiveOpts -XX:+OptimizeStringConcat -XX:+UseFastAccessorMethods -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled -XX:SurvivorRatio=8 -XX:MaxTenuringThreshold=1 -XX:CMSInitiatingOccupancyFraction=75 -XX:+UseCMSInitiatingOccupancyOnly -Djava.awt.headless=true -server'

  # Quote because path may contain spaces
  if [ -h $0 ]
  then
    ROOT_DIR="$(cd "$(dirname "$(readlink -n "$0")")" && pwd)"
  else
    ROOT_DIR="$(cd "$(dirname $0)" && pwd)"
  fi
  cd "$ROOT_DIR"

  # Include ROOT_DIR to do "ps aux | grep java" to get pid easier when
  # starting multiple processes from different directories
  CLASS_PATH="$ROOT_DIR/lib/*:config"

  # Use exec to be compatible with daemontools:
  # http://cr.yp.to/daemontools.html
  exec java $JAVA_OPTS -cp $CLASS_PATH "$@" my_package.MyMainClass
