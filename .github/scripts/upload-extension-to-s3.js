// Uploads one extension build into the Maven layout of the lucee-downloads bucket (cdn.lucee.org):
//   org/lucee/<artifactId>/<version>/<artifactId>-<version>.lex (+ -lite.lex, .pom, .sha1)
//   org/lucee/<artifactId>/<version>/maven-metadata.xml
//   org/lucee/<artifactId>/maven-metadata.xml
//   org/lucee/maven-metadata.xml + org/lucee/index.html (group metadata)
// For RC/BETA/ALPHA builds also the extension's own jar(s) packed in the full .lex (LDEV-6721), because pre-releases
// are not on Maven Central and the lite .lex only references them by coordinate:
//   org/lucee/<jarArtifactId>/<version>/<jarArtifactId>-<version>.jar (+ .pom, .sha1, version and artifact maven-metadata.xml)
// Called by .github/workflows/extension-s3-upload.yml (workflow_call) from every extension build (LDEV-XXXX).
// LDEV-6804: when R2_ENDPOINT, R2_BUCKET, R2_ACCESS_KEY_ID and R2_SECRET_ACCESS_KEY are set, the same files (not the artifact and
// group metadata, which are derived from the whole repository) are also uploaded to the Cloudflare R2 bucket the Maven bridge worker
// serves from (see r2.js). That upload does not depend on the S3 cut-off and a failure fails the run.
import { S3Client, ListObjectsV2Command, GetBucketLocationCommand, PutObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { createHash } from 'node:crypto';
import { readFileSync, readdirSync } from 'node:fs';
import { inflateRawSync } from 'node:zlib';
import { join, basename, relative } from 'node:path';
import { r2Config, r2Key, uploadToR2 } from './r2.js';
import { DEFAULT_ORG_LUCEE_PREFIX, GROUP_METADATA_KEY, mavenTimestamp, sortVersions, findLatestRelease, xmlEscape, rebuildCdnGroupIndex } from './maven-group-metadata.js';

// The one place the extension cut-off is defined (same date as core S3_CUTOFF in ant/upload_to_s3.cfm, LDEV-6537):
// from then on SNAPSHOTs are only on Sonatype, RC/BETA/ALPHA/final only go into the Maven layout.
export const S3_CUTOFF = new Date('2027-03-01T00:00:00Z');

const log = (m, l = 'INFO') => console.log(`[${new Date().toISOString()}] [${l}] ${m}`);

export function isSnapshot(version) { return /-SNAPSHOT$/i.test(version); }

export function decide(version, now = new Date()) {
  if (isSnapshot(version) && now >= S3_CUTOFF) return { upload: false, reason: `SNAPSHOT not uploaded to S3 since ${S3_CUTOFF.toISOString().slice(0, 10)} (only on Sonatype)` };
  return { upload: true };
}

export function versionMetadataXml(artifactId, version, ts, extensions = ['lex', 'pom']) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>${xmlEscape(artifactId)}</artifactId>
  <version>${xmlEscape(version)}</version>
  <versioning>
    <lastUpdated>${ts}</lastUpdated>
    <snapshotVersions>
${extensions.map(e => `      <snapshotVersion>
        <extension>${xmlEscape(e)}</extension>
        <value>${xmlEscape(version)}</value>
        <updated>${ts}</updated>
      </snapshotVersion>`).join('\n')}
    </snapshotVersions>
  </versioning>
</metadata>`;
}

export function artifactMetadataXml(artifactId, existingXml, newVersion, ts) {
  const versions = [];
  if (existingXml) {
    const block = (existingXml.match(/<versions>([\s\S]*?)<\/versions>/) || [])[1] || '';
    for (const m of block.matchAll(/<version>([^<]+)<\/version>/g)) versions.push(m[1].trim());
  }
  if (!versions.includes(newVersion)) versions.push(newVersion);
  const sorted = sortVersions(versions);
  const latest = sorted[sorted.length - 1];
  const release = findLatestRelease(sorted) || '';
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>${xmlEscape(artifactId)}</artifactId>
  <versioning>
    <latest>${xmlEscape(latest)}</latest>
    <release>${xmlEscape(release)}</release>
    <versions>
${sorted.map(v => `      <version>${xmlEscape(v)}</version>`).join('\n')}
    </versions>
    <lastUpdated>${ts}</lastUpdated>
  </versioning>
</metadata>`;
}

