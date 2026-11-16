import {
  S3Client,
  ListObjectsV2Command,
  GetObjectCommand,
  PutObjectCommand
} from '@aws-sdk/client-s3';

export const DEFAULT_GROUP_ID = 'org.lucee';
export const DEFAULT_ORG_LUCEE_PREFIX = 'org/lucee/';
export const GROUP_METADATA_KEY = 'org/lucee/maven-metadata.xml';
export const GROUP_INDEX_KEY = 'org/lucee/index.html';

const SNAPSHOT_SUFFIX = '-SNAPSHOT';

// Maven ComparableVersion ordering (same rules as org.apache.maven.artifact.versioning.ComparableVersion):
// numeric segments compare numerically, a qualifier sorts before the plain release of the same number
// and the known qualifiers are ordered alpha < beta < milestone < rc < snapshot < (release) < sp,
// unknown qualifiers after sp (lexically). So 5.0.0.60-BETA < 5.0.0.252, 8.0.0.304-RC < 8.0.0.304 and
// 7.1.3.0-SNAPSHOT < 7.1.3.0. maven-metadata.xml lists <versions> in this order, oldest first.
const QUALIFIERS = ['alpha', 'beta', 'milestone', 'rc', 'snapshot', '', 'sp'];
const QUALIFIER_ALIASES = { ga: '', final: '', release: '', cr: 'rc' };
const RELEASE_INDEX = String(QUALIFIERS.indexOf(''));
const INT = 0, STR = 1, LIST = 2;

function comparableQualifier(q) {
  const i = QUALIFIERS.indexOf(q);
  return i === -1 ? `${QUALIFIERS.length}-${q}` : String(i);
}

function stringItem(value, followedByDigit) {
  if (followedByDigit && value.length === 1) {
    value = { a: 'alpha', b: 'beta', m: 'milestone' }[value] || value;
  }
  value = QUALIFIER_ALIASES[value] ?? value;
  return { type: STR, value };
}

function isNullItem(item) {
  if (item.type === INT) return item.value === 0n;
  if (item.type === STR) return comparableQualifier(item.value) === RELEASE_INDEX;
  return item.items.length === 0;
}

function normalize(list) {
  for (let i = list.items.length - 1; i >= 0; i--) {
    const item = list.items[i];
    if (isNullItem(item)) list.items.splice(i, 1);
    else if (item.type !== LIST) break;
  }
}

function parseItem(isDigit, buf) {
  return isDigit ? { type: INT, value: BigInt(buf) } : stringItem(buf, false);
}

export function parseMavenVersion(version) {
  const v = String(version).trim().toLowerCase();
  const root = { type: LIST, items: [] };
  let list = root;
  const stack = [list];
  let isDigit = false;
  let start = 0;
  const push = () => { const sub = { type: LIST, items: [] }; list.items.push(sub); list = sub; stack.push(sub); };
  for (let i = 0; i < v.length; i++) {
    const c = v[i];
    if (c === '.') {
      list.items.push(i === start ? { type: INT, value: 0n } : parseItem(isDigit, v.slice(start, i)));
      start = i + 1;
    } else if (c === '-') {
      list.items.push(i === start ? { type: INT, value: 0n } : parseItem(isDigit, v.slice(start, i)));
      start = i + 1;
      push();
    } else if (c >= '0' && c <= '9') {
      if (!isDigit && i > start) {
        list.items.push(stringItem(v.slice(start, i), true));
        start = i;
        push();
      }
      isDigit = true;
    } else {
      if (isDigit && i > start) {
        list.items.push(parseItem(true, v.slice(start, i)));
        start = i;
        push();
      }
      isDigit = false;
    }
  }
  if (v.length > start) list.items.push(parseItem(isDigit, v.slice(start)));
  while (stack.length) normalize(stack.pop());
  return root;
}

