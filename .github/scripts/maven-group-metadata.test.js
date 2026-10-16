import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  versionSortKey,
  sortVersions,
  findLatestRelease,
  buildGroupMetadataXml,
  parseArtifactMetadataXml,
  isExtensionArtifactId,
  mavenTimestamp
} from './maven-group-metadata.js';

describe('versionSortKey', () => {
  it('orders semver-like extension versions correctly', () => {
    const sorted = sortVersions(['8.0.9', '8.0.10', '8.0.2-SNAPSHOT', '8.0.2']);
    assert.deepEqual(sorted, ['8.0.2', '8.0.2-SNAPSHOT', '8.0.9', '8.0.10']);
  });

  it('places SNAPSHOT after its release line', () => {
    assert.ok(versionSortKey('1.0.0') < versionSortKey('1.0.0-SNAPSHOT'));
  });
});

describe('findLatestRelease', () => {
  it('returns highest non-SNAPSHOT version', () => {
    const sorted = sortVersions(['1.0.0-SNAPSHOT', '1.0.0', '0.9.9']);
    assert.equal(findLatestRelease(sorted), '1.0.0');
  });

  it('returns empty when only snapshots exist', () => {
    assert.equal(findLatestRelease(['2.0.0-SNAPSHOT']), '');
  });
});

describe('buildGroupMetadataXml', () => {
  it('matches GroupMetadataExtensionLister schema', () => {
    const xml = buildGroupMetadataXml('org.lucee', [
      { artifactId: 'mysql-jdbc-extension', latest: '8.0.35', release: '8.0.35' },
      { artifactId: 'redis-extension', latest: '4.0.1.1-SNAPSHOT', release: '' }
    ], '20260612213000');

    assert.match(xml, /<groupId>org\.lucee<\/groupId>/);
    assert.match(xml, /<artifactId>mysql-jdbc-extension<\/artifactId>/);
    assert.match(xml, /<latest>8\.0\.35<\/latest>/);
    assert.match(xml, /<release>8\.0\.35<\/release>/);
    assert.match(xml, /<artifactId>redis-extension<\/artifactId>/);
    assert.doesNotMatch(xml, /<release>4\.0\.1\.1-SNAPSHOT<\/release>/);
    assert.match(xml, /<lastUpdated>20260612213000<\/lastUpdated>/);
  });
});

describe('parseArtifactMetadataXml', () => {
  it('extracts and sorts versions from standard artifact metadata', () => {
    const xml = `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>mysql-jdbc-extension</artifactId>
  <versioning>
    <latest>8.0.10</latest>
    <release>8.0.10</release>
    <versions>
      <version>8.0.9</version>
      <version>8.0.10</version>
    </versions>
  </versioning>
</metadata>`;

    const parsed = parseArtifactMetadataXml(xml);
    assert.deepEqual(parsed.versions, ['8.0.9', '8.0.10']);
    assert.equal(parsed.latest, '8.0.10');
    assert.equal(parsed.release, '8.0.10');
  });
});

describe('isExtensionArtifactId', () => {
  it('accepts only -extension suffix', () => {
    assert.equal(isExtensionArtifactId('mysql-jdbc-extension'), true);
    assert.equal(isExtensionArtifactId('lucee'), false);
  });
});

describe('mavenTimestamp', () => {
  it('formats UTC timestamp as YYYYMMDDHHmmss', () => {
    const ts = mavenTimestamp(new Date('2026-06-12T21:30:00Z'));
    assert.equal(ts, '20260612213000');
  });
});
