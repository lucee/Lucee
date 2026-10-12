import test from 'node:test';
import assert from 'node:assert/strict';
import { decide, S3_CUTOFF, artifactMetadataXml } from './upload-extension-to-s3.js';

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
