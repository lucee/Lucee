#!/usr/bin/env bash
# LDEV-6106: write <file>.sha256 and <file>.sha512 next to every file of a directory, in the format of sha256sum / sha512sum
# ("<hash>  <file name>"), so a download can be verified with: sha256sum -c <file>.sha256
# Files that already are hash files are skipped, existing hash files are overwritten.
#
# usage: write-hashes.sh <directory>
set -euo pipefail

if [ $# -ne 1 ] || [ ! -d "$1" ]; then
	echo "usage: $0 <directory>" >&2
	exit 2
fi
cd "$1"
shopt -s nullglob
for f in *; do
	[ -f "$f" ] || continue
	case "$f" in *.sha256|*.sha512) continue;; esac
	sha256sum -- "$f" > "$f.sha256"
	sha512sum -- "$f" > "$f.sha512"
	echo "hashes written for $f"
done
