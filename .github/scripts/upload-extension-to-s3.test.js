import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { deflateRawSync, crc32 } from 'node:zlib';
import { decide, S3_CUTOFF, artifactMetadataXml, findInputs, isPreRelease, readZip, findOwnJars, findReferencedJars, planOwnJars, versionMetadataXml } from './upload-extension-to-s3.js';

function layout(files) {
  const d = mkdtempSync(join(tmpdir(), 'ext-s3-'));
  for (const f of files) { mkdirSync(join(d, f, '..'), { recursive: true }); writeFileSync(join(d, f), 'x'); }
  return d;
}

const before = new Date(S3_CUTOFF.getTime() - 1000);
const after = new Date(S3_CUTOFF.getTime());

test('before the cut-off everything is uploaded', () => {
  for (const v of ['1.0.0.1-SNAPSHOT', '1.0.0.1-RC', '1.0.0.1-BETA', '1.0.0.1']) assert.equal(decide(v, before).upload, true);
});
test('from the cut-off SNAPSHOTs are skipped, the rest uploaded', () => {
  assert.equal(decide('1.0.0.1-SNAPSHOT', after).upload, false);
  for (const v of ['1.0.0.1-RC', '1.0.0.1-BETA', '1.0.0.1-ALPHA', '1.0.0.1']) assert.equal(decide(v, after).upload, true);
});
test('artifact metadata adds the version and keeps release on non-snapshots', () => {
  const x1 = artifactMetadataXml('smb-extension', null, '1.0.0.5-SNAPSHOT', '20261012000000');
  assert.match(x1, /<release><\/release>/);
  const x2 = artifactMetadataXml('smb-extension', x1, '1.0.0.7', '20261012000001');
  assert.match(x2, /<version>1\.0\.0\.5-SNAPSHOT<\/version>[\s\S]*<version>1\.0\.0\.7<\/version>/);
  assert.match(x2, /<release>1\.0\.0\.7<\/release>/);
  assert.match(x2, /<latest>1\.0\.0\.7<\/latest>/);
});

const V = '1.0.0.9-SNAPSHOT';
test('finds .lex files in target/ below the flattened POM (upload-artifact layout)', () => {
  const d = layout(['.flattened-pom.xml', `target/smb-extension-${V}.lex`, `target/smb-extension-${V}-lite.lex`]);
  const r = findInputs(d, V);
  assert.equal(r.full, join(d, `target/smb-extension-${V}.lex`));
  assert.equal(r.lite, join(d, `target/smb-extension-${V}-lite.lex`));
  assert.equal(r.pom, join(d, '.flattened-pom.xml'));
});
test('flat layout still works and .lite.lex is recognised', () => {
  const d = layout(['pom.xml', `x-${V}.lex`, `x-${V}.lite.lex`]);
  const r = findInputs(d, V);
  assert.equal(r.full, join(d, `x-${V}.lex`));
  assert.equal(r.lite, join(d, `x-${V}.lite.lex`));
  assert.equal(r.pom, join(d, 'pom.xml'));
});
test('prefers files matching the version, ignores others', () => {
  const d = layout(['.flattened-pom.xml', 'target/x-1.0.0.8-SNAPSHOT.lex', `target/x-${V}.lex`]);
  const r = findInputs(d, V);
  assert.equal(r.full, join(d, `target/x-${V}.lex`));
  assert.equal(r.lite, null);
});
test('fails clearly when no matching .lex exists', () => {
  const d = layout(['.flattened-pom.xml', 'target/x-1.0.0.8-SNAPSHOT.lex']);
  assert.throws(() => findInputs(d, V), /no 1\.0\.0\.9-SNAPSHOT \.lex found.*target\/x-1\.0\.0\.8-SNAPSHOT\.lex/);
});
test('fails on ambiguous duplicates at the same depth', () => {
  const d = layout([`a/x-${V}.lex`, `b/x-${V}.lex`]);
  assert.throws(() => findInputs(d, V), /ambiguous/);
});
test('shallower match wins over a nested copy', () => {
  const d = layout([`target/x-${V}.lex`, `target/sub/x-${V}.lex`]);
  assert.equal(findInputs(d, V).full, join(d, `target/x-${V}.lex`));
});

