# CDN Maven repository scripts

Node scripts that organize Lucee build artifacts on S3 (`lucee-downloads` → `cdn.lucee.org`) into a Maven-style layout under `org/lucee/`.

## Scripts

| Script | Workflow | Purpose |
|--------|----------|---------|
| `organize-s3-artifacts.js` | [organize-artifacts.yml](../workflows/organize-artifacts.yml) | Copy/move Lucee core builds into `org/lucee/lucee/{version}/` and update per-artifact Maven metadata |
| `organize-s3-extensions.js` | [organize-extensions.yml](../workflows/organize-extensions.yml) | Copy/move extension `.lex` files into `org/lucee/{artifactId}/{version}/`, update artifact metadata, refresh group index |
| `rebuild-group-metadata.js` | [rebuild-cdn-group-metadata.yml](../workflows/rebuild-cdn-group-metadata.yml) | Regenerate `org/lucee/maven-metadata.xml` and `org/lucee/index.html` only (no file moves) |

Shared logic lives in `maven-group-metadata.js` (group-level metadata, version sorting, HTML index).

## Group-level `maven-metadata.xml`

Lucee 8.0+ discovers extensions via `GroupMetadataExtensionLister`, which reads:

```
https://cdn.lucee.org/org/lucee/maven-metadata.xml
```

That file uses a Lucee-specific schema (see `maven-bridge` `BridgeSupport.buildGroupMetadata()`): a list of `<artifact>` entries with `<artifactId>`, `<latest>`, and optional `<release>` for each `*-extension` artifact.

The rebuild script scans existing S3 artifact metadata under `org/lucee/*/maven-metadata.xml` (or version directories as fallback) and publishes the group index.

## When to run which workflow

- **Publishing a new Lucee version** → `Organize Lucee S3 Artifacts`
- **Publishing a new extension version** → `Organize Lucee Extensions` (also refreshes group metadata and `index.html`)
- **Backfill or repair group metadata** after bulk changes, or without moving files → `Rebuild CDN Group Metadata`

## Local development

```sh
cd .github/scripts
npm install
npm test
```

Rebuild against S3 (requires credentials):

```sh
export INPUT_S3_ACCESS_KEY=...
export INPUT_S3_SECRET_KEY=...
export INPUT_S3_BUCKET=lucee-downloads
node rebuild-group-metadata.js
```

Dry run (scan only):

```sh
INPUT_DRY_RUN=true node rebuild-group-metadata.js
```
