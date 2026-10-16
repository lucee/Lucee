import { S3Client, ListObjectsV2Command, GetBucketLocationCommand } from '@aws-sdk/client-s3';
import { rebuildCdnGroupIndex } from './maven-group-metadata.js';

function log(message, level = 'INFO') {
  console.log(`[${new Date().toISOString()}] [${level}] ${message}`);
}

function logError(message) {
  log(message, 'ERROR');
}

async function createS3Client(accessKeyId, secretAccessKey, bucket, region) {
  let s3Client = new S3Client({
    region,
    credentials: { accessKeyId, secretAccessKey }
  });

  try {
    await s3Client.send(new ListObjectsV2Command({ Bucket: bucket, MaxKeys: 1 }));
    return s3Client;
  } catch (error) {
    if (error.name !== 'PermanentRedirect') {
      throw error;
    }

    const locationResponse = await s3Client.send(new GetBucketLocationCommand({ Bucket: bucket }));
    const correctRegion = locationResponse.LocationConstraint || 'us-east-1';
    log(`Detected bucket region: ${correctRegion}`);

    s3Client = new S3Client({
      region: correctRegion,
      credentials: { accessKeyId, secretAccessKey }
    });
    await s3Client.send(new ListObjectsV2Command({ Bucket: bucket, MaxKeys: 1 }));
    return s3Client;
  }
}

async function run() {
  const dryRun = process.env.INPUT_DRY_RUN === 'true';
  const accessKeyId = process.env.INPUT_S3_ACCESS_KEY;
  const secretAccessKey = process.env.INPUT_S3_SECRET_KEY;
  const bucket = process.env.INPUT_S3_BUCKET || 'lucee-downloads';
  const region = process.env.INPUT_S3_REGION || 'us-east-1';

  if (!accessKeyId || !secretAccessKey) {
    throw new Error('S3 credentials are required (INPUT_S3_ACCESS_KEY, INPUT_S3_SECRET_KEY)');
  }

  log(`Rebuilding CDN group index for bucket: ${bucket}${dryRun ? ' (DRY RUN)' : ''}`);

  const s3Client = await createS3Client(accessKeyId, secretAccessKey, bucket, region);

  if (dryRun) {
    const { generateGroupMetadataFromS3, generateDirectoryListingFromS3 } = await import('./maven-group-metadata.js');
    const metadata = await generateGroupMetadataFromS3(s3Client, bucket);
    const listing = await generateDirectoryListingFromS3(s3Client, bucket);
    log(`Would upload ${metadata.artifacts.length} extension artifacts to org/lucee/maven-metadata.xml`);
    log(`Would upload org/lucee/index.html with ${listing.artifacts.length} directory entries`);
    return;
  }

  const result = await rebuildCdnGroupIndex(s3Client, bucket);
  log(`✓ Uploaded org/lucee/maven-metadata.xml (${result.artifactCount} extensions)`);
  log(`✓ Uploaded org/lucee/index.html (${result.directoryEntries} entries)`);
  log(`✓ lastUpdated=${result.lastUpdated}`);
}

run().catch((error) => {
  logError(`Script failed: ${error.message}`);
  process.exit(1);
});
