// LDEV-6711: publishes the Lucee core build to the lucee-downloads bucket (cdn.lucee.org / maven.lucee-services.com), after the Maven build.
// Before this was done by ant/upload_to_s3.cfm with a running Lucee, inside the build.
//   org/lucee/lucee/<version>/lucee-<version>.jar, -light.jar, -zero.jar, .pom (+ .sha1)
//   org/lucee/lucee/<version>/maven-metadata.xml
//   org/lucee/lucee/maven-metadata.xml
// and, until the cut-off (see S3_CUTOFF), for final/RC/BETA builds the legacy names in the root of the bucket:
//   lucee-<version>.jar, lucee-light-<version>.jar, lucee-zero-<version>.jar
// The group index (org/lucee/index.html, org/lucee/maven-metadata.xml) is regenerated afterwards by rebuild-group-metadata.js.
// A build is only published once (LDEV-3921), the script does nothing when the jar of that version is already there.
//
// env: INPUT_VERSION, INPUT_DIR (folder with the jars and the pom), INPUT_S3_BUCKET, INPUT_S3_REGION, INPUT_S3_ACCESS_KEY, INPUT_S3_SECRET_KEY,
//      INPUT_DRY_RUN=true (only log what would be uploaded), FORCE_RELEASE (alpha versions are only published when true)
import { S3Client, ListObjectsV2Command, GetBucketLocationCommand, PutObjectCommand, GetObjectCommand, HeadObjectCommand } from '@aws-sdk/client-s3';
import { createHash } from 'node:crypto';
import { readFileSync, existsSync, appendFileSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { mavenTimestamp, xmlEscape } from './maven-group-metadata.js';
import { artifactMetadataXml } from './upload-extension-to-s3.js';

// The one place the core cut-off is defined (the same date as in upload-extension-to-s3.js, LDEV-6537): from then on SNAPSHOTs are not
// uploaded to S3 at all (they live on Sonatype snapshots) and the bucket root is no longer written.
export const S3_CUTOFF = new Date('2027-03-01T00:00:00Z');

const log = (m, l = 'INFO') => console.log(`[${new Date().toISOString()}] [${l}] ${m}`);

export const isSnapshot = v => /-SNAPSHOT$/i.test(v);
export const isAlpha = v => /-ALPHA/i.test(v);

// What happens with a version. Same rules as ant/upload_to_s3.cfm:
//   alpha versions are only published when released explicitly (FORCE_RELEASE, LDEV-6708) and then only in the Maven layout
//   SNAPSHOTs are not published after the cut-off
//   the bucket root (legacy names) is written until the cut-off, not for alpha versions
export function decide(version, { deploy = true, forceRelease = false, now = new Date() } = {}) {
	if (!deploy) return { publish: false, root: false, reason: 'DO_DEPLOY is false' };
	if (isAlpha(version) && !forceRelease) return { publish: false, root: false, reason: 'alpha versions are only published with [release] in the commit message (LDEV-6708)' };
	const retired = now >= S3_CUTOFF;
	if (isSnapshot(version) && retired) return { publish: false, root: false, reason: `SNAPSHOT not uploaded to S3 since ${S3_CUTOFF.toISOString().slice(0, 10)} (only on Sonatype, LDEV-6537)` };
	return { publish: true, root: !retired && !isAlpha(version) };
}

// maven-metadata.xml of the version folder, the classifiers light and zero are listed as snapshotVersions (as before)
export function versionMetadataXml(version, ts) {
	const entry = classifier => `      <snapshotVersion>
${classifier ? `        <classifier>${xmlEscape(classifier)}</classifier>\n` : ''}        <extension>jar</extension>
        <value>${xmlEscape(version)}</value>
        <updated>${ts}</updated>
      </snapshotVersion>`;
	const pom = `      <snapshotVersion>
        <extension>pom</extension>
        <value>${xmlEscape(version)}</value>
        <updated>${ts}</updated>
      </snapshotVersion>`;
	return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <version>${xmlEscape(version)}</version>
  <versioning>
    <lastUpdated>${ts}</lastUpdated>
    <snapshotVersions>
${[entry(''), entry('light'), entry('zero'), pom].join('\n')}
    </snapshotVersions>
  </versioning>
</metadata>`;
}

// The files that go to S3: [{ src, key, type }]. dir holds lucee-<v>.jar, lucee-light-<v>.jar, lucee-zero-<v>.jar and the flattened pom.
export function plan(version, dir, root) {
	const base = `org/lucee/lucee/${version}/`;
	const jar = join(dir, `lucee-${version}.jar`);
	const light = join(dir, `lucee-light-${version}.jar`);
	const zero = join(dir, `lucee-zero-${version}.jar`);
	const pom = [join(dir, '.flattened-pom.xml'), join(dir, 'lucee-' + version + '.pom'), join(dir, '..', '.flattened-pom.xml')].find(existsSync);
	const items = [
		{ src: jar, key: `${base}lucee-${version}.jar`, type: 'application/java-archive' },
		{ src: light, key: `${base}lucee-${version}-light.jar`, type: 'application/java-archive' },
		{ src: zero, key: `${base}lucee-${version}-zero.jar`, type: 'application/java-archive' }
	];
	if (root) {
		items.push({ src: jar, key: `lucee-${version}.jar`, type: 'application/java-archive', sha1: false });
		items.push({ src: light, key: `lucee-light-${version}.jar`, type: 'application/java-archive', sha1: false });
		items.push({ src: zero, key: `lucee-zero-${version}.jar`, type: 'application/java-archive', sha1: false });
	}
	if (pom) items.push({ src: pom, key: `${base}lucee-${version}.pom`, type: 'application/xml', replaceRevision: version });
	return items;
}

async function client(accessKeyId, secretAccessKey, bucket, region) {
	const c = new S3Client({ region, credentials: { accessKeyId, secretAccessKey } });
	try { await c.send(new ListObjectsV2Command({ Bucket: bucket, MaxKeys: 1 })); return c; }
	catch (e) {
		if (e.name !== 'PermanentRedirect') throw e;
		const loc = await c.send(new GetBucketLocationCommand({ Bucket: bucket }));
		return new S3Client({ region: loc.LocationConstraint || 'us-east-1', credentials: { accessKeyId, secretAccessKey } });
	}
}

async function exists(c, bucket, key) {
	try { await c.send(new HeadObjectCommand({ Bucket: bucket, Key: key })); return true; }
	catch (e) { if (e.name === 'NotFound' || e.$metadata?.httpStatusCode === 404) return false; throw e; }
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
	if (!/^[0-9]+(\.[0-9]+){3}(-[A-Za-z0-9]+)*$/.test(version)) throw new Error(`[${version}] is not a valid Lucee version`);

	const d = decide(version, { deploy: process.env.DO_DEPLOY !== 'false', forceRelease: process.env.FORCE_RELEASE === 'true' });
	if (!d.publish) { log(`not publishing ${version} to S3: ${d.reason}`); return; }
	if (!bucket) throw new Error('INPUT_S3_BUCKET is required');

	const items = plan(version, dir, d.root);
	for (const it of items) if (!existsSync(it.src)) throw new Error(`missing file ${it.src}`);
	if (!items.some(i => i.key.endsWith('.pom'))) log('no flattened pom found, no pom is uploaded', 'WARN');

	const ts = mavenTimestamp();
	const puts = [];
	for (const it of items) {
		let body = readFileSync(it.src);
		if (it.replaceRevision) body = Buffer.from(body.toString('utf8').replace(/\$\{revision\}/g, it.replaceRevision));
		puts.push({ key: it.key, body, type: it.type });
		if (it.sha1 !== false) puts.push({ key: it.key + '.sha1', body: createHash('sha1').update(body).digest('hex'), type: 'text/plain' });
	}
	puts.push({ key: `org/lucee/lucee/${version}/maven-metadata.xml`, body: versionMetadataXml(version, ts), type: 'application/xml' });

	if (dryRun) {
		puts.forEach(p => log(`[dry run] would upload ${p.key} (${Buffer.byteLength(p.body)} bytes)`));
		log('[dry run] would update org/lucee/lucee/maven-metadata.xml');
		return;
	}

	const c = await client(process.env.INPUT_S3_ACCESS_KEY, process.env.INPUT_S3_SECRET_KEY, bucket, region);
	if (await exists(c, bucket, `org/lucee/lucee/${version}/lucee-${version}.jar`)) {
		log(`${version} was already published to S3, nothing to do (LDEV-3921)`);
		return;
	}
	for (const p of puts) {
		await c.send(new PutObjectCommand({ Bucket: bucket, Key: p.key, Body: p.body, ContentType: p.type }));
		log(`uploaded ${p.key}`);
	}
	const key = 'org/lucee/lucee/maven-metadata.xml';
	await c.send(new PutObjectCommand({ Bucket: bucket, Key: key, Body: artifactMetadataXml('lucee', await getText(c, bucket, key), version, ts), ContentType: 'application/xml' }));
	log(`updated ${key}`);

	// main.yml regenerates org/lucee/index.html + org/lucee/maven-metadata.xml only when this is set
	if (process.env.GITHUB_ENV) appendFileSync(process.env.GITHUB_ENV, 'S3_PUBLISHED=true\n');
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
	run().catch(e => { console.error(`::error::${e.message}`); process.exit(1); });
}
