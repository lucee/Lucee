// Uploads one Lucee core build into the Maven layout of the Cloudflare R2 bucket (LDEV-6804), next to the S3 upload:
//   org/lucee/lucee/<version>/lucee-<version>.jar, -light.jar, -zero.jar, .lco, .pom (+ .sha1 of each), maven-metadata.xml
// Only the Maven layout is written, not the legacy bucket-root names (lucee-<version>.jar, lucee-light-<version>.jar, ...).
// The express zip and the war are only attached to the GitHub release, so they are not uploaded either.
// Called by the "Build" workflow (main.yml) once the version has been deployed. Settings come from the environment:
//   INPUT_VERSION, INPUT_DIR (loader/target), INPUT_POM (loader/.flattened-pom.xml), INPUT_DRY_RUN
//   R2_ENDPOINT, R2_BUCKET, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY, R2_PREFIX (optional), R2_SNAPSHOT_PREFIX (optional)
import { createHash } from 'node:crypto';
import { existsSync, readFileSync } from 'node:fs';
import { join, basename } from 'node:path';
import { r2Config, r2Key, uploadToR2 } from './r2.js';

const log = (m, l = 'INFO') => console.log(`[${new Date().toISOString()}] [${l}] ${m}`);

export const CORE_ARTIFACT = 'lucee';
export const CORE_PREFIX = 'org/lucee/lucee/';

function mavenTimestamp(d = new Date()) {
  const p = n => String(n).padStart(2, '0');
  return `${d.getUTCFullYear()}${p(d.getUTCMonth() + 1)}${p(d.getUTCDate())}${p(d.getUTCHours())}${p(d.getUTCMinutes())}${p(d.getUTCSeconds())}`;
}

export function versionMetadataXml(version, ts) {
  const exts = ['jar', 'lco', 'pom'];
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>${CORE_ARTIFACT}</artifactId>
  <version>${version}</version>
  <versioning>
    <lastUpdated>${ts}</lastUpdated>
    <snapshotVersions>
${exts.map(e => `      <snapshotVersion>
        <extension>${e}</extension>
        <value>${version}</value>
        <updated>${ts}</updated>
      </snapshotVersion>`).join('\n')}
    </snapshotVersions>
  </versioning>
</metadata>`;
}

// Build output name -> Maven name, the same files ant/upload_to_s3.cfm puts into org/lucee/lucee/<version>/
export function coreFiles(dir, pomFile, version) {
  const files = [
    { src: join(dir, `lucee-${version}.jar`), name: `lucee-${version}.jar`, type: 'application/java-archive' },
    { src: join(dir, `lucee-light-${version}.jar`), name: `lucee-${version}-light.jar`, type: 'application/java-archive' },
    { src: join(dir, `lucee-zero-${version}.jar`), name: `lucee-${version}-zero.jar`, type: 'application/java-archive' },
    { src: join(dir, `${version}.lco`), name: `lucee-${version}.lco`, type: 'application/octet-stream' },
    { src: pomFile, name: `lucee-${version}.pom`, type: 'application/xml' }
  ];
  const missing = files.filter(f => !f.src || !existsSync(f.src));
  if (missing.length) throw new Error(`build output missing: ${missing.map(f => f.src || f.name).join(', ')}`);
  return files;
}

export function buildPuts(files, version, ts = mavenTimestamp()) {
  const base = `${CORE_PREFIX}${version}/`;
  const puts = [];
  for (const f of files) {
    const body = readFileSync(f.src);
    puts.push({ key: base + f.name, body, type: f.type });
    puts.push({ key: base + f.name + '.sha1', body: createHash('sha1').update(body).digest('hex'), type: 'text/plain' });
  }
  puts.push({ key: `${base}maven-metadata.xml`, body: versionMetadataXml(version, ts), type: 'application/xml' });
  return puts;
}

async function run() {
  const version = (process.env.INPUT_VERSION || '').trim();
  if (!/^[0-9]+(\.[0-9]+){3}(-[A-Za-z0-9]+)*$/.test(version)) throw new Error(`[${version}] is not a valid Lucee version`);
  const cfg = r2Config(process.env);
  if (!cfg.enabled) throw new Error('R2 is not configured (R2_ENDPOINT, R2_BUCKET, R2_ACCESS_KEY_ID, R2_SECRET_ACCESS_KEY)');
  const dir = process.env.INPUT_DIR || 'loader/target';
  const pom = process.env.INPUT_POM || 'loader/.flattened-pom.xml';
  const files = coreFiles(dir, pom, version);
  files.forEach(f => log(`using ${f.src} as ${CORE_PREFIX}${version}/${f.name}`));
  const puts = buildPuts(files, version);
  if (process.env.INPUT_DRY_RUN === 'true') {
    puts.forEach(p => log(`DRY RUN would upload ${cfg.bucket}/${r2Key(cfg, p.key, version)}`));
    return;
  }
  await uploadToR2(cfg, version, puts, m => log(m));
  log(`uploaded ${puts.length} objects for ${version} to R2`);
}

if (process.argv[1] && basename(process.argv[1]) === 'upload-core-to-r2.js') run().catch(e => { log(e.message, 'ERROR'); process.exit(1); });
