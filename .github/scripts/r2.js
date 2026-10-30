// Cloudflare R2 mirror of the Maven-layout uploads (LDEV-6804).
// R2 is S3 compatible, so the same SDK is used with a custom endpoint. The Maven bridge worker (lucee-data-provider,
// apps/maven-bridge-worker) serves from this bucket, so the keys have to be exactly where the worker looks:
//   <R2_PREFIX><maven path>                         releases, RC, BETA, ALPHA
//   <R2_PREFIX><R2_SNAPSHOT_PREFIX><maven path>     SNAPSHOTs (own prefix so a lifecycle rule can expire them)
// Nothing is hardcoded: endpoint, bucket, prefixes and credentials come from the environment (org variables / secrets).
import { S3Client, PutObjectCommand } from '@aws-sdk/client-s3';

// same default as R2_SNAPSHOT_PREFIX in the worker's wrangler.toml; override with the R2_SNAPSHOT_PREFIX variable
export const DEFAULT_SNAPSHOT_PREFIX = 'snapshots/';

export function isSnapshotVersion(version) { return /-SNAPSHOT$/i.test(version); }

function dirPrefix(p) {
  const s = (p ?? '').trim().replace(/^\/+/, '');
  return s === '' || s.endsWith('/') ? s : s + '/';
}

// Reads the R2 settings from an env-like object.
//   none of endpoint/bucket/keys set -> { enabled: false }   (R2 not configured, the upload is skipped)
//   some but not all set             -> throws                (half configured is a mistake, never skipped silently)
//   all set                          -> { enabled: true, endpoint, bucket, accessKeyId, secretAccessKey, prefix, snapshotPrefix }
export function r2Config(env, p = 'R2_') {
  const g = k => (env[p + k] ?? '').trim();
  const required = { ENDPOINT: g('ENDPOINT'), BUCKET: g('BUCKET'), ACCESS_KEY_ID: g('ACCESS_KEY_ID'), SECRET_ACCESS_KEY: g('SECRET_ACCESS_KEY') };
  const missing = Object.keys(required).filter(k => !required[k]);
  if (missing.length === Object.keys(required).length) return { enabled: false };
  if (missing.length) throw new Error(`R2 is partly configured, missing: ${missing.map(k => p + k).join(', ')}`);
  const snapshotPrefix = env[p + 'SNAPSHOT_PREFIX'];
  return {
    enabled: true,
    endpoint: required.ENDPOINT,
    bucket: required.BUCKET,
    accessKeyId: required.ACCESS_KEY_ID,
    secretAccessKey: required.SECRET_ACCESS_KEY,
    prefix: dirPrefix(g('PREFIX')),
    snapshotPrefix: snapshotPrefix === undefined || snapshotPrefix.trim() === '' ? DEFAULT_SNAPSHOT_PREFIX : dirPrefix(snapshotPrefix)
  };
}

// Key in the R2 bucket for a Maven-layout key such as org/lucee/lucee/7.1.0.10/lucee-7.1.0.10.jar
export function r2Key(cfg, mavenKey, version) {
  return cfg.prefix + (isSnapshotVersion(version) ? cfg.snapshotPrefix : '') + mavenKey;
}

export function r2Client(cfg) {
  return new S3Client({
    region: 'auto',
    endpoint: cfg.endpoint,
    forcePathStyle: true,
    credentials: { accessKeyId: cfg.accessKeyId, secretAccessKey: cfg.secretAccessKey },
    // R2 does not support the additional checksums newer SDK versions send by default
    requestChecksumCalculation: 'WHEN_REQUIRED',
    responseChecksumValidation: 'WHEN_REQUIRED'
  });
}

// Uploads [{ key (Maven layout), body, type }]. Any failure throws, there is nothing swallowed here.
export async function uploadToR2(cfg, version, puts, log = console.log, client = r2Client(cfg)) {
  for (const p of puts) {
    const key = r2Key(cfg, p.key, version);
    await client.send(new PutObjectCommand({ Bucket: cfg.bucket, Key: key, Body: p.body, ContentType: p.type }));
    log(`uploaded to R2: ${key}`);
  }
}