function compareItems(a, b) {
  if (b === undefined) {
    if (a.type === INT) return a.value === 0n ? 0 : 1;
    if (a.type === STR) return cmp(comparableQualifier(a.value), RELEASE_INDEX);
    return a.items.length === 0 ? 0 : compareItems(a.items[0], undefined);
  }
  if (a.type === INT) {
    if (b.type === INT) return a.value < b.value ? -1 : a.value > b.value ? 1 : 0;
    return 1;
  }
  if (a.type === STR) {
    if (b.type === INT || b.type === LIST) return -1;
    return cmp(comparableQualifier(a.value), comparableQualifier(b.value));
  }
  if (b.type === INT) return -1;
  if (b.type === STR) return 1;
  const n = Math.max(a.items.length, b.items.length);
  for (let i = 0; i < n; i++) {
    const l = a.items[i];
    const r = b.items[i];
    const result = l === undefined ? (r === undefined ? 0 : -compareItems(r, undefined)) : compareItems(l, r);
    if (result !== 0) return result;
  }
  return 0;
}

function cmp(a, b) {
  return a < b ? -1 : a > b ? 1 : 0;
}

/** Compares two Maven versions, <0, 0 or >0 (ComparableVersion semantics, case-insensitive). */
export function compareVersions(a, b) {
  return compareItems(parseMavenVersion(a), parseMavenVersion(b));
}

export function sortVersions(versions) {
  // ties (e.g. 1.0 and 1.0.0) keep a stable, deterministic order
  return [...versions].sort((a, b) => compareVersions(a, b) || cmp(a, b));
}

export function findLatestRelease(sortedVersions) {
  for (let i = sortedVersions.length - 1; i >= 0; i--) {
    if (!sortedVersions[i].toUpperCase().includes(SNAPSHOT_SUFFIX)) {
      return sortedVersions[i];
    }
  }
  return '';
}

export function mavenTimestamp(date = new Date()) {
  const pad = (n) => String(n).padStart(2, '0');
  return (
    String(date.getUTCFullYear()) +
    pad(date.getUTCMonth() + 1) +
    pad(date.getUTCDate()) +
    pad(date.getUTCHours()) +
    pad(date.getUTCMinutes()) +
    pad(date.getUTCSeconds())
  );
}

