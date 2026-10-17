// Uploads one extension build into the Maven layout of the lucee-downloads bucket (cdn.lucee.org):
//   org/lucee/<artifactId>/<version>/<artifactId>-<version>.lex (+ -lite.lex, .pom, .sha1)
//   org/lucee/<artifactId>/<version>/maven-metadata.xml
//   org/lucee/<artifactId>/maven-metadata.xml
//   org/lucee/maven-metadata.xml + org/lucee/index.html (group metadata)
// Called by .github/workflows/extension-s3-upload.yml (workflow_call) from every extension build (LDEV-XXXX).
import { S3Client, ListObjectsV2Command, GetBucketLocationCommand, PutObjectCommand, GetObjectCommand } from '@aws-sdk/client-s3';
import { createHash } from 'node:crypto';
import { readFileSync, readdirSync } from 'node:fs';
import { join, basename, relative } from 'node:path';
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

export function versionMetadataXml(artifactId, version, ts) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>${xmlEscape(artifactId)}</artifactId>
  <version>${xmlEscape(version)}</version>
  <versioning>
    <lastUpdated>${ts}</lastUpdated>
    <snapshotVersions>
      <snapshotVersion>
        <extension>lex</extension>
        <value>${xmlEscape(version)}</value>
        <updated>${ts}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <extension>pom</extension>
        <value>${xmlEscape(version)}</value>
        <updated>${ts}</updated>
      </snapshotVersion>
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
  if (!d.upload) { log(d.reason); return; }

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
  for (const f of files) {
    const body = readFileSync(f.src);
    puts.push({ key: vbase + f.name, body, type: f.type });
    puts.push({ key: vbase + f.name + '.sha1', body: createHash('sha1').update(body).digest('hex'), type: 'text/plain' });
  }
  if (pom) {
    puts.push({ key: `${vbase}${artifactId}-${version}.pom`, body: pom, type: 'application/xml' });
    puts.push({ key: `${vbase}${artifactId}-${version}.pom.sha1`, body: createHash('sha1').update(pom).digest('hex'), type: 'text/plain' });
  } else log('no POM found, uploading the .lex only', 'WARN');
  puts.push({ key: `${vbase}maven-metadata.xml`, body: versionMetadataXml(artifactId, version, ts), type: 'application/xml' });

  if (dryRun) { puts.forEach(p => log(`DRY RUN would upload ${bucket}/${p.key}`)); log(`DRY RUN would update ${base}maven-metadata.xml and the group metadata`); return; }

  const c = await client(process.env.INPUT_S3_ACCESS_KEY, process.env.INPUT_S3_SECRET_KEY, bucket, region);
  for (const p of puts) {
    await c.send(new PutObjectCommand({ Bucket: bucket, Key: p.key, Body: p.body, ContentType: p.type }));
    log(`uploaded ${p.key}`);
  }
  const existing = await getText(c, bucket, `${base}maven-metadata.xml`);
  await c.send(new PutObjectCommand({ Bucket: bucket, Key: `${base}maven-metadata.xml`, Body: artifactMetadataXml(artifactId, existing, version, ts), ContentType: 'application/xml' }));
  log(`updated ${base}maven-metadata.xml`);

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
