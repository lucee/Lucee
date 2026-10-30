import test from 'node:test';
import assert from 'node:assert/strict';
import http from 'node:http';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { createHash } from 'node:crypto';
import { r2Config, r2Key, uploadToR2, DEFAULT_SNAPSHOT_PREFIX } from './r2.js';
import { coreFiles, buildPuts, versionMetadataXml } from './upload-core-to-r2.js';

const full = { R2_ENDPOINT: 'https://x.r2.example.com', R2_BUCKET: 'b', R2_ACCESS_KEY_ID: 'k', R2_SECRET_ACCESS_KEY: 's' };

test('R2 is not configured when nothing is set', () => {
  assert.deepEqual(r2Config({}), { enabled: false });
  assert.deepEqual(r2Config({ R2_ENDPOINT: ' ', R2_PREFIX: 'x/' }), { enabled: false });
});
test('half configured R2 throws and names what is missing', () => {
  assert.throws(() => r2Config({ R2_ENDPOINT: 'https://x', R2_BUCKET: 'b' }), /R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY/);
  assert.throws(() => r2Config({ ...full, R2_SECRET_ACCESS_KEY: '' }), /R2_SECRET_ACCESS_KEY/);
});
test('prefixes get a trailing slash, snapshot prefix defaults to the worker default', () => {
  const c = r2Config(full);
  assert.equal(c.prefix, '');
  assert.equal(c.snapshotPrefix, DEFAULT_SNAPSHOT_PREFIX);
  assert.equal(DEFAULT_SNAPSHOT_PREFIX, 'snapshots/');
  const d = r2Config({ ...full, R2_PREFIX: '/pre', R2_SNAPSHOT_PREFIX: 'snap' });
  assert.equal(d.prefix, 'pre/');
  assert.equal(d.snapshotPrefix, 'snap/');
  assert.equal(r2Config({ ...full, R2_SNAPSHOT_PREFIX: '' }).snapshotPrefix, 'snapshots/');
});
test('keys: Maven layout, SNAPSHOTs below the snapshot prefix, both below R2_PREFIX', () => {
  const k = 'org/lucee/lucee/7.1.0.10/lucee-7.1.0.10.jar';
  assert.equal(r2Key(r2Config(full), k, '7.1.0.10'), k);
  assert.equal(r2Key(r2Config(full), k, '7.1.0.10-RC'), k);
  const s = 'org/lucee/lucee/7.1.0.10-SNAPSHOT/lucee-7.1.0.10-SNAPSHOT.jar';
  assert.equal(r2Key(r2Config(full), s, '7.1.0.10-SNAPSHOT'), 'snapshots/' + s);
  assert.equal(r2Key(r2Config({ ...full, R2_PREFIX: 'p' }), s, '7.1.0.10-SNAPSHOT'), 'p/snapshots/' + s);
  assert.equal(r2Key(r2Config({ ...full, R2_PREFIX: 'p' }), k, '7.1.0.10'), 'p/' + k);
});

function buildOutput(v) {
  const d = mkdtempSync(join(tmpdir(), 'core-r2-'));
  for (const n of [`lucee-${v}.jar`, `lucee-light-${v}.jar`, `lucee-zero-${v}.jar`, `${v}.lco`, `${v}.jvm21.lco`]) writeFileSync(join(d, n), 'content of ' + n);
  const pom = join(d, '.flattened-pom.xml');
  writeFileSync(pom, `<project><version>${v}</version></project>`);
  return { d, pom };
}

test('core files are mapped to the Maven names, with sha1 and version metadata, and no legacy root names', () => {
  const v = '7.1.0.10-SNAPSHOT';
  const { d, pom } = buildOutput(v);
  const puts = buildPuts(coreFiles(d, pom, v), v, '20261030120000');
  const base = `org/lucee/lucee/${v}/`;
  assert.deepEqual(puts.map(p => p.key).sort(), [
    'lucee-' + v + '.jar', 'lucee-' + v + '.lco', 'lucee-' + v + '-light.jar', 'lucee-' + v + '-zero.jar', 'lucee-' + v + '.pom'
  ].flatMap(n => [base + n, base + n + '.sha1']).concat(base + 'maven-metadata.xml').sort());
  const jar = puts.find(p => p.key === `${base}lucee-${v}.jar`);
  assert.equal(puts.find(p => p.key === `${base}lucee-${v}.jar.sha1`).body, createHash('sha1').update(jar.body).digest('hex'));
  assert.equal(puts.find(p => p.key === `${base}lucee-${v}-light.jar`).body.toString(), `content of lucee-light-${v}.jar`);
  assert.ok(puts.every(p => p.key.startsWith(base)), 'nothing outside the version folder');
  assert.match(versionMetadataXml(v, '1'), /<extension>lco<\/extension>/);
});
test('a missing build output file is an error', () => {
  const v = '7.1.0.10';
  const { d, pom } = buildOutput(v);
  assert.throws(() => coreFiles(d, join(d, 'nope.xml'), v), /build output missing/);
  assert.throws(() => coreFiles(join(d, 'nowhere'), pom, v), /build output missing/);
});

