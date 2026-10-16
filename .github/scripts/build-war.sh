#!/usr/bin/env bash
# LDEV-6529: build lucee-<version>.war from a Lucee jar, without cdn.lucee.org/lucee-*.war.
# Same layout as createWar() in lucee-data-provider (apps/updateserver/services/legacy/S3.cfc), which built the
# WARs on the update server until now:
#   - WEB-INF/lib/lucee.jar
#   - License.txt (build/common) and the welcome page (build/website) at the root
#   - the WEB-INF/web.xml of the matching war template (build/war-7.0, build/war-6.2 or build/war)
#   - an empty WEB-INF/lucee-server/context/deploy/ (no bundled extensions)
#
# usage: build-war.sh <version> <lucee jar> <target war>
set -euo pipefail

if [ $# -ne 3 ]; then
	echo "usage: $0 <version> <lucee jar> <target war>" >&2
	exit 2
fi
VERSION="$1"
JAR="$2"
TRG="$3"
DATA_PROVIDER_REPO="${DATA_PROVIDER_REPO:-https://github.com/lucee/lucee-data-provider.git}"
DP_BUILD=apps/updateserver/services/legacy/build

if [ ! -s "$JAR" ]; then
	echo "::error::Lucee jar [$JAR] does not exist"
	exit 1
fi

# war template as in getWarTemplate(): 7 and newer jakarta, 6.2 javax and jakarta, older javax
IFS=. read -r MAJOR MINOR _ <<< "${VERSION%%-*}"
if (( MAJOR >= 7 )); then
	TEMPLATE=war-7.0
elif (( MAJOR == 6 && MINOR >= 2 )); then
	TEMPLATE=war-6.2
else
	TEMPLATE=war
fi

mkdir -p "$(dirname "$TRG")"
TRG_ABS="$(cd "$(dirname "$TRG")" && pwd)/$(basename "$TRG")"
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

git clone -q --depth 1 --filter=blob:none --sparse "$DATA_PROVIDER_REPO" "$WORK/data-provider"
git -C "$WORK/data-provider" sparse-checkout set "$DP_BUILD/common" "$DP_BUILD/website" "$DP_BUILD/$TEMPLATE"
echo "lucee-data-provider: $(git -C "$WORK/data-provider" rev-parse HEAD), war template: $TEMPLATE"

WAR="$WORK/war"
mkdir -p "$WAR/WEB-INF/lib" "$WAR/WEB-INF/lucee-server/context/deploy"
cp "$JAR" "$WAR/WEB-INF/lib/lucee.jar"
cp -a "$WORK/data-provider/$DP_BUILD/common/." "$WAR/"
cp -a "$WORK/data-provider/$DP_BUILD/website/." "$WAR/"
cp -a "$WORK/data-provider/$DP_BUILD/$TEMPLATE/." "$WAR/"

rm -f "$TRG_ABS"
(cd "$WAR" && zip -qrX "$TRG_ABS" .)
unzip -tq "$TRG_ABS"
ls -l "$TRG_ABS"
