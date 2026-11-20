import test from 'node:test';
import assert from 'node:assert/strict';
import { deflateRawSync, crc32 } from 'node:zlib';
import { validate, planUpload, embeddedPom, pomCoordinates, checkPom, bundleVersion } from './upload-maven-jar-to-s3.js';

// a zip with stored/deflated entries, enough for readZip
function zip(files) {
  const locals = [], centrals = [];
  let offset = 0;
  for (const [name, text] of Object.entries(files)) {
    const nameBuf = Buffer.from(name), raw = Buffer.from(text), data = deflateRawSync(raw);
    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0); local.writeUInt16LE(8, 8); local.writeUInt32LE(crc32(raw), 14);
    local.writeUInt32LE(data.length, 18); local.writeUInt32LE(raw.length, 22); local.writeUInt16LE(nameBuf.length, 26);
    const central = Buffer.alloc(46);
    central.writeUInt32LE(0x02014b50, 0); central.writeUInt16LE(8, 10); central.writeUInt32LE(crc32(raw), 16);
    central.writeUInt32LE(data.length, 20); central.writeUInt32LE(raw.length, 24); central.writeUInt16LE(nameBuf.length, 28);
    central.writeUInt32LE(offset, 42);
    locals.push(local, nameBuf, data); centrals.push(central, nameBuf);
    offset += 30 + nameBuf.length + data.length;
  }
  const cd = Buffer.concat(centrals);
  const eocd = Buffer.alloc(22);
  eocd.writeUInt32LE(0x06054b50, 0); eocd.writeUInt16LE(Object.keys(files).length, 10); eocd.writeUInt32LE(cd.length, 12); eocd.writeUInt32LE(offset, 16);
  return Buffer.concat([...locals, cd, eocd]);
}

test('validate accepts a bundle version with a numeric qualifier and rejects bad input', () => {
  validate('spymemcached', '2.12.3.0001', 'https://example.org/a.jar', '');
  assert.throws(() => validate('../x', '1.0.0', 'https://example.org/a.jar', ''));
  assert.throws(() => validate('a', '1 0', 'https://example.org/a.jar', ''));
  assert.throws(() => validate('a', '1.0.0', 'http://example.org/a.jar', ''));
  assert.throws(() => validate('a', '1.0.0', 'https://example.org/a.jar', 'file:///etc/passwd'));
});
test('the upload plan has jar, pom, checksums and version metadata in Maven layout', () => {
  const puts = planUpload({ artifactId: 'spymemcached', version: '2.12.3.0001', jar: Buffer.from('jar'), pom: '<project/>', ts: '20261120000000' });
  assert.deepEqual(puts.map(p => p.key), [
    'org/lucee/spymemcached/2.12.3.0001/spymemcached-2.12.3.0001.jar',
    'org/lucee/spymemcached/2.12.3.0001/spymemcached-2.12.3.0001.pom',
    'org/lucee/spymemcached/2.12.3.0001/spymemcached-2.12.3.0001.jar.sha1',
    'org/lucee/spymemcached/2.12.3.0001/spymemcached-2.12.3.0001.pom.sha1',
    'org/lucee/spymemcached/2.12.3.0001/maven-metadata.xml'
  ]);
  assert.match(puts[4].body, /<extension>pom<\/extension>/);
});
test('without a pom only the jar is listed', () => {
  const puts = planUpload({ artifactId: 'a', version: '1.0.0', jar: Buffer.from('jar'), pom: null, ts: '20261120000000' });
  assert.equal(puts.length, 3);
  assert.doesNotMatch(puts[2].body, /<extension>pom<\/extension>/);
});
test('the embedded pom is read from the jar', () => {
  const jar = zip({ 'META-INF/MANIFEST.MF': 'x', 'META-INF/maven/org.lucee/spymemcached/pom.xml': '<project>embedded</project>' });
  assert.equal(embeddedPom(jar, 'spymemcached'), '<project>embedded</project>');
  assert.equal(embeddedPom(jar, 'other'), null);
});
test('a SNAPSHOT version is rejected', () => {
  assert.throws(() => validate('a', '1.0.0-SNAPSHOT', 'https://example.org/a.jar', ''), /SNAPSHOT/);
});
test('the pom has to be for the uploaded coordinates', () => {
  const pom = v => `<project><parent><groupId>x</groupId><artifactId>p</artifactId><version>9</version></parent><groupId>org.lucee</groupId><artifactId>spymemcached</artifactId><version>${v}</version><dependencies><dependency><groupId>g</groupId><artifactId>a</artifactId><version>7</version></dependency></dependencies></project>`;
  assert.deepEqual(pomCoordinates(pom('2.12.3.0001')), { groupId: 'org.lucee', artifactId: 'spymemcached', version: '2.12.3.0001' });
  checkPom(pom('2.12.3.0001'), 'spymemcached', '2.12.3.0001');
  assert.throws(() => checkPom(pom('2.12.3'), 'spymemcached', '2.12.3.0001'), /2\.12\.3/);
  assert.throws(() => checkPom(pom('2.12.3.0001'), 'other', '2.12.3.0001'));
});
test('the Bundle-Version is read from the manifest, also over a folded line', () => {
  assert.equal(bundleVersion(zip({ 'META-INF/MANIFEST.MF': 'Manifest-Version: 1.0\r\nBundle-Version: 2.12.\r\n 3.0001\r\n' })), '2.12.3.0001');
  assert.equal(bundleVersion(zip({ 'a.txt': 'x' })), null);
});
