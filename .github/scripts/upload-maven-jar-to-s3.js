// Uploads one jar that is not on a public Maven repository (for example an OSGi bundle a Lucee extension requires) into
// the Maven layout of the lucee-downloads bucket (cdn.lucee.org / maven.lucee-services.com):
//   org/lucee/<artifactId>/<version>/<artifactId>-<version>.jar (+ .pom, .sha1)
//   org/lucee/<artifactId>/<version>/maven-metadata.xml
//   org/lucee/<artifactId>/maven-metadata.xml
// The jar and the optional POM are downloaded from the given URLs. Without a POM URL the POM embedded in the jar
// (META-INF/maven/org.lucee/<artifactId>/pom.xml) is used, when there is one. A version that is already there is never overwritten.
// When R2_ENDPOINT, R2_BUCKET, R2_ACCESS_KEY_ID and R2_SECRET_ACCESS_KEY are set, the files are also uploaded to R2 (LDEV-6804).
import { PutObjectCommand } from '@aws-sdk/client-s3';
import { createHash } from 'node:crypto';
import { basename } from 'node:path';
import { r2Config, r2Key, uploadToR2 } from './r2.js';
import { DEFAULT_ORG_LUCEE_PREFIX, mavenTimestamp } from './maven-group-metadata.js';
import { artifactMetadataXml, versionMetadataXml, readZip, client, getText } from './upload-extension-to-s3.js';

const log = (m, l = 'INFO') => console.log(`[${new Date().toISOString()}] [${l}] ${m}`);
const sha1 = body => createHash('sha1').update(body).digest('hex');