export function isPreRelease(version) { return /-(RC|BETA|ALPHA)[0-9]*$/i.test(version); }

// Minimal zip reader for the .lex files (stored and deflated entries, no zip64), so the script needs no extra dependency.
// Returns a Map of entry name -> () => Buffer.
export function readZip(buf) {
  let eocd = -1;
  for (let i = buf.length - 22; i >= Math.max(0, buf.length - 22 - 0xffff); i--) {
    if (buf.readUInt32LE(i) === 0x06054b50) { eocd = i; break; }
  }
  if (eocd < 0) throw new Error('not a zip file (no end of central directory record)');
  const count = buf.readUInt16LE(eocd + 10);
  let p = buf.readUInt32LE(eocd + 16);
  if (count === 0xffff || p === 0xffffffff) throw new Error('zip64 archives are not supported');
  const entries = new Map();
  for (let n = 0; n < count; n++) {
    if (buf.readUInt32LE(p) !== 0x02014b50) throw new Error(`corrupt zip central directory at offset ${p}`);
    const method = buf.readUInt16LE(p + 10);
    const size = buf.readUInt32LE(p + 20);
    const nameLen = buf.readUInt16LE(p + 28), extraLen = buf.readUInt16LE(p + 30), commentLen = buf.readUInt16LE(p + 32);
    const local = buf.readUInt32LE(p + 42);
    const name = buf.toString('utf8', p + 46, p + 46 + nameLen);
    p += 46 + nameLen + extraLen + commentLen;
    if (name.endsWith('/')) continue;
    if (buf.readUInt32LE(local) !== 0x04034b50) throw new Error(`corrupt zip local header for ${name}`);
    const start = local + 30 + buf.readUInt16LE(local + 26) + buf.readUInt16LE(local + 28);
    entries.set(name, () => {
      const data = buf.subarray(start, start + size);
      if (method === 0) return Buffer.from(data);
      if (method === 8) return inflateRawSync(data);
      throw new Error(`unsupported zip compression method ${method} for ${name}`);
    });
  }
  return entries;
}

