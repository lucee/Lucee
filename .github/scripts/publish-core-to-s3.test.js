import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { decide, plan, versionMetadataXml, S3_CUTOFF } from './publish-core-to-s3.js';

const before = new Date(S3_CUTOFF.getTime() - 86400000);
const after = new Date(S3_CUTOFF.getTime() + 86400000);

test('nothing is published without DO_DEPLOY', () => {
	assert.equal(decide('9.0.0.5', { deploy: false, now: before }).publish, false);
});

test('alpha versions are only published when released explicitly, and only in the Maven layout', () => {
	assert.equal(decide('9.0.0.0-ALPHA', { now: before }).publish, false);
	const d = decide('9.0.0.0-ALPHA', { forceRelease: true, now: before });
	assert.deepEqual([d.publish, d.root], [true, false]);
});

test('snapshots are published until the cut-off, with the legacy names in the bucket root', () => {
	assert.deepEqual(decide('9.0.0.1-SNAPSHOT', { now: before }), { publish: true, root: true });
	assert.equal(decide('9.0.0.1-SNAPSHOT', { now: after }).publish, false);
});

test('releases are published after the cut-off, but not in the bucket root', () => {
	assert.deepEqual(decide('9.0.0.5-RC', { now: after }), { publish: true, root: false });
	assert.deepEqual(decide('9.0.0.5', { now: before }), { publish: true, root: true });
});

test('plan: Maven layout names, legacy names only for root', () => {
	const dir = mkdtempSync(join(tmpdir(), 'core-s3-'));
	writeFileSync(join(dir, '.flattened-pom.xml'), '<project/>');
	const mvn = plan('9.0.0.5', dir, false).map(i => i.key);
	assert.deepEqual(mvn, [
		'org/lucee/lucee/9.0.0.5/lucee-9.0.0.5.jar',
		'org/lucee/lucee/9.0.0.5/lucee-9.0.0.5-light.jar',
		'org/lucee/lucee/9.0.0.5/lucee-9.0.0.5-zero.jar',
		'org/lucee/lucee/9.0.0.5/lucee-9.0.0.5.pom'
	]);
	const withRoot = plan('9.0.0.5', dir, true).map(i => i.key);
	for (const k of ['lucee-9.0.0.5.jar', 'lucee-light-9.0.0.5.jar', 'lucee-zero-9.0.0.5.jar']) assert.ok(withRoot.includes(k), k);
	assert.ok(!withRoot.some(k => k.endsWith('.lco')), 'there is no .lco anymore');
});

test('version metadata lists jar, light, zero and pom, no lco', () => {
	const xml = versionMetadataXml('9.0.0.5', '20270101000000');
	assert.match(xml, /<classifier>light<\/classifier>/);
	assert.match(xml, /<classifier>zero<\/classifier>/);
	assert.match(xml, /<extension>pom<\/extension>/);
	assert.doesNotMatch(xml, /lco/);
});
