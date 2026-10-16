# Lucee Server

Open source CFML Server deployed as a Java Servlet.

## Folder Structure

- `/ant`: Ant build scripts
- `/loader`: The Loader Interface API for Lucee Core and its extensions, do not modify any interfaces
- `/core`: The main source code for Lucee Server
- `/test`: CFML test suites

## Build & Commands

Always run from the `/loader` directory and pipe the output to a file.

Maven builds automatically before testing, so do NOT run `ant fast` before `mvn test`.

- Run a specific test: `mvn test -DtestFilter="{testFilename}"`
- Run the whole test suite: `mvn test`
- Build only (no tests, for script-runner): `ant fast`

### Development Environment

- Build requires Java, Maven and Ant
- Build usually is run with Java 21
- All artifacts are compiled to bytecode targeting Java 11

### Issue Tracking

Lucee tickets are in the style `LDEV-xxxx`, where xxxx is a number.

Test cases for tickets are created under `/test/tickets/LDEVxxxx.cfc`, with any additional files under `/test/tickets/LDEVxxxx/`.

To read an issue from Jira, rewrite the browse URL to the XML version:

- `https://luceeserver.atlassian.net/browse/LDEV-5850`
- `https://luceeserver.atlassian.net/si/jira.issueviews:issue-xml/LDEV-5850/LDEV-5850.xml`

## Script Runner

Run CFML scripts headless using [script-runner](https://github.com/lucee/script-runner). Requires a local checkout (usually in the parent directory of this repo).

Build a custom JAR first with `ant fast` from `/loader`, then run:

```sh
ant -buildfile "../script-runner/build.xml" -DluceeJar="/full/path/to/loader/target/lucee-{version}.jar" -Dwebroot="D:\work\yourproject" -Dexecute="test.cfm"
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
