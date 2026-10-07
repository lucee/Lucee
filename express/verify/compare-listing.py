#!/usr/bin/env python3
"""Compare an assembled directory with a CDN express zip listing (size + CRC-32 per file, from remotezip.py)."""
import sys, os, zlib
listing, root = sys.argv[1], sys.argv[2]
want = {}
for line in open(listing):
    if line.startswith("#"): continue
    size, crc, name = line.split(None, 2); name = name.rstrip("\n")
    want[name] = (int(size), int(crc, 16))
have = {}
for d, dirs, files in os.walk(root):
    rel = os.path.relpath(d, root).replace(os.sep, "/")
    if rel != ".": have[rel + "/"] = None
    for f in files:
        p = os.path.join(d, f); r = (f if rel == "." else rel + "/" + f)
        c = 0
        with open(p, "rb") as fh:
            for chunk in iter(lambda: fh.read(1 << 20), b""): c = zlib.crc32(chunk, c)
        have[r] = (os.path.getsize(p), c & 0xffffffff)
files_w = {k: v for k, v in want.items() if not k.endswith("/")}
files_h = {k: v for k, v in have.items() if not k.endswith("/")}
same = [k for k in files_w if files_h.get(k) == files_w[k]]
diff = [k for k in files_w if k in files_h and files_h[k] != files_w[k]]
missing = [k for k in files_w if k not in files_h]
extra = [k for k in files_h if k not in files_w]
dirs_missing = [k for k in want if k.endswith("/") and k not in have]
print(f"{root}: {len(same)}/{len(files_w)} files identical to CDN zip; different: {diff}; missing: {missing}; extra: {extra}; zip dir entries missing: {dirs_missing}")