// LDEV-6721: own jars of pre-releases are taken from the full .lex
// builds a zip in memory, every other entry deflated, with directory entries like the real .lex files
function zip(files) {
  const locals = [], central = [];
  let offset = 0, n = 0;
  const dirs = new Set();
  for (const name of Object.keys(files)) { const parts = name.split('/'); for (let i = 1; i < parts.length; i++) dirs.add(parts.slice(0, i).join('/') + '/'); }
  const entries = [...[...dirs].map(d => [d, Buffer.alloc(0)]), ...Object.entries(files).map(([k, v]) => [k, Buffer.from(v)])];
  for (const [name, raw] of entries) {
    const deflate = !name.endsWith('/') && n++ % 2 === 0;
    const data = deflate ? deflateRawSync(raw) : raw;
    const nameBuf = Buffer.from(name);
    const lh = Buffer.alloc(30);
    lh.writeUInt32LE(0x04034b50, 0); lh.writeUInt16LE(20, 4); lh.writeUInt16LE(deflate ? 8 : 0, 8);
    lh.writeUInt32LE(crc32(raw), 14); lh.writeUInt32LE(data.length, 18); lh.writeUInt32LE(raw.length, 22); lh.writeUInt16LE(nameBuf.length, 26);
    const ch = Buffer.alloc(46);
    ch.writeUInt32LE(0x02014b50, 0); ch.writeUInt16LE(20, 4); ch.writeUInt16LE(20, 6); ch.writeUInt16LE(deflate ? 8 : 0, 10);
    ch.writeUInt32LE(crc32(raw), 16); ch.writeUInt32LE(data.length, 20); ch.writeUInt32LE(raw.length, 24); ch.writeUInt16LE(nameBuf.length, 28); ch.writeUInt32LE(offset, 42);
    locals.push(lh, nameBuf, data); central.push(ch, nameBuf);
    offset += lh.length + nameBuf.length + data.length;
  }
  const cd = Buffer.concat(central);
  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0); end.writeUInt16LE(entries.length, 8); end.writeUInt16LE(entries.length, 10); end.writeUInt32LE(cd.length, 12); end.writeUInt32LE(offset, 16);
  return Buffer.concat([...locals, cd, end]);
}

const RC = '2.2.0.1-RC';
const manifest = (v, coord) => `Manifest-Version: 1.0\r\nversion: "${v}"\r\nresource: "[{'class':'a.B','maven':'${coord.slice(0, 20)}\r\n ${coord.slice(20)}','scheme':'tgz'}]"\r\n`;
const fld = coord => `<func-lib><function><class maven="${coord}">a.C</class></function></func-lib>`;
function fullLex(v, extra = {}) {
  return zip({
    'META-INF/MANIFEST.MF': manifest(v, `org.lucee:compress:${v}`),
    'flds/compress.fld': fld(`org.lucee:compress:${v}`),
    [`maven/org/lucee/compress/${v}/compress-${v}.jar`]: 'JAR-' + 'x'.repeat(500),
    [`maven/org/lucee/compress/${v}/compress-${v}.pom`]: '<project/>',
    'maven/commons-io/commons-io/2.21.0/commons-io-2.21.0.jar': 'dep',
    'maven/org/lucee/other/1.0.0.0/other-1.0.0.0.jar': 'older own jar',
    ...extra
  });
}
function liteLex(v, coord = `org.lucee:compress:${v}`) {
  return zip({ 'META-INF/MANIFEST.MF': manifest(v, coord), 'flds/compress.fld': fld(coord) });
}

