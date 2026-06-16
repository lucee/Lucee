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

/**
 * Maven-style version sort key (ported from maven-bridge BridgeSupport.cfc).
 */
export function versionSortKey(version) {
  const parts = version.split(/[.-]/);
  let key = '';
  for (const part of parts) {
    if (part.toUpperCase() === 'SNAPSHOT') {
      key += '.999999';
    } else if (/^\d+$/.test(part)) {
      key += '.' + String(Number(part)).padStart(5, '0');
    } else {
      key += '.' + part.toLowerCase();
    }
  }
  return key;
}

export function sortVersions(versions) {
  return [...versions].sort((a, b) => versionSortKey(a).localeCompare(versionSortKey(b)));
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
    latest: latestMatch?.[1]?.trim() || sorted[sorted.length - 1] || '',
    release: releaseMatch?.[1]?.trim() || findLatestRelease(sorted)
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