export function validate(artifactId, version, jarUrl, pomUrl) {
  if (!/^[A-Za-z0-9][A-Za-z0-9._-]*$/.test(artifactId)) throw new Error(`[${artifactId}] is not a valid artifactId`);
  if (!/^[0-9][A-Za-z0-9._-]*$/.test(version)) throw new Error(`[${version}] is not a valid version`);
  if (/-SNAPSHOT$/i.test(version)) throw new Error(`[${version}] is a SNAPSHOT, only fixed versions are uploaded`);
  for (const [n, u] of [['jar_url', jarUrl], ['pom_url', pomUrl]]) {
    if (n === 'pom_url' && !u) continue;
    if (!/^https:\/\//.test(u || '')) throw new Error(`${n} has to be an https URL, got [${u}]`);
  }
}

// the POM embedded in a bundle built with maven-bundle-plugin, null when there is none
export function embeddedPom(jarBuffer, artifactId) {
  const entries = readZip(jarBuffer);
  const read = entries.get(`META-INF/maven/org.lucee/${artifactId}/pom.xml`);
  return read ? read().toString('utf8') : null;
}

// the coordinates of a POM, the parent is ignored
export function pomCoordinates(pom) {
  const own = pom.replace(/<parent>[\s\S]*?<\/parent>/, '').replace(/<(build|dependencies|dependencyManagement|profiles|properties)>[\s\S]*?<\/\1>/g, '');
  const tag = t => (own.match(new RegExp(`<${t}>([^<]+)</${t}>`)) || [])[1]?.trim();
  return { groupId: tag('groupId'), artifactId: tag('artifactId'), version: tag('version') };
}

// the POM has to describe the file that is uploaded, otherwise Maven ignores or rejects it
export function checkPom(pom, artifactId, version) {
  const c = pomCoordinates(pom);
  if (c.groupId !== 'org.lucee' || c.artifactId !== artifactId || c.version !== version) {
    throw new Error(`the POM is for [${c.groupId}:${c.artifactId}:${c.version}], not for [org.lucee:${artifactId}:${version}]`);
  }
}

// the Bundle-Version of a jar, null when it is not a bundle
export function bundleVersion(jarBuffer) {
  const read = readZip(jarBuffer).get('META-INF/MANIFEST.MF');
  if (!read) return null;
  const mf = read().toString('utf8').replace(/\r?\n /g, '');
  return (mf.match(/^Bundle-Version:\s*(.+?)\s*$/m) || [])[1] || null;
}

// the files of one upload, in Maven layout: jar, pom (optional) and the version metadata, each with a .sha1
export function planUpload({ artifactId, version, jar, pom, ts }) {
  const vbase = `${DEFAULT_ORG_LUCEE_PREFIX}${artifactId}/${version}/`;
  const puts = [{ key: `${vbase}${artifactId}-${version}.jar`, body: jar, type: 'application/java-archive' }];
  if (pom) puts.push({ key: `${vbase}${artifactId}-${version}.pom`, body: pom, type: 'application/xml' });
  for (const p of [...puts]) puts.push({ key: `${p.key}.sha1`, body: sha1(p.body), type: 'text/plain' });
  puts.push({ key: `${vbase}maven-metadata.xml`, body: versionMetadataXml(artifactId, version, ts, pom ? ['jar', 'pom'] : ['jar']), type: 'application/xml' });
  return puts;
}

async function download(url) {
  const res = await fetch(url, { redirect: 'follow' });
  if (!res.url.startsWith('https://')) throw new Error(`[${url}] was redirected to [${res.url}], which is not https`);
  if (!res.ok) throw new Error(`failed to download [${url}], status code ${res.status}`);
  return Buffer.from(await res.arrayBuffer());
}

async function run() {
  const artifactId = (process.env.INPUT_ARTIFACT_ID || '').trim();
  const version = (process.env.INPUT_VERSION || '').trim();
  const jarUrl = (process.env.INPUT_JAR_URL || '').trim();
  const pomUrl = (process.env.INPUT_POM_URL || '').trim();
  const dryRun = process.env.INPUT_DRY_RUN === 'true';
  const bucket = process.env.INPUT_S3_BUCKET;
  const region = process.env.INPUT_S3_REGION || 'us-east-1';
  if (!bucket) throw new Error('INPUT_S3_BUCKET is required');
  validate(artifactId, version, jarUrl, pomUrl);

  const jar = await download(jarUrl);
  const bv = bundleVersion(jar); // throws when it is not a zip
  if (bv && bv !== version) throw new Error(`the Bundle-Version of the jar is [${bv}], not [${version}]`);
  log(`downloaded ${jarUrl} (${jar.length} bytes, sha1 ${sha1(jar)})`);
  let pom = null;
  if (pomUrl) pom = (await download(pomUrl)).toString('utf8');
  else {
    pom = embeddedPom(jar, artifactId);
    log(pom ? 'using the POM embedded in the jar' : 'no POM given and none embedded in the jar, uploading the jar only', pom ? 'INFO' : 'WARN');
  }

  if (pom) checkPom(pom, artifactId, version);

  const ts = mavenTimestamp();
  const puts = planUpload({ artifactId, version, jar, pom, ts });
  const base = `${DEFAULT_ORG_LUCEE_PREFIX}${artifactId}/`;
  const r2 = r2Config(process.env);
  if (!dryRun && !process.env.INPUT_S3_ACCESS_KEY) throw new Error('no S3 credentials, they are needed to check that the version does not exist yet');

  // never overwrite: the public repositories (S3 and R2 are both behind them), the jar and the artifact metadata in S3
  const rel = `${base}${version}/${artifactId}-${version}.jar`;
  for (const host of ['https://maven.lucee-services.com/', 'https://cdn.lucee.org/']) {
    const res = await fetch(host + rel, { method: 'HEAD' });
    if (res.status === 200) throw new Error(`org.lucee:${artifactId}:${version} already exists at ${host}${rel}, it is not overwritten`);
    if (res.status !== 404) throw new Error(`could not check if ${host}${rel} exists, status code ${res.status}`);
  }
  let c = null;
  if (process.env.INPUT_S3_ACCESS_KEY) {
    c = await client(process.env.INPUT_S3_ACCESS_KEY, process.env.INPUT_S3_SECRET_KEY, bucket, region);
    const meta = await getText(c, bucket, `${base}maven-metadata.xml`);
    if (await getText(c, bucket, rel + '.sha1') !== null || (meta && meta.includes(`<version>${version}</version>`))) throw new Error(`org.lucee:${artifactId}:${version} already exists, it is not overwritten`);
  }
  else log('dry run without S3 credentials, the S3 metadata was not checked', 'WARN');
  if (dryRun) {
    puts.forEach(p => log(`DRY RUN would upload ${bucket}/${p.key}`));
    log(`DRY RUN would update ${base}maven-metadata.xml`);
    if (r2.enabled) puts.forEach(p => log(`DRY RUN would upload to R2 ${r2.bucket}/${r2Key(r2, p.key, version)}`));
    return;
  }

  const errors = [];
  if (c) {
    try {
      for (const p of puts) {
        await c.send(new PutObjectCommand({ Bucket: bucket, Key: p.key, Body: p.body, ContentType: p.type }));
        log(`uploaded ${p.key}`);
      }
      const key = `${base}maven-metadata.xml`;
      await c.send(new PutObjectCommand({ Bucket: bucket, Key: key, Body: artifactMetadataXml(artifactId, await getText(c, bucket, key), version, ts), ContentType: 'application/xml' }));
      log(`updated ${key}`);
    }
    catch (e) { log(`S3 upload failed: ${e.message}`, 'ERROR'); errors.push(e); }
  }
  if (r2.enabled) {
    try { await uploadToR2(r2, version, puts, log); }
    catch (e) { log(`R2 upload failed: ${e.message}`, 'ERROR'); errors.push(e); }
  }
  if (errors.length) throw errors[0];
}

if (process.argv[1] && basename(process.argv[1]) === 'upload-maven-jar-to-s3.js') run().catch(e => { log(e.message, 'ERROR'); process.exit(1); });