function escapeRe(s) { return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'); }

// The extension's own org.lucee jars inside a .lex: maven/org/lucee/<a>/<version>/<a>-<version>.jar (+ .pom), same version as the extension.
export function findOwnJars(entries, version) {
  const re = new RegExp(`^maven/org/lucee/([^/]+)/${escapeRe(version)}/\\1-${escapeRe(version)}\\.(jar|pom)$`);
  const out = new Map();
  for (const name of entries.keys()) {
    const m = name.match(re);
    if (!m) continue;
    const a = out.get(m[1]) || { artifactId: m[1], jar: null, pom: null };
    a[m[2]] = name;
    out.set(m[1], a);
  }
  return [...out.values()].filter(a => a.jar).sort((x, y) => x.artifactId.localeCompare(y.artifactId));
}

// org.lucee:<a>:<version> coordinates referenced by a .lex (manifest, .fld/.tld/.tldx and other text descriptors).
export function findReferencedJars(entries, version) {
  const re = new RegExp(`org\\.lucee:([A-Za-z0-9_.-]+?):${escapeRe(version)}(?![A-Za-z0-9_.-])`, 'g');
  const refs = new Set();
  for (const [name, read] of entries) {
    if (!/(^META-INF\/MANIFEST\.MF$|\.(fld|tld|tldx|xml|json|properties|txt)$)/i.test(name)) continue;
    // manifest lines are wrapped at 72 bytes, continuation lines start with a single space
    const text = read().toString('utf8').replace(/\r?\n /g, '');
    for (const m of text.matchAll(re)) refs.add(m[1]);
  }
  return [...refs].sort();
}

// Decides which own jars of a pre-release go to S3; throws when the lite .lex references one the full .lex does not contain.
// A reference to the extension itself (extensionArtifactId) is not a jar and is ignored.
export function planOwnJars(fullEntries, liteEntries, version, extensionArtifactId = null) {
  if (!isPreRelease(version)) return [];
  const own = findOwnJars(fullEntries, version);
  if (liteEntries) {
    const inLite = new Set(findOwnJars(liteEntries, version).map(a => a.artifactId));
    const have = new Set(own.map(a => a.artifactId));
    const missing = findReferencedJars(liteEntries, version).filter(a => a !== extensionArtifactId && !have.has(a) && !inLite.has(a));
    if (missing.length) throw new Error(`lite .lex references ${missing.map(a => `org.lucee:${a}:${version}`).join(', ')} but the full .lex does not contain ${missing.length > 1 ? 'these jars' : 'this jar'} (expected maven/org/lucee/<artifactId>/${version}/<artifactId>-${version}.jar)`);
  }
  return own;
}

// upload-artifact keeps the common ancestor path, so the .lex files usually sit in target/ below the POM.
// Walk the input dir recursively (skipping node_modules/.git) and pick the files for this version.
function walk(dir, out = []) {
  for (const e of readdirSync(dir, { withFileTypes: true })) {
    if (e.name === 'node_modules' || e.name === '.git') continue;
    const p = join(dir, e.name);
    if (e.isDirectory()) walk(p, out); else if (e.isFile()) out.push(p);
  }
  return out;
}

function isLite(f) { return /[.-]lite\.lex$/i.test(f); }

function pickOne(cands, what, dir) {
  if (cands.length === 0) return null;
  // prefer the shallowest match, but two at the same depth are ambiguous
  const depth = p => relative(dir, p).split(/[\\/]/).length;
  const min = Math.min(...cands.map(depth));
  const best = cands.filter(p => depth(p) === min);
  if (best.length > 1) throw new Error(`ambiguous ${what} in ${dir}: ${best.map(p => relative(dir, p)).join(', ')}`);
  return best[0];
}

export function findInputs(dir, version) {
  const all = walk(dir);
  const lex = all.filter(p => p.toLowerCase().endsWith('.lex'));
  const matching = lex.filter(p => basename(p).includes(version));
  for (const p of lex) if (!matching.includes(p)) log(`skipping ${relative(dir, p)}, not version ${version}`, 'WARN');
  const full = pickOne(matching.filter(p => !isLite(p)), `${version} .lex`, dir);
  if (!full) throw new Error(`no ${version} .lex found in ${dir} (searched recursively, found: ${lex.map(p => relative(dir, p)).join(', ') || 'none'})`);
  const lite = pickOne(matching.filter(isLite), `${version} lite .lex`, dir);
  const pom = pickOne(all.filter(p => basename(p) === '.flattened-pom.xml'), '.flattened-pom.xml', dir)
    || pickOne(all.filter(p => basename(p) === 'pom.xml'), 'pom.xml', dir);
  return { full, lite, pom };
}

async function client(accessKeyId, secretAccessKey, bucket, region) {
  let c = new S3Client({ region, credentials: { accessKeyId, secretAccessKey } });
  try { await c.send(new ListObjectsV2Command({ Bucket: bucket, MaxKeys: 1 })); return c; }
  catch (e) {
    if (e.name !== 'PermanentRedirect') throw e;
    const loc = await c.send(new GetBucketLocationCommand({ Bucket: bucket }));
    return new S3Client({ region: loc.LocationConstraint || 'us-east-1', credentials: { accessKeyId, secretAccessKey } });
  }
}

async function getText(c, bucket, key) {
  try { return await (await c.send(new GetObjectCommand({ Bucket: bucket, Key: key }))).Body.transformToString(); }
  catch (e) { if (e.name === 'NoSuchKey' || e.$metadata?.httpStatusCode === 404) return null; throw e; }
}

async function run() {
  const version = (process.env.INPUT_VERSION || '').trim();
  const dir = process.env.INPUT_DIR || '.';
  const dryRun = process.env.INPUT_DRY_RUN === 'true';
  const bucket = process.env.INPUT_S3_BUCKET;
  const region = process.env.INPUT_S3_REGION || 'us-east-1';
  if (!/^[0-9]+(\.[0-9]+){3}(-[A-Za-z0-9]+)*$/.test(version)) throw new Error(`[${version}] is not a valid extension version`);
  if (!bucket) throw new Error('INPUT_S3_BUCKET is required');

  const d = decide(version);
  const r2 = r2Config(process.env);
  const toS3 = d.upload && (dryRun || !!process.env.INPUT_S3_ACCESS_KEY);
  if (!d.upload) log(d.reason);
  else if (!toS3) log('no S3 credentials, not uploading to S3', 'WARN');
  if (!toS3 && !r2.enabled) { log('nothing to upload'); return; }

  const found = findInputs(dir, version);
  // the flattened POM tells us the artifactId, unless the caller passes one
  let pom = found.pom ? readFileSync(found.pom, 'utf8') : null;
  let artifactId = (process.env.INPUT_ARTIFACT_ID || '').trim();
  if (!artifactId && pom) artifactId = (pom.replace(/<parent>[\s\S]*?<\/parent>/, '').match(/<artifactId>([^<]+)<\/artifactId>/) || [])[1];
  if (!artifactId) throw new Error('could not resolve artifactId (pass artifact-id or include .flattened-pom.xml)');
  if (pom) pom = pom.replace(/\$\{revision\}/g, version);

  // map built files to Maven names
  const files = [{ src: found.full, name: `${artifactId}-${version}.lex`, type: 'application/octet-stream' }];
  if (found.lite) files.push({ src: found.lite, name: `${artifactId}-${version}-lite.lex`, type: 'application/octet-stream' });
  files.forEach(f => log(`using ${relative(dir, f.src)} as ${f.name}`));
  if (found.pom) log(`using ${relative(dir, found.pom)} as POM`);

  const base = `${DEFAULT_ORG_LUCEE_PREFIX}${artifactId}/`;
  const vbase = `${base}${version}/`;
  const ts = mavenTimestamp();
  const puts = [];

  // pre-releases are not on Maven Central, so the extension's own jar(s) the lite .lex points to have to go next to it
  const fullEntries = isPreRelease(version) ? readZip(readFileSync(found.full)) : null;
  const ownJars = fullEntries ? planOwnJars(fullEntries, found.lite ? readZip(readFileSync(found.lite)) : null, version, artifactId) : [];
  const extraArtifacts = [];
  const versionExts = ['lex', 'pom'];
  for (const j of ownJars) {
    const jbase = `${DEFAULT_ORG_LUCEE_PREFIX}${j.artifactId}/`;
    const jvbase = `${jbase}${version}/`;
    const shared = j.artifactId === artifactId;
    const items = [{ name: `${j.artifactId}-${version}.jar`, body: fullEntries.get(j.jar)(), type: 'application/java-archive' }];
    // an own jar with the same artifactId as the extension shares its version folder, POM and metadata
    if (shared) log(`org.lucee:${j.artifactId}:${version} has the extension's artifactId, uploading the jar next to the .lex`);
    else if (j.pom) items.push({ name: `${j.artifactId}-${version}.pom`, body: fullEntries.get(j.pom)(), type: 'application/xml' });
    else log(`no POM for org.lucee:${j.artifactId}:${version} in the full .lex, uploading the jar only`, 'WARN');
    log(`using ${j.jar} from the full .lex as ${jvbase}${items[0].name}`);
    for (const it of items) {
      puts.push({ key: jvbase + it.name, body: it.body, type: it.type });
      puts.push({ key: jvbase + it.name + '.sha1', body: createHash('sha1').update(it.body).digest('hex'), type: 'text/plain' });
    }
    if (shared) { versionExts.splice(1, 0, 'jar'); continue; }
    puts.push({ key: `${jvbase}maven-metadata.xml`, body: versionMetadataXml(j.artifactId, version, ts, j.pom ? ['jar', 'pom'] : ['jar']), type: 'application/xml' });
    extraArtifacts.push(j.artifactId);
  }
  for (const f of files) {
    const body = readFileSync(f.src);
    puts.push({ key: vbase + f.name, body, type: f.type });
    puts.push({ key: vbase + f.name + '.sha1', body: createHash('sha1').update(body).digest('hex'), type: 'text/plain' });
  }
  if (pom) {
    puts.push({ key: `${vbase}${artifactId}-${version}.pom`, body: pom, type: 'application/xml' });
    puts.push({ key: `${vbase}${artifactId}-${version}.pom.sha1`, body: createHash('sha1').update(pom).digest('hex'), type: 'text/plain' });
  } else log('no POM found, uploading the .lex only', 'WARN');
  puts.push({ key: `${vbase}maven-metadata.xml`, body: versionMetadataXml(artifactId, version, ts, versionExts), type: 'application/xml' });

  if (dryRun) {
    if (toS3) {
      puts.forEach(p => log(`DRY RUN would upload ${bucket}/${p.key}`));
      extraArtifacts.forEach(a => log(`DRY RUN would update ${DEFAULT_ORG_LUCEE_PREFIX}${a}/maven-metadata.xml`));
      log(`DRY RUN would update ${base}maven-metadata.xml and the group metadata`);
    }
    if (r2.enabled) puts.forEach(p => log(`DRY RUN would upload to R2 ${r2.bucket}/${r2Key(r2, p.key, version)}`));
    return;
  }

  // S3 and R2 are independent: a failure of one does not stop the other, but fails the run
  const errors = [];
  if (toS3) {
    try { await publishToS3({ bucket, region, puts, base, artifactId, version, ts, extraArtifacts }); }
    catch (e) { log(`S3 upload failed: ${e.message}`, 'ERROR'); errors.push(e); }
  }
  if (r2.enabled) {
    try { await uploadToR2(r2, version, puts, log); }
    catch (e) { log(`R2 upload failed: ${e.message}`, 'ERROR'); errors.push(e); }
  }
  if (errors.length) throw errors[0];
}

async function publishToS3({ bucket, region, puts, base, artifactId, version, ts, extraArtifacts }) {
  const c = await client(process.env.INPUT_S3_ACCESS_KEY, process.env.INPUT_S3_SECRET_KEY, bucket, region);
  for (const p of puts) {
    await c.send(new PutObjectCommand({ Bucket: bucket, Key: p.key, Body: p.body, ContentType: p.type }));
    log(`uploaded ${p.key}`);
  }
  const existing = await getText(c, bucket, `${base}maven-metadata.xml`);
  await c.send(new PutObjectCommand({ Bucket: bucket, Key: `${base}maven-metadata.xml`, Body: artifactMetadataXml(artifactId, existing, version, ts), ContentType: 'application/xml' }));
  log(`updated ${base}maven-metadata.xml`);
  for (const a of extraArtifacts) {
    const key = `${DEFAULT_ORG_LUCEE_PREFIX}${a}/maven-metadata.xml`;
    await c.send(new PutObjectCommand({ Bucket: bucket, Key: key, Body: artifactMetadataXml(a, await getText(c, bucket, key), version, ts), ContentType: 'application/xml' }));
    log(`updated ${key}`);
  }

  // Group metadata is a full rebuild from S3. GitHub concurrency groups are per repository, so two extension repos can
  // still rebuild at the same moment; re-check that our artifact made it in and rebuild again if a parallel run overwrote it.
  for (let i = 1; i <= 3; i++) {
    const r = await rebuildCdnGroupIndex(c, bucket);
    log(`rebuilt ${GROUP_METADATA_KEY} (${r.artifactCount} extensions), attempt ${i}`);
    await new Promise(res => setTimeout(res, 5000 + Math.floor(Math.random() * 10000)));
    const g = await getText(c, bucket, GROUP_METADATA_KEY) || '';
    if (!artifactId.endsWith('-extension') || g.includes(`<artifactId>${artifactId}</artifactId>`)) return;
  }
  log(`${artifactId} still missing in ${GROUP_METADATA_KEY}, run "Rebuild CDN Group Metadata"`, 'WARN');
}

if (process.argv[1] && basename(process.argv[1]) === 'upload-extension-to-s3.js') run().catch(e => { log(e.message, 'ERROR'); process.exit(1); });
