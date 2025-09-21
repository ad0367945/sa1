#!/usr/bin/env sh
APP_NAME="Gradle"
APP_BASE_NAME=`basename "$0"`
DEFAULT_JVM_OPTS=""
DIRNAME=`dirname "$0"`
if [ "$DIRNAME" = "." ] ; then
  DIRNAME=`pwd`
fi
CLASSPATH=$DIRNAME/gradle/wrapper/gradle-wrapper.jar
exec "$JAVA_HOME/bin/java" $DEFAULT_JVM_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
