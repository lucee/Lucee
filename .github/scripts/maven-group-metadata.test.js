import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import {
  compareVersions,
  sortVersions,
  findLatestRelease,
  buildGroupMetadataXml,
  parseArtifactMetadataXml,
  isExtensionArtifactId,
  mavenTimestamp
} from './maven-group-metadata.js';

describe('sortVersions (Maven ComparableVersion order)', () => {
  it('orders semver-like extension versions numerically', () => {
    const sorted = sortVersions(['8.0.9', '8.0.10', '8.0.2-SNAPSHOT', '8.0.2']);
    assert.deepEqual(sorted, ['8.0.2-SNAPSHOT', '8.0.2', '8.0.9', '8.0.10']);
  });

  it('places a SNAPSHOT before its release', () => {
    assert.ok(compareVersions('1.0.0-SNAPSHOT', '1.0.0') < 0);
    assert.ok(compareVersions('7.1.3.0-SNAPSHOT', '7.1.2.191') > 0);
  });

  it('orders qualifiers alpha < beta < milestone < rc < snapshot < release < sp', () => {
    const order = ['1.0-alpha', '1.0-beta', '1.0-milestone', '1.0-rc', '1.0-SNAPSHOT', '1.0', '1.0-sp'];
    assert.deepEqual(sortVersions([...order].reverse()), order);
    assert.equal(compareVersions('1.0-cr', '1.0-RC'), 0);
    assert.equal(compareVersions('1.0-final', '1.0'), 0);
    assert.ok(compareVersions('1.0-a1', '1.0-b1') < 0);
    assert.ok(compareVersions('1.0-rc1', '1.0-rc2') < 0);
  });

  it('compares numbers beyond 5 digits and leading zeros numerically', () => {
    assert.ok(compareVersions('9.0.0.1435-ALPHA', '9.0.0.999-ALPHA') > 0);
    assert.ok(compareVersions('1.0.123456', '1.0.99999') > 0);
    assert.equal(compareVersions('1.01', '1.1'), 0);
  });

  it('orders Lucee core versions like Maven does', () => {
    const expected = [
      '5.0.0.60-BETA',
      '5.0.0.98-BETA',
      '5.0.0.219-RC',
      '5.0.0.252',
      '5.2.2.71-RC',
      '5.2.2.71',
      '5.3.2.77',
      '5.3.10.97',
      '6.2.10.1-SNAPSHOT',
      '7.1.2.191',
      '7.1.2.192-RC',
      '7.1.2.194-RC',
      '7.1.3.0-SNAPSHOT',
      '7.1.3.138-SNAPSHOT',
      '8.0.0.304-RC',
      '8.0.0.304',
      '8.0.0.307-RC',
      '8.0.1.88-SNAPSHOT',
      '9.0.0.624-ALPHA',
      '9.0.0.1435-ALPHA'
    ];
    // publish order, as the old writers appended it
    const published = ['5.0.0.252', '5.0.0.60-BETA', '5.0.0.219-RC', '5.3.10.97', '5.0.0.98-BETA', '5.2.2.71', '5.2.2.71-RC',
      '5.3.2.77', '9.0.0.624-ALPHA', '8.0.0.304', '7.1.2.191', '8.0.0.304-RC', '7.1.3.0-SNAPSHOT', '9.0.0.1435-ALPHA',
      '7.1.2.192-RC', '8.0.1.88-SNAPSHOT', '6.2.10.1-SNAPSHOT', '7.1.3.138-SNAPSHOT', '7.1.2.194-RC', '8.0.0.307-RC'];
    assert.deepEqual(sortVersions(published), expected);
  });

  it('extension version forms', () => {
    assert.deepEqual(
      sortVersions(['2.2.0.6-SNAPSHOT', '2.2.0.3-RC', '2.0.3.10', '2.2.0.3', '2.0.3.9']),
      ['2.0.3.9', '2.0.3.10', '2.2.0.3-RC', '2.2.0.3', '2.2.0.6-SNAPSHOT']
    );
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

  it('takes latest/release from the version order, not from a stale <latest>/<release>', () => {
    const xml = `<metadata><versioning>
    <latest>7.1.2.194-RC</latest>
    <release>7.1.2.194-RC</release>
    <versions>
      <version>8.0.0.304</version>
      <version>8.0.1.88-SNAPSHOT</version>
      <version>7.1.2.194-RC</version>
    </versions></versioning></metadata>`;
    const parsed = parseArtifactMetadataXml(xml);
    assert.deepEqual(parsed.versions, ['7.1.2.194-RC', '8.0.0.304', '8.0.1.88-SNAPSHOT']);
    assert.equal(parsed.latest, '8.0.1.88-SNAPSHOT');
    assert.equal(parsed.release, '8.0.0.304');
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
