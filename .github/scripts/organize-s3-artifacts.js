import { S3Client, ListObjectsV2Command, CopyObjectCommand, DeleteObjectCommand, PutObjectCommand, GetObjectCommand, HeadObjectCommand, GetBucketLocationCommand } from '@aws-sdk/client-s3';
import {
  generateAndUploadDirectoryListing,
  generateAndUploadGroupMetadata
} from './maven-group-metadata.js';

// Helper function to log with timestamp
function log(message, level = 'INFO') {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] [${level}] ${message}`);
}

function logError(message) {
  log(message, 'ERROR');
}

function logWarning(message) {
  log(message, 'WARN');
}

async function run() {
  try {
    // Debug: Log all INPUT_ environment variables
    log('Environment variables:');
    Object.keys(process.env)
      .filter(key => key.startsWith('INPUT_'))
      .forEach(key => {
        log(`  ${key}=${process.env[key]}`);
      });

    // Get inputs from environment variables
    const versionsInput = process.env.INPUT_VERSIONS;
    const operation = process.env.INPUT_OPERATION || 'move';
    const dryRun = process.env.INPUT_DRY_RUN === 'true';
    const accessKeyId = process.env.INPUT_S3_ACCESS_KEY;
    const secretAccessKey = process.env.INPUT_S3_SECRET_KEY;
    const bucket = process.env.INPUT_S3_BUCKET || 'lucee-downloads';
    const region = process.env.INPUT_S3_REGION || 'us-east-1';

    log(`Versions input: "${versionsInput}"`);

    if (!versionsInput || versionsInput.trim() === '') {
      throw new Error('Versions list is required and cannot be empty');
    }
    
    if (!accessKeyId || !secretAccessKey) {
      throw new Error('S3 credentials are required');
    }

    // Parse versions from comma-separated input
    const versions = versionsInput.split(',').map(v => v.trim()).filter(v => v.length > 0);
    
    if (versions.length === 0) {
      throw new Error('No valid versions found in input');
    }

    log(`Starting ${dryRun ? 'DRY RUN of ' : ''}${operation} operation for ${versions.length} version(s): ${versions.join(', ')}`);
    log(`S3 Bucket: ${bucket}, Initial Region: ${region}`);

    // Create initial S3 client to detect bucket region
    let s3Client = new S3Client({
      region,
      credentials: {
        accessKeyId,
        secretAccessKey
      }
    });
    
    // Test S3 connectivity and detect correct region
    try {
      log('Testing S3 connectivity and detecting bucket region...');
      await s3Client.send(new ListObjectsV2Command({
        Bucket: bucket,
        MaxKeys: 1
      }));
      log('✓ S3 connectivity successful');
    } catch (error) {
      if (error.name === 'PermanentRedirect') {
        // Try to get the correct region
        try {
          log('Bucket is in different region, detecting correct region...');
          const locationResponse = await s3Client.send(new GetBucketLocationCommand({ Bucket: bucket }));
          const correctRegion = locationResponse.LocationConstraint || 'us-east-1';
          log(`✓ Detected bucket region: ${correctRegion}`);
          
          // Create new client with correct region
          s3Client = new S3Client({
            region: correctRegion,
            credentials: {
              accessKeyId,
              secretAccessKey
            }
          });
          
          // Test again with correct region
          await s3Client.send(new ListObjectsV2Command({
            Bucket: bucket,
            MaxKeys: 1
          }));
          log('✓ S3 connectivity successful with correct region');
        } catch (regionError) {
          throw new Error(`Failed to detect bucket region: ${regionError.name} - ${regionError.message}`);
        }
      } else {
        throw new Error(`S3 connectivity test failed: ${error.name} - ${error.message}. Check bucket name and credentials.`);
      }
    }

    // Overall statistics
    let totalProcessed = 0;
    let totalMissing = 0;
    let totalSkipped = 0;
    let totalErrors = 0;
    let successfulVersions = 0;

    // Process each version
    for (let i = 0; i < versions.length; i++) {
      const version = versions[i];
      log(`\n${'='.repeat(60)}`);
      log(`Processing version ${i + 1}/${versions.length}: ${version}`);
      log(`${'='.repeat(60)}`);

      try {
        const versionStats = await processVersion(s3Client, bucket, version, operation, dryRun);
        totalProcessed += versionStats.processed;
        totalMissing += versionStats.missing;
        totalSkipped += versionStats.skipped;
        totalErrors += versionStats.errors;
        
        if (versionStats.errors === 0) {
          successfulVersions++;
          log(`✅ Version ${version} completed successfully`);
        } else {
          log(`⚠️  Version ${version} completed with ${versionStats.errors} errors`);
        }
      } catch (error) {
        totalErrors++;
        logError(`❌ Version ${version} failed: ${error.message}`);
      }
    }

    // Regenerate org/lucee group index and maven-metadata.xml when files were processed
    if (!dryRun && totalProcessed > 0) {
      try {
        const listing = await generateAndUploadDirectoryListing(s3Client, bucket);
        log(`✓ Generated HTML directory listing for org/lucee (${listing.length} entries)`);
      } catch (error) {
        totalErrors++;
        logError(`✗ Failed to generate directory listing: ${error.message}`);
      }

      try {
        const group = await generateAndUploadGroupMetadata(s3Client, bucket);
        log(`✓ Generated group maven-metadata.xml for org/lucee (${group.artifacts.length} extensions)`);
      } catch (error) {
        totalErrors++;
        logError(`✗ Failed to generate group metadata: ${error.message}`);
      }
    } else if (dryRun) {
      log('[DRY RUN] Would generate org/lucee/index.html and org/lucee/maven-metadata.xml');
    }

    // Overall summary
    log(`\n${'='.repeat(60)}`);
    log('OVERALL SUMMARY');
    log(`${'='.repeat(60)}`);
    log(`Versions processed: ${successfulVersions}/${versions.length}`);
    log(`Total files processed: ${totalProcessed}`);
    log(`Total missing files (skipped): ${totalMissing}`);
    log(`Total already organized (skipped): ${totalSkipped}`);
    log(`Total errors: ${totalErrors}`);
    
    if (dryRun) {
      log('This was a dry run - no actual changes were made');
    }
    
    if (totalErrors > 0 || successfulVersions < versions.length) {
      logWarning(`Completed with issues. ${successfulVersions}/${versions.length} versions processed successfully.`);
      process.exit(1);
    } else {
      log('🎉 All versions processed successfully!');
    }

  } catch (error) {
    logError(`Script failed: ${error.message}`);
    process.exit(1);
  }
}

async function processVersion(s3Client, bucket, version, operation, dryRun) {
  log(`Starting processing for version: ${version}`);

  // Define file mappings from source to target for this version
  const fileMappings = [
    {
      source: `lucee-${version}.jar`,
      target: `org/lucee/lucee/${version}/lucee-${version}.jar`
    },
    {
      source: `${version}.lco`,
      target: `org/lucee/lucee/${version}/lucee-${version}.lco`
    },
    {
      source: `lucee-light-${version}.jar`,
      target: `org/lucee/lucee/${version}/lucee-${version}-light.jar`
    },
    {
      source: `lucee-zero-${version}.jar`,
      target: `org/lucee/lucee/${version}/lucee-${version}-zero.jar`
    },
    {
      source: `lucee-${version}.war`,
      target: `org/lucee/lucee/${version}/lucee-${version}.war`
    },
    {
      source: `lucee-express-${version}.zip`,
      target: `org/lucee/lucee/${version}/lucee-${version}-express.zip`
    },
    {
      source: `forgebox-${version}.zip`,
      target: `org/lucee/lucee/${version}/lucee-${version}-forgebox.zip`
    },
    {
      source: `forgebox-light-${version}.zip`,
      target: `org/lucee/lucee/${version}/lucee-${version}-forgebox-light.zip`
    }
  ];

  log(`Found ${fileMappings.length} file mappings to process for version ${version}`);

  // In dry run mode, also list files in bucket to help with debugging (only for first version)
  if (dryRun && version === versions[0]) {
    try {
      log('Listing files in S3 bucket (first 50 files):');
      const listResponse = await s3Client.send(new ListObjectsV2Command({
        Bucket: bucket,
        MaxKeys: 50
      }));
      
      if (listResponse.Contents && listResponse.Contents.length > 0) {
        listResponse.Contents.forEach(obj => {
          log(`  - ${obj.Key} (${obj.Size} bytes, modified: ${obj.LastModified})`);
        });
      } else {
        log('  No files found in bucket');
      }
    } catch (error) {
      logWarning(`Could not list bucket contents: ${error.message}`);
    }
  }

  // Process each file mapping for this version
  let processedCount = 0;
  let skippedCount = 0;
  let errorCount = 0;
  let missingCount = 0;

  for (const mapping of fileMappings) {
    try {
      const result = await processFile(s3Client, bucket, mapping.source, mapping.target, operation, dryRun);
      if (result.processed) {
        processedCount++;
        log(`  ✓ ${result.action}: ${mapping.source} -> ${mapping.target}`);
      } else {
        if (result.reason === 'source file not found') {
          missingCount++;
          log(`  ⚠ Missing: ${mapping.source} (file does not exist in S3)`);
        } else {
          skippedCount++;
          logWarning(`  ⚠ Skipped: ${mapping.source} (${result.reason})`);
        }
      }
    } catch (error) {
      errorCount++;
      logError(`  ✗ Failed to process ${mapping.source}: ${error.message}`);
    }
  }

  // Generate and upload version-specific maven-metadata.xml
  if (!dryRun && processedCount > 0) {
    try {
      // Get the timestamp from the main JAR file for metadata
      const mainJarKey = `lucee-${version}.jar`;
      let artifactTimestamp;
      
      try {
        const headResponse = await s3Client.send(new HeadObjectCommand({
          Bucket: bucket,
          Key: mainJarKey
        }));
        artifactTimestamp = headResponse.LastModified;
        log(`  ✓ Using timestamp from ${mainJarKey}: ${artifactTimestamp}`);
      } catch (error) {
        // If main JAR doesn't exist, try to get timestamp from any processed file
        artifactTimestamp = new Date();
        log(`  ⚠ Could not get timestamp from ${mainJarKey}, using current time`);
      }
      
      const versionMetadata = generateVersionMetadata(version, artifactTimestamp);
      await uploadMetadata(s3Client, bucket, `org/lucee/lucee/${version}/maven-metadata.xml`, versionMetadata);
      log(`  ✓ Generated version-specific maven-metadata.xml for ${version}`);
    } catch (error) {
      errorCount++;
      logError(`  ✗ Failed to generate version metadata: ${error.message}`);
    }

    // Update parent maven-metadata.xml
    try {
      // For parent metadata, also try to use the artifact timestamp
      const mainJarKey = `lucee-${version}.jar`;
      let artifactTimestamp;
      
      try {
        const headResponse = await s3Client.send(new HeadObjectCommand({
          Bucket: bucket,
          Key: mainJarKey
        }));
        artifactTimestamp = headResponse.LastModified;
      } catch (error) {
        artifactTimestamp = new Date();
      }
      
      await updateParentMetadata(s3Client, bucket, version, artifactTimestamp);
      log(`  ✓ Updated parent maven-metadata.xml with version ${version}`);
    } catch (error) {
      errorCount++;
      logError(`  ✗ Failed to update parent metadata: ${error.message}`);
    }
  } else if (dryRun) {
    log(`  [DRY RUN] Would generate maven-metadata.xml files for version ${version}`);
  }

  // Version summary
  log(`--- Version ${version} Summary ---`);
  log(`  Processed: ${processedCount}`);
  log(`  Missing files (skipped): ${missingCount}`);
  log(`  Already organized (skipped): ${skippedCount}`);
  log(`  Errors: ${errorCount}`);

  return {
    processed: processedCount,
    missing: missingCount,
    skipped: skippedCount,
    errors: errorCount
  };
}

async function processFile(s3Client, bucket, sourceKey, targetKey, operation, dryRun) {
  // Check if source file exists
  try {
    log(`Checking if source file exists: ${sourceKey}`);
    await s3Client.send(new HeadObjectCommand({
      Bucket: bucket,
      Key: sourceKey
    }));
    log(`✓ Source file exists: ${sourceKey}`);
  } catch (error) {
    if (error.name === 'NotFound' || error.name === 'NoSuchKey' || error.$metadata?.httpStatusCode === 404) {
      log(`Source file not found: ${sourceKey} - this is normal, some versions don't have all file types`);
      return { processed: false, reason: 'source file not found' };
    }
    if (error.name === 'AccessDenied' || error.$metadata?.httpStatusCode === 403) {
      throw new Error(`Access denied to ${sourceKey}. Check S3 permissions.`);
    }
    throw new Error(`Failed to check source file ${sourceKey}: ${error.name} - ${error.message}`);
  }

  // Check if target file already exists
  try {
    log(`Checking if target file already exists: ${targetKey}`);
    await s3Client.send(new HeadObjectCommand({
      Bucket: bucket,
      Key: targetKey
    }));
    log(`Target file already exists: ${targetKey}`);
    return { processed: false, reason: 'target file already exists' };
  } catch (error) {
    if (error.name === 'NotFound' || error.name === 'NoSuchKey' || error.$metadata?.httpStatusCode === 404) {
      // Target doesn't exist, proceed
      log(`✓ Target location is available: ${targetKey}`);
    } else if (error.name === 'AccessDenied' || error.$metadata?.httpStatusCode === 403) {
      throw new Error(`Access denied to ${targetKey}. Check S3 permissions.`);
    } else {
      throw new Error(`Failed to check target file ${targetKey}: ${error.name} - ${error.message}`);
    }
  }

  if (dryRun) {
    return { processed: true, action: `[DRY RUN] Would ${operation}` };
  }

  try {
    // Copy file to new location
    log(`Copying ${sourceKey} to ${targetKey}`);
    await s3Client.send(new CopyObjectCommand({
      Bucket: bucket,
      CopySource: `${bucket}/${sourceKey}`,
      Key: targetKey,
      MetadataDirective: 'COPY'
    }));

    // Delete original file if operation is 'move'
    if (operation === 'move') {
      log(`Deleting original file: ${sourceKey}`);
      await s3Client.send(new DeleteObjectCommand({
        Bucket: bucket,
        Key: sourceKey
      }));
      return { processed: true, action: 'Moved' };
    } else {
      return { processed: true, action: 'Copied' };
    }
  } catch (error) {
    throw new Error(`Failed to ${operation} ${sourceKey}: ${error.name} - ${error.message}`);
  }
}

