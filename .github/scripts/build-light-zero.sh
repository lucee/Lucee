#!/usr/bin/env bash
# LDEV-6711: build lucee-light-<version>.jar and lucee-zero-<version>.jar from the Lucee jar, after the Maven build.
# Before this was done by createLight() in ant/upload_to_s3.cfm with a running Lucee, inside the build.
#   light: no bundled extensions (the extensions folder is empty, the maven files of the extensions are removed,
#          Require-Extension is removed from the manifest)
#   zero:  light without the archives of the admin and the documentation (lucee-admin.lar, lucee-doc.lar)
#
# usage: build-light-zero.sh <version> <lucee jar> <target directory>
# needs: unzip, awk and a JDK (jar)
set -euo pipefail

if [ $# -ne 3 ]; then
	echo "usage: $0 <version> <lucee jar> <target directory>" >&2
	exit 2
fi
VERSION="$1"
JAR="$(realpath "$2")"
TRG_DIR="$3"

if [ ! -s "$JAR" ]; then
	echo "::error::Lucee jar [$JAR] does not exist"
	exit 1
fi
mkdir -p "$TRG_DIR"
TRG_DIR="$(realpath "$TRG_DIR")"
LIGHT="$TRG_DIR/lucee-light-$VERSION.jar"
ZERO="$TRG_DIR/lucee-zero-$VERSION.jar"

WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

pack() { # <directory> <target jar>, the manifest is the first entry
	rm -f "$2"
	jar --create --file "$2" --manifest "$1/META-INF/MANIFEST.MF" -C "$1" . 
}

check_no_maven() { # <jar>
	if unzip -Z1 "$1" | grep -q '^maven/'; then
		echo "::error::[$1] still contains the maven folder"
		exit 1
	fi
}

mb() { echo "$(( $(wc -c < "$1") / 1024 / 1024 )) MB"; }

# light
LDIR="$WORK/light"
mkdir -p "$LDIR"
unzip -q "$JAR" -d "$LDIR"
rm -rf "$LDIR/extensions" "$LDIR/maven"
mkdir "$LDIR/extensions"
: > "$LDIR/extensions/.index"
# remove Require-Extension (and its continuation lines) from the manifest
awk '
	/^Require-Extension:/ { skip = 1; next }
	skip && /^ / { next }
	{ skip = 0; print }
' "$LDIR/META-INF/MANIFEST.MF" > "$WORK/MANIFEST.MF"
cp "$WORK/MANIFEST.MF" "$LDIR/META-INF/MANIFEST.MF"
pack "$LDIR" "$LIGHT"
check_no_maven "$LIGHT"
echo "$(basename "$LIGHT") $(mb "$LIGHT")"

# zero
rm -f "$LDIR/resource/context/lucee-admin.lar" "$LDIR/resource/context/lucee-doc.lar"
pack "$LDIR" "$ZERO"
check_no_maven "$ZERO"
echo "$(basename "$ZERO") $(mb "$ZERO")"
