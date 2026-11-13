#!/bin/sh

# compiles BlockingRequestMonitor.jar, needs a built Lucee checkout (loader/target/classes, temp/loader/core/core.lco)
# and the jakarta servlet, jsp and el api jars, set LUCEE_ROOT and M2 if the defaults do not fit

SCRIPT_DIR="$( cd "$( dirname "$0" )" && pwd )"
LUCEE_ROOT="${LUCEE_ROOT:-$SCRIPT_DIR/../../../..}"
M2="${M2:-$HOME/.m2/repository}"

CP="$LUCEE_ROOT/loader/target/classes:$LUCEE_ROOT/temp/loader/core/core.lco"
CP="$CP:$M2/jakarta/servlet/jakarta.servlet-api/6.0.0/jakarta.servlet-api-6.0.0.jar"
CP="$CP:$M2/jakarta/servlet/jsp/jakarta.servlet.jsp-api/3.1.0/jakarta.servlet.jsp-api-3.1.0.jar"
CP="$CP:$M2/jakarta/el/jakarta.el-api/5.0.1/jakarta.el-api-5.0.1.jar"

BUILD_DIR="$(mktemp -d)"

javac --release 11 -cp "$CP" -d "$BUILD_DIR" "$SCRIPT_DIR/src/lucee/test/ldev3792/BlockingRequestMonitor.java" || exit 1

jar cvf "$SCRIPT_DIR/BlockingRequestMonitor.jar" -C "$BUILD_DIR" lucee/test/ldev3792

rm -rf "$BUILD_DIR"