export function xmlEscape(value) {
  return String(value)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

export function parseArtifactMetadataXml(xml) {
  const versions = [];
  const versionRegex = /<version>([^<]+)<\/version>/g;
  let match;
  while ((match = versionRegex.exec(xml)) !== null) {
    versions.push(match[1]);
  }

  const latestMatch = xml.match(/<latest>([^<]*)<\/latest>/);
  const releaseMatch = xml.match(/<release>([^<]*)<\/release>/);

  const unique = [...new Set(versions)];
  const sorted = sortVersions(unique);

  return {
    versions: sorted,
    // highest version / highest non-SNAPSHOT, not what the file says (older writers set the last published one)
    latest: sorted[sorted.length - 1] || latestMatch?.[1]?.trim() || '',
    release: findLatestRelease(sorted) || releaseMatch?.[1]?.trim() || ''
  };
}

/**
 * Build Lucee group-level maven-metadata.xml (GroupMetadataExtensionLister format).
 */
export function buildGroupMetadataXml(groupId, artifacts, lastUpdated) {
  const lines = [
    '<?xml version="1.0" encoding="UTF-8"?>',
    '<metadata>',
    `  <groupId>${xmlEscape(groupId)}</groupId>`,
    '  <artifacts>'
  ];

  for (const artifact of artifacts) {
    lines.push('    <artifact>');
    lines.push(`      <artifactId>${xmlEscape(artifact.artifactId)}</artifactId>`);
    lines.push(`      <latest>${xmlEscape(artifact.latest)}</latest>`);
    if (artifact.release) {
      lines.push(`      <release>${xmlEscape(artifact.release)}</release>`);
    }
    lines.push('    </artifact>');
  }

  lines.push('  </artifacts>');
  lines.push(`  <lastUpdated>${xmlEscape(lastUpdated)}</lastUpdated>`);
  lines.push('</metadata>');
  return lines.join('\n');
}

export function generateDirectoryListingHtml(artifacts) {
  const formatDate = (date) => {
    if (!date || date.getTime() === 0) return '                   -';
    return date.toISOString().slice(0, 16).replace('T', ' ');
  };

  const metadataLine =
    `<a href="maven-metadata.xml" title="maven-metadata.xml">${'maven-metadata.xml'.padEnd(50)}</a>                     ${formatDate(new Date())}         -      `;

  const artifactLinks = artifacts.map((artifact) => {
    const paddedName = (artifact.name + '/').padEnd(50);
    const formattedDate = formatDate(artifact.lastModified);
    return `<a href="${artifact.name}/" title="${artifact.name}/">${paddedName}</a>                     ${formattedDate}         -      `;
  }).join('\n');

  return `<!DOCTYPE html>
<html>

<head>
  <title>Central Repository: org/lucee</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <style>
body {
  background: #fff;
}
  </style>
</head>

<body>
  <header>
    <h1>org/lucee</h1>
  </header>
  <hr/>
  <main>
    <pre id="contents">
<a href="../">../</a>
${metadataLine}
${artifactLinks}
    </pre>
  </main>
  <hr/>
</body>

</html>`;
}

export function isExtensionArtifactId(artifactId) {
  return artifactId.endsWith('-extension');
}

async function listArtifactPrefixes(s3Client, bucket, prefix = DEFAULT_ORG_LUCEE_PREFIX) {
  const prefixes = [];
  let continuationToken;

  do {
    const response = await s3Client.send(new ListObjectsV2Command({
      Bucket: bucket,
      Prefix: prefix,
      Delimiter: '/',
      ContinuationToken: continuationToken
    }));

    if (response.CommonPrefixes) {
      for (const entry of response.CommonPrefixes) {
        prefixes.push(entry.Prefix);
      }
    }
    continuationToken = response.IsTruncated ? response.NextContinuationToken : undefined;
  } while (continuationToken);

  return prefixes;
}

async function getObjectBody(s3Client, bucket, key) {
  const response = await s3Client.send(new GetObjectCommand({ Bucket: bucket, Key: key }));
  return response.Body.transformToString();
}

async function listVersionPrefixes(s3Client, bucket, artifactPrefix) {
  const versions = [];
  let continuationToken;

  do {
    const response = await s3Client.send(new ListObjectsV2Command({
      Bucket: bucket,
      Prefix: artifactPrefix,
      Delimiter: '/',
      ContinuationToken: continuationToken
    }));

    if (response.CommonPrefixes) {
      for (const entry of response.CommonPrefixes) {
        const version = entry.Prefix.slice(artifactPrefix.length).replace(/\/$/, '');
        if (version) versions.push(version);
      }
    }
    continuationToken = response.IsTruncated ? response.NextContinuationToken : undefined;
  } while (continuationToken);

  return sortVersions(versions);
}

async function getArtifactLastModified(s3Client, bucket, artifactPrefix) {
  let latestDate = new Date(0);
  let continuationToken;

  do {
    const response = await s3Client.send(new ListObjectsV2Command({
      Bucket: bucket,
      Prefix: artifactPrefix,
      MaxKeys: 1000,
      ContinuationToken: continuationToken
    }));

    if (response.Contents) {
      for (const obj of response.Contents) {
        if (obj.LastModified && obj.LastModified > latestDate) {
          latestDate = obj.LastModified;
        }
      }
    }
    continuationToken = response.IsTruncated ? response.NextContinuationToken : undefined;
  } while (continuationToken);

  return latestDate;
}

async function resolveArtifactVersions(s3Client, bucket, artifactId) {
  const metadataKey = `${DEFAULT_ORG_LUCEE_PREFIX}${artifactId}/maven-metadata.xml`;

  try {
    const xml = await getObjectBody(s3Client, bucket, metadataKey);
    const parsed = parseArtifactMetadataXml(xml);
    if (parsed.versions.length > 0) {
      return parsed;
    }
  } catch (error) {
    if (error.name !== 'NoSuchKey' && error.$metadata?.httpStatusCode !== 404) {
      throw error;
    }
  }

  const versions = await listVersionPrefixes(s3Client, bucket, `${DEFAULT_ORG_LUCEE_PREFIX}${artifactId}/`);
  return {
    versions,
    latest: versions[versions.length - 1] || '',
    release: findLatestRelease(versions)
  };
}

/**
 * Scan S3 org/lucee/ and build group metadata entries for *-extension artifacts.
 */
export async function collectExtensionArtifactsFromS3(s3Client, bucket, options = {}) {
  const prefix = options.prefix || DEFAULT_ORG_LUCEE_PREFIX;
  const groupId = options.groupId || DEFAULT_GROUP_ID;
  const artifactPrefixes = await listArtifactPrefixes(s3Client, bucket, prefix);
  const artifacts = [];

  for (const artifactPrefix of artifactPrefixes) {
    const artifactId = artifactPrefix.slice(prefix.length).replace(/\/$/, '');
    if (!artifactId || !isExtensionArtifactId(artifactId)) {
      continue;
    }

    const resolved = await resolveArtifactVersions(s3Client, bucket, artifactId);
    if (!resolved.versions.length) {
      continue;
    }

    artifacts.push({
      artifactId,
      latest: resolved.latest,
      release: resolved.release
    });
  }

  artifacts.sort((a, b) => a.artifactId.localeCompare(b.artifactId));

  return { groupId, artifacts };
}

export async function generateGroupMetadataFromS3(s3Client, bucket, options = {}) {
  const { groupId, artifacts } = await collectExtensionArtifactsFromS3(s3Client, bucket, options);
  const lastUpdated = mavenTimestamp(new Date());
  const xml = buildGroupMetadataXml(groupId, artifacts, lastUpdated);
  return { xml, artifacts, lastUpdated };
}

export async function uploadGroupMetadata(s3Client, bucket, xml, key = GROUP_METADATA_KEY) {
  await s3Client.send(new PutObjectCommand({
    Bucket: bucket,
    Key: key,
    Body: xml,
    ContentType: 'application/xml'
  }));
}

export async function generateAndUploadGroupMetadata(s3Client, bucket, options = {}) {
  const result = await generateGroupMetadataFromS3(s3Client, bucket, options);
  await uploadGroupMetadata(s3Client, bucket, result.xml, options.metadataKey || GROUP_METADATA_KEY);
  return result;
}

export async function generateDirectoryListingFromS3(s3Client, bucket, options = {}) {
  const prefix = options.prefix || DEFAULT_ORG_LUCEE_PREFIX;
  const artifactPrefixes = await listArtifactPrefixes(s3Client, bucket, prefix);
  const artifacts = [];

  for (const artifactPrefix of artifactPrefixes) {
    const name = artifactPrefix.slice(prefix.length).replace(/\/$/, '');
    if (!name) continue;

    artifacts.push({
      name,
      lastModified: await getArtifactLastModified(s3Client, bucket, artifactPrefix)
    });
  }

  artifacts.sort((a, b) => a.name.localeCompare(b.name));
  return { artifacts, html: generateDirectoryListingHtml(artifacts) };
}

export async function generateAndUploadDirectoryListing(s3Client, bucket, options = {}) {
  const { artifacts, html } = await generateDirectoryListingFromS3(s3Client, bucket, options);
  await s3Client.send(new PutObjectCommand({
    Bucket: bucket,
    Key: options.indexKey || GROUP_INDEX_KEY,
    Body: html,
    ContentType: 'text/html'
  }));
  return artifacts;
}

export async function rebuildCdnGroupIndex(s3Client, bucket, options = {}) {
  const metadata = await generateAndUploadGroupMetadata(s3Client, bucket, options);
  const listing = await generateAndUploadDirectoryListing(s3Client, bucket, options);
  return {
    artifactCount: metadata.artifacts.length,
    directoryEntries: listing.length,
    lastUpdated: metadata.lastUpdated
  };
}
