#!/usr/bin/env bash
# LDEV-6529: build lucee-express-<version>.zip from a Lucee jar, without cdn.lucee.org/lucee-express-*.zip.
# Same layout as createExpress() in lucee-data-provider (apps/updateserver/services/legacy/S3.cfc), which built the
# express zips on the update server until now:
#   - the Lucee Tomcat express template (built by lucee-installer express-template.yml, the update server's
#     /rest/update/provider/expressTemplates returns the current one per Tomcat major)
#   - lib/ext/lucee-<version>.jar
#   - License.txt and webapps/ROOT (welcome page) from lucee-data-provider
#   - empty lucee-server/context/deploy/
#
# usage: build-express.sh <version> <lucee jar> <target zip>
set -euo pipefail

if [ $# -ne 3 ]; then
	echo "usage: $0 <version> <lucee jar> <target zip>" >&2
	exit 2
fi
VERSION="$1"
JAR="$2"
TRG="$3"
TEMPLATES_URL="${EXPRESS_TEMPLATES_URL:-https://update.lucee.org/rest/update/provider/expressTemplates}"
DATA_PROVIDER_REPO="${DATA_PROVIDER_REPO:-https://github.com/lucee/lucee-data-provider.git}"
DP_BUILD=apps/updateserver/services/legacy/build

if [ ! -s "$JAR" ]; then
	echo "::error::Lucee jar [$JAR] does not exist"
	exit 1
fi

# Tomcat major as in createExpress(): 6.2.1 and newer use Tomcat 11, 6.2.0 Tomcat 10, older Tomcat 9
IFS=. read -r MAJOR MINOR PATCH _ <<< "${VERSION%%-*}"
if (( MAJOR > 6 || (MAJOR == 6 && MINOR > 2) || (MAJOR == 6 && MINOR == 2 && PATCH >= 1) )); then
	TOMCAT=tomcat-11
elif (( MAJOR == 6 && MINOR == 2 )); then
	TOMCAT=tomcat-10
else
	TOMCAT=tomcat-9
fi

TRG_ABS=$(realpath -m "$TRG")
WORK=$(mktemp -d)
trap 'rm -rf "$WORK"' EXIT

TEMPLATE_URL=$(curl -fsSL --retry 3 "$TEMPLATES_URL" | jq -er --arg t "$TOMCAT" '.[$t]')
echo "express template ($TOMCAT): $TEMPLATE_URL"
curl -fsSL --retry 3 -o "$WORK/template.zip" "$TEMPLATE_URL"

git clone -q --depth 1 --filter=blob:none --sparse "$DATA_PROVIDER_REPO" "$WORK/data-provider"
git -C "$WORK/data-provider" sparse-checkout set "$DP_BUILD/common" "$DP_BUILD/website"
echo "lucee-data-provider: $(git -C "$WORK/data-provider" rev-parse HEAD)"

EXPRESS="$WORK/express"
mkdir -p "$EXPRESS"
unzip -q "$WORK/template.zip" -d "$EXPRESS"
mkdir -p "$EXPRESS/lib/ext" "$EXPRESS/lucee-server/context/deploy" "$EXPRESS/webapps/ROOT"
cp "$JAR" "$EXPRESS/lib/ext/lucee-$VERSION.jar"
cp -a "$WORK/data-provider/$DP_BUILD/common/." "$EXPRESS/"
cp -a "$WORK/data-provider/$DP_BUILD/website/." "$EXPRESS/webapps/ROOT/"

rm -f "$TRG_ABS"
mkdir -p "$(dirname "$TRG_ABS")"
(cd "$EXPRESS" && zip -qrX "$TRG_ABS" .)
unzip -tq "$TRG_ABS"
ls -l "$TRG_ABS"
