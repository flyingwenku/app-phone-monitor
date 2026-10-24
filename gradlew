#!/bin/sh
#
# Gradle start up script for POSIX (simplified wrapper)
#

PRG="$0"
while [ -h "$PRG" ]; do
  ls=$(ls -ld "$PRG")
  link=$(expr "$ls" : '.*-> \(.*\)$')
  case "$link" in
    /*) PRG="$link" ;;
    *)  PRG=$(dirname "$PRG")"/$link" ;;
  esac
done

APP_HOME=$(cd "$(dirname "$PRG")" > /dev/null && pwd)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
  if [ ! -x "$JAVACMD" ]; then
    echo "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME" >&2
    exit 1
  fi
else
  JAVACMD=$(command -v java)
  if [ -z "$JAVACMD" ]; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found." >&2
    exit 1
  fi
fi

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

exec "$JAVACMD" $DEFAULT_JVM_OPTS $JAVA_OPTS $GRADLE_OPTS \
  -classpath "$CLASSPATH" \
  -Dorg.gradle.appname="gradlew" \
  org.gradle.wrapper.GradleWrapperMain "$@"