function generateVersionMetadata(version, artifactTimestamp) {
  // Convert timestamp to the format Maven metadata expects (YYYYMMDDHHMMSS)
  const timestamp = artifactTimestamp.toISOString().replace(/[-:T]/g, '').split('.')[0];
  
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <version>${version}</version>
  <versioning>
    <lastUpdated>${timestamp}</lastUpdated>
    <snapshotVersions>
      <snapshotVersion>
        <extension>jar</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>light</classifier>
        <extension>jar</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>zero</classifier>
        <extension>jar</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <extension>lco</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <extension>war</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>express</classifier>
        <extension>zip</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>forgebox</classifier>
        <extension>zip</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
      <snapshotVersion>
        <classifier>forgebox-light</classifier>
        <extension>zip</extension>
        <value>${version}</value>
        <updated>${timestamp}</updated>
      </snapshotVersion>
    </snapshotVersions>
  </versioning>
</metadata>`;
}

async function uploadMetadata(s3Client, bucket, key, content) {
  await s3Client.send(new PutObjectCommand({
    Bucket: bucket,
    Key: key,
    Body: content,
    ContentType: 'application/xml'
  }));
}

async function updateParentMetadata(s3Client, bucket, newVersion, artifactTimestamp) {
  const metadataKey = 'org/lucee/lucee/maven-metadata.xml';
  let existingMetadata = null;
  
  try {
    // Try to get existing parent metadata
    const response = await s3Client.send(new GetObjectCommand({
      Bucket: bucket,
      Key: metadataKey
    }));
    existingMetadata = await response.Body.transformToString();
  } catch (error) {
    if (error.name === 'NoSuchKey' || error.$metadata?.httpStatusCode === 404) {
      log('Parent maven-metadata.xml not found, creating new one');
    } else {
      throw error;
    }
  }

  // Convert artifact timestamp to Maven format
  const timestamp = artifactTimestamp.toISOString().replace(/[-:T]/g, '').split('.')[0];
  let updatedMetadata;

  if (existingMetadata) {
    // Parse existing metadata and add new version
    updatedMetadata = addVersionToMetadata(existingMetadata, newVersion, timestamp);
  } else {
    // Create new metadata
    updatedMetadata = `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <versioning>
    <latest>${newVersion}</latest>
    <release>${newVersion.includes('SNAPSHOT') ? '' : newVersion}</release>
    <versions>
      <version>${newVersion}</version>
    </versions>
    <lastUpdated>${timestamp}</lastUpdated>
  </versioning>
</metadata>`;
  }

  await uploadMetadata(s3Client, bucket, metadataKey, updatedMetadata);
}

function addVersionToMetadata(existingXml, newVersion, timestamp) {
  // Extract existing versions from the XML
  const versionRegex = /<version>([^<]+)<\/version>/g;
  const existingVersions = [];
  let match;
  
  while ((match = versionRegex.exec(existingXml)) !== null) {
    existingVersions.push(match[1]);
  }
  
  // Add new version if it doesn't exist
  if (!existingVersions.includes(newVersion)) {
    existingVersions.push(newVersion);
  }
  
  // Sort versions (simple string sort, which works reasonably well for semantic versions)
  existingVersions.sort();
  
  // Determine latest and release versions
  const latest = newVersion; // Always set the new version as latest
  let release = '';
  
  // Find the latest non-SNAPSHOT version for release
  for (let i = existingVersions.length - 1; i >= 0; i--) {
    if (!existingVersions[i].includes('SNAPSHOT')) {
      release = existingVersions[i];
      break;
    }
  }
  
  // Generate properly formatted XML
  const versionsXml = existingVersions.map(v => `      <version>${v}</version>`).join('\n');
  
  return `<?xml version="1.0" encoding="UTF-8"?>
<metadata>
  <groupId>org.lucee</groupId>
  <artifactId>lucee</artifactId>
  <versioning>
    <latest>${latest}</latest>
    <release>${release}</release>
    <versions>
${versionsXml}
    </versions>
    <lastUpdated>${timestamp}</lastUpdated>
  </versioning>
</metadata>`;
}

// Run the script
run();