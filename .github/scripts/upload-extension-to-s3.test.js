import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, mkdirSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { decide, S3_CUTOFF, artifactMetadataXml, findInputs } from './upload-extension-to-s3.js';

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