test('isPreRelease: only RC/BETA/ALPHA', () => {
  for (const v of ['1.0.0.1-RC', '1.0.0.1-BETA', '1.0.0.1-ALPHA', '1.0.0.1-rc', '1.0.0.1-RC2']) assert.equal(isPreRelease(v), true, v);
  for (const v of ['1.0.0.1', '1.0.0.1-SNAPSHOT', '1.0.0.1-RC-SNAPSHOT']) assert.equal(isPreRelease(v), false, v);
});
test('readZip reads stored and deflated entries and skips directories', () => {
  const e = readZip(zip({ 'a/one.txt': 'first', 'a/b/two.txt': 'second'.repeat(100) }));
  assert.deepEqual([...e.keys()].sort(), ['a/b/two.txt', 'a/one.txt']);
  assert.equal(e.get('a/one.txt')().toString(), 'first');
  assert.equal(e.get('a/b/two.txt')().toString(), 'second'.repeat(100));
});
test('readZip fails on something that is not a zip', () => {
  assert.throws(() => readZip(Buffer.from('not a zip file at all, just some text here')), /not a zip file/);
});
test('findOwnJars returns only org.lucee jars with the extension version', () => {
  const r = findOwnJars(readZip(fullLex(RC)), RC);
  assert.deepEqual(r, [{ artifactId: 'compress', jar: `maven/org/lucee/compress/${RC}/compress-${RC}.jar`, pom: `maven/org/lucee/compress/${RC}/compress-${RC}.pom` }]);
});
test('findReferencedJars reads wrapped manifest lines and .fld files', () => {
  const e = readZip(zip({ 'META-INF/MANIFEST.MF': manifest(RC, `org.lucee:compress:${RC}`), 'tlds/x.tldx': `<tag-class maven="org.lucee:ziptag:${RC}">a</tag-class><x maven="org.lucee:old:${RC}2"/>` }));
  assert.deepEqual(findReferencedJars(e, RC), ['compress', 'ziptag']);
});
test('planOwnJars: pre-release with lite returns the own jar', () => {
  const r = planOwnJars(readZip(fullLex(RC)), readZip(liteLex(RC)), RC);
  assert.equal(r.length, 1);
  assert.equal(r[0].artifactId, 'compress');
  assert.equal(readZip(fullLex(RC)).get(r[0].jar)().toString(), 'JAR-' + 'x'.repeat(500));
});
test('planOwnJars: pre-release without lite still returns the own jar', () => {
  assert.equal(planOwnJars(readZip(fullLex('1.0.0.3-BETA')), null, '1.0.0.3-BETA').length, 1);
});
test('planOwnJars: final releases upload no own jar (they are on Maven Central)', () => {
  assert.deepEqual(planOwnJars(readZip(fullLex('2.2.0.1')), readZip(liteLex('2.2.0.1')), '2.2.0.1'), []);
});
test('planOwnJars: fails when the lite .lex references a jar missing in the full .lex', () => {
  const full = readZip(zip({ 'META-INF/MANIFEST.MF': 'Manifest-Version: 1.0\r\n' }));
  assert.throws(() => planOwnJars(full, readZip(liteLex(RC)), RC), /lite \.lex references org\.lucee:compress:2\.2\.0\.1-RC but the full \.lex does not contain this jar/);
});
test('planOwnJars: a reference resolved inside the lite .lex itself is fine', () => {
  const lite = readZip(zip({ 'flds/a.fld': fld(`org.lucee:compress:${RC}`), [`maven/org/lucee/compress/${RC}/compress-${RC}.jar`]: 'jar' }));
  const full = readZip(zip({ 'META-INF/MANIFEST.MF': 'Manifest-Version: 1.0\r\n' }));
  assert.deepEqual(planOwnJars(full, lite, RC), []);
});
test('versionMetadataXml lists the given extensions, lex+pom by default', () => {
  assert.match(versionMetadataXml('x-extension', RC, '1'), /<extension>lex<\/extension>[\s\S]*<extension>pom<\/extension>/);
  const j = versionMetadataXml('compress', RC, '1', ['jar', 'pom']);
  assert.match(j, /<extension>jar<\/extension>[\s\S]*<extension>pom<\/extension>/);
  assert.doesNotMatch(j, /<extension>lex<\/extension>/);
});
test('planOwnJars: a reference to the extension itself is not a missing jar', () => {
  const lite = readZip(liteLex(RC, `org.lucee:compress-extension:${RC}`));
  assert.throws(() => planOwnJars(readZip(fullLex(RC)), lite, RC), /org\.lucee:compress-extension:2\.2\.0\.1-RC/);
  assert.equal(planOwnJars(readZip(fullLex(RC)), lite, RC, 'compress-extension').length, 1);
});
