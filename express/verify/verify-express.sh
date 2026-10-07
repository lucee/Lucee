#!/usr/bin/env bash
# verify-express.sh - pre-publish gate for org.lucee:lucee-express (LDEV ticket, Phase 1)
#
#   verify-express.sh <luceeVersion> <tomcatVersion> <pom> <templateZip> <workDir> [<localRepo>]
#
# 1. stages the just-built POM + template zip in a file:// repo (Maven layout)
# 2. assembles Lucee Express with the reference assembler (verify/LuceeExpress.java) from:
#    staging repo -> local repo (~/.m2, has the just-installed org.lucee:lucee jar) -> Maven Central
# 3. structural checks (jars in lib/ext, Tomcat version, template applied, exec bits)
# 4. optional: compare against a CDN listing (size + CRC per file) if EXPRESS_REFERENCE_LISTING is set
# 5. boots the assembled tree (catalina.sh run) and checks /version.cfm == <luceeVersion> and / == 200
#
# Env: EXPRESS_VERIFY_REPOS (default https://repo1.maven.org/maven2), EXPRESS_VERIFY_PORT (default 8888),
#      EXPRESS_VERIFY_BOOT=false to skip the boot test, EXPRESS_VERIFY_TIMEOUT (s, default 180),
#      EXPRESS_REFERENCE_LISTING=<file from tools/remotezip.py> (optional)
set -euo pipefail
[ $# -ge 5 ] || { echo "usage: $0 <luceeVersion> <tomcatVersion> <pom> <templateZip> <workDir> [<localRepo>]" >&2; exit 2; }
V=$1; TOMCAT=$2; POM=$3; TEMPLATE=$4; WORK=$5; M2=${6:-$HOME/.m2/repository}
HERE=$(cd "$(dirname "$0")" && pwd)
REPOS=${EXPRESS_VERIFY_REPOS:-https://repo1.maven.org/maven2}
PORT=${EXPRESS_VERIFY_PORT:-8888}
TIMEOUT=${EXPRESS_VERIFY_TIMEOUT:-180}
fail() { echo "::error::lucee-express verify: $*" >&2; exit 1; }
log() { echo "[verify-express] $*"; }

[ -f "$POM" ] || fail "POM not found: $POM"
[ -f "$TEMPLATE" ] || fail "template zip not found: $TEMPLATE"
grep -q '\${revision}' "$POM" && fail "published POM still contains \${revision}: $POM"

rm -rf "$WORK"; mkdir -p "$WORK"
STAGE="$WORK/stage"; GAV="$STAGE/org/lucee/lucee-express/$V"
mkdir -p "$GAV"
cp "$POM" "$GAV/lucee-express-$V.pom"
cp "$TEMPLATE" "$GAV/lucee-express-$V-template.zip"
for f in "$GAV"/*; do sha256sum "$f" | cut -d' ' -f1 > "$f.sha256"; done

# --- 1. assemble ------------------------------------------------------------------------------------
ARGS=(--repo "file://$STAGE")
for r in $REPOS; do ARGS+=(--repo "$r"); done
[ -d "$M2" ] && ARGS+=(--read-cache "$M2")
OUT="$WORK/express"
log "assembling $V into $OUT"
java "$HERE/LuceeExpress.java" "$V" "$OUT" "${ARGS[@]}" --cache "$WORK/cache" --force | tee "$WORK/assemble.log"

# --- 2. structural checks -----------------------------------------------------------------------------
want_jars="javax.el-api-3.0.0.jar javax.servlet-api-4.0.1.jar javax.servlet.jsp-api-2.3.3.jar lucee-$V.jar"
have_jars=$(cd "$OUT/lib/ext" && ls | sort | tr '\n' ' ' | sed 's/ $//')
[ "$have_jars" = "$want_jars" ] || fail "lib/ext is [$have_jars], expected [$want_jars]"
LOCAL_JAR="$M2/org/lucee/lucee/$V/lucee-$V.jar"
if [ -f "$LOCAL_JAR" ]; then
  cmp -s "$LOCAL_JAR" "$OUT/lib/ext/lucee-$V.jar" || fail "lib/ext/lucee-$V.jar differs from the jar being published ($LOCAL_JAR)"
fi
grep -q "Apache Tomcat Version $TOMCAT" "$OUT/RELEASE-NOTES" || fail "RELEASE-NOTES is not Tomcat $TOMCAT"
grep -q "Apache Tomcat $TOMCAT " "$OUT/bin/service.bat" || fail "bin/service.bat does not name Tomcat $TOMCAT"
grep -q 'port="8888"' "$OUT/conf/server.xml" || fail "conf/server.xml is not the Lucee one (no port 8888)"
[ -f "$OUT/webapps/ROOT/index.cfm" ] || fail "webapps/ROOT/index.cfm missing (template not applied)"
[ "$(ls "$OUT/webapps")" = "ROOT" ] || fail "webapps/ contains more than ROOT: $(ls "$OUT/webapps" | tr '\n' ' ')"
[ -d "$OUT/lucee-server/context/deploy" ] || fail "lucee-server/context/deploy/ missing"
for x in bin/catalina.sh bin/startup.sh startup.sh shutdown.sh; do [ -x "$OUT/$x" ] || fail "$x is not executable"; done
NFILES=$(find "$OUT" -type f | wc -l)
log "structure OK: $NFILES files (Tomcat 11.0.x express: 116 expected), lib/ext = $have_jars"

if [ -n "${EXPRESS_REFERENCE_LISTING:-}" ]; then
  python3 "$HERE/compare-listing.py" "$EXPRESS_REFERENCE_LISTING" "$OUT" | tee "$WORK/compare.txt"
fi

# --- 3. boot test ---------------------------------------------------------------------------------------
if [ "${EXPRESS_VERIFY_BOOT:-true}" != "false" ]; then
  RUN="$WORK/run"; rm -rf "$RUN"; cp -a "$OUT" "$RUN"
  SHUT=$((PORT + 1000)); AJP=$((PORT + 2000))
  sed -e "s/port=\"8888\"/port=\"$PORT\"/" -e "s/port=\"8005\"/port=\"$SHUT\"/" -e "s/port=\"8009\"/port=\"$AJP\"/" "$RUN/conf/server.xml" > "$RUN/conf/server.xml.new" && mv "$RUN/conf/server.xml.new" "$RUN/conf/server.xml"
  echo '<cfoutput>#server.lucee.version#</cfoutput>' > "$RUN/webapps/ROOT/version.cfm"
  log "booting on port $PORT (timeout ${TIMEOUT}s)"
  "$RUN/bin/catalina.sh" run > "$WORK/catalina.log" 2>&1 &
  PID=$!
  GOT=""; i=0
  while [ $i -lt "$TIMEOUT" ]; do
    GOT=$(curl -s -m 5 "http://127.0.0.1:$PORT/version.cfm" | tr -d '[:space:]' || true)
    [ "$GOT" = "$V" ] && break
    kill -0 $PID 2>/dev/null || break
    sleep 1; i=$((i + 1))
  done
  INDEX=$(curl -s -o /dev/null -w '%{http_code}' -m 10 "http://127.0.0.1:$PORT/" || true)
  "$RUN/bin/catalina.sh" stop > /dev/null 2>&1 || true
  # Lucee keeps non-daemon threads, so the JVM may not exit after a clean stop (same with the CDN zip): kill after 15 s
  j=0; while kill -0 $PID 2>/dev/null && [ $j -lt 15 ]; do sleep 1; j=$((j + 1)); done
  kill -0 $PID 2>/dev/null && { pkill -f "catalina.base=$RUN" || true; kill -9 $PID 2>/dev/null || true; }
  wait $PID 2>/dev/null || true
  if [ "$GOT" != "$V" ]; then tail -50 "$WORK/catalina.log" >&2; fail "/version.cfm returned '$GOT', expected '$V'"; fi
  [ "$INDEX" = "200" ] || fail "GET / returned HTTP $INDEX"
  log "boot OK: server.lucee.version=$GOT after ~${i}s, / -> $INDEX"
fi

SUMMARY="lucee-express $V verified: Tomcat $TOMCAT, $NFILES files, lib/ext=[$have_jars]"
log "$SUMMARY"
[ -n "${GITHUB_STEP_SUMMARY:-}" ] && echo "### $SUMMARY" >> "$GITHUB_STEP_SUMMARY" || true
