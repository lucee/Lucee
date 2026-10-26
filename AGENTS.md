# Lucee Server

Open source CFML Server deployed as a Java Servlet.

## Folder Structure

- `/ant`: Ant build scripts
- `/loader`: The Loader Interface API for Lucee Core and its extensions, do not modify any interfaces
- `/core`: The main source code for Lucee Server
- `/test`: CFML test suites

## Build & Commands

LDEV-6711: Lucee is built as a single jar by the `pom.xml` in the repository root (`loader/src` and `core/src` are compiled together). Ant is only used inside it for the archive step (`ant/build-single-jar.xml`).

Always run from the repository root and pipe the output to a file.

The CFML tests run against the packaged jar, so use `verify` (not `test`). Do NOT run `ant fast` before.

- Run a specific test: `mvn verify -DtestFilter="{testFilename}"`
- Run the whole test suite: `mvn verify`
- Build only (no tests): `mvn package -DskipTests`
- Skip the archives, extensions and bundles (the jar is not usable, compile check only): `-Dlucee.skip.archives=true`

The old build (`loader/pom.xml`, `ant fast` in `/loader`) still exists, it is used by the release (light/zero jars, `.lco`, S3 upload) until that is moved to the root pom. `lucee.version.*` and the library versions are defined in both poms, keep them in sync.

### Development Environment

- Build requires Java, Maven and Ant
- Build usually is run with Java 21
- All artifacts are compiled to bytecode targeting Java 21

### Issue Tracking

Lucee tickets are in the style `LDEV-xxxx`, where xxxx is a number.

Test cases for tickets are created under `/test/tickets/LDEVxxxx.cfc`, with any additional files under `/test/tickets/LDEVxxxx/`.

To read an issue from Jira, rewrite the browse URL to the XML version:

- `https://luceeserver.atlassian.net/browse/LDEV-5850`
- `https://luceeserver.atlassian.net/si/jira.issueviews:issue-xml/LDEV-5850/LDEV-5850.xml`

## Script Runner

Run CFML scripts headless using [script-runner](https://github.com/lucee/script-runner). Requires a local checkout (usually in the parent directory of this repo).

Build a custom JAR first with `mvn package -DskipTests` from the root (`target/lucee-{version}.jar`), then run:

```sh
ant -buildfile "../script-runner/build.xml" -DluceeJar="/full/path/to/target/lucee-{version}.jar" -Dwebroot="D:\work\yourproject" -Dexecute="test.cfm"
```

See the [script-runner README](https://github.com/lucee/script-runner/blob/main/README.md) for full details.

## Contribution Workflow

- Create a feature branch off the appropriate version branch. `7.0` is the active stable branch. `6.2` is the active LTS branch.
- Create or update unit tests for your changes, TDD, repro then fix.
- Rebase with the latest upstream changes before submitting.
- Commit messages must include the ticket number, e.g., `LDEV-007 Add support for OSGI bundles`.
- Include a link to the JIRA ticket in your pull request description.
- If your change affects a documented feature, also submit a PR to the Lucee docs repo.

## Code Style

- Follow the Eclipse settings for Java code in `/org.eclipse.jdt.core.prefs`
- Use Tabs for indentation (2 spaces for YAML/JSON/MD)
- Avoid adding comments, unless they add important additional context
- Never remove existing comments

## Testing

[Testing Guidelines](test/README.md)

- CFML tests are written using [TestBox](https://testbox.ortusbooks.com/)
- All CFML tests should extend `org.lucee.cfml.test.LuceeTestCase`
- CFML tests should not use Java unless absolutely required, prefer CFML functionality
- Tests should cleanup after themselves, temporary files go under `getTempDirectory()`
- Test framework code in the root of `/test` must be compatible with Lucee 5.4 — do not use newer CFML functionality

## Configuration

When adding new configuration options:

1. Variables are always strings, cast to the correct type with an appropriate default
2. Read variables once into a static variable using `getSystemPropOrEnvVar(String name, String defaultValue)`
3. Document variables in `core/src/main/java/resource/setting/sysprop-envvar.json`

When updating a Java library:

1. Update both `pom.xml` files under `/loader` and `/core`
2. Update the corresponding `Require-Bundle:` entry in `core/src/main/java/META-INF/MANIFEST.MF`