// minimal S3 endpoint: records PUT /<bucket>/<key>, answers 200 (or a given error)
async function mock(status = 200) {
  const got = [];
  const srv = http.createServer((req, res) => {
    const chunks = [];
    req.on('data', c => chunks.push(c));
    req.on('end', () => { got.push({ method: req.method, path: decodeURIComponent(req.url.split('?')[0]), body: Buffer.concat(chunks).toString('latin1'), ct: req.headers['content-type'] }); res.statusCode = status; res.end(status === 200 ? '' : '<Error><Code>AccessDenied</Code><Message>no</Message></Error>'); });
  });
  await new Promise(r => srv.listen(0, '127.0.0.1', r));
  return { got, url: `http://127.0.0.1:${srv.address().port}`, close: () => new Promise(r => srv.close(r)) };
}

test('uploads go to <bucket>/<prefix><snapshot prefix><maven key> on the configured endpoint', async () => {
  const m = await mock();
  try {
    const cfg = r2Config({ ...full, R2_ENDPOINT: m.url, R2_PREFIX: 'pre' });
    const v = '7.1.0.10-SNAPSHOT';
    const { d, pom } = buildOutput(v);
    await uploadToR2(cfg, v, buildPuts(coreFiles(d, pom, v), v), () => {});
    assert.equal(m.got.length, 11);
    assert.ok(m.got.every(g => g.method === 'PUT' && g.path.startsWith(`/b/pre/snapshots/org/lucee/lucee/${v}/`)), JSON.stringify(m.got.map(g => g.path)));
    assert.ok(m.got.some(g => g.path.endsWith(`/lucee-${v}-light.jar`) && g.body.includes('content of lucee-light')));
  } finally { await m.close(); }
});
test('an upload failure throws', async () => {
  const m = await mock(403);
  try {
    const cfg = r2Config({ ...full, R2_ENDPOINT: m.url });
    await assert.rejects(() => uploadToR2(cfg, '7.1.0.10', [{ key: 'org/lucee/lucee/7.1.0.10/a.jar', body: 'x', type: 'text/plain' }], () => {}));
  } finally { await m.close(); }
});

// the extension script end to end: R2 only (no S3 credentials), against the mock endpoint
import { execFile } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { mkdirSync } from 'node:fs';
const script = fileURLToPath(new URL('./upload-extension-to-s3.js', import.meta.url));
function runExt(env) {
  return new Promise(res => execFile(process.execPath, [script], { env: { PATH: process.env.PATH, ...env } }, (err, stdout, stderr) => res({ code: err ? err.code : 0, out: stdout + stderr })));
}
function extDir(v) {
  const d = mkdtempSync(join(tmpdir(), 'ext-r2-'));
  mkdirSync(join(d, 'target'));
  writeFileSync(join(d, 'target', `redis-extension-${v}.lex`), 'full');
  writeFileSync(join(d, 'target', `redis-extension-${v}-lite.lex`), 'lite');
  writeFileSync(join(d, '.flattened-pom.xml'), '<project><artifactId>redis-extension</artifactId></project>');
  return d;
}
test('extension upload: R2 only, SNAPSHOT goes below the snapshot prefix', async () => {
  const m = await mock();
  try {
    const v = '3.0.0.1-SNAPSHOT';
    const r = await runExt({ INPUT_VERSION: v, INPUT_DIR: extDir(v), INPUT_S3_BUCKET: 'lucee-downloads', ...full, R2_ENDPOINT: m.url });
    assert.equal(r.code, 0, r.out);
    const keys = m.got.map(g => g.path).sort();
    const b = `/b/snapshots/org/lucee/redis-extension/${v}/redis-extension-${v}`;
    assert.deepEqual(keys, [`${b}-lite.lex`, `${b}-lite.lex.sha1`, `${b}.lex`, `${b}.lex.sha1`, `${b}.pom`, `${b}.pom.sha1`, `/b/snapshots/org/lucee/redis-extension/${v}/maven-metadata.xml`].sort());
  } finally { await m.close(); }
});
test('extension upload: a failing R2 upload fails the run', async () => {
  const m = await mock(403);
  try {
    const v = '3.0.0.1';
    const r = await runExt({ INPUT_VERSION: v, INPUT_DIR: extDir(v), INPUT_S3_BUCKET: 'lucee-downloads', ...full, R2_ENDPOINT: m.url });
    assert.notEqual(r.code, 0);
    assert.match(r.out, /R2 upload failed/);
  } finally { await m.close(); }
});
test('extension upload: neither S3 nor R2 configured uploads nothing', async () => {
  const v = '3.0.0.1';
  const r = await runExt({ INPUT_VERSION: v, INPUT_DIR: extDir(v), INPUT_S3_BUCKET: 'lucee-downloads' });
  assert.equal(r.code, 0, r.out);
  assert.match(r.out, /nothing to upload/);
});
