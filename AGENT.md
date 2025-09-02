# Lucee Server

Lucee Server is an open source CFML Server which gets deployed via a Java Servlet.
The Lucee code base was forked from the Railo Server Project (Version 4.2) in January 2015.

## Architecture

- Documentation is published at `https://docs.lucee.org/`
- Java baseline version is 11

## Folder Structure

- `/ant`: Ant Build scripts
- `/loader`: The Loader Interface API used by for Lucee Core and its extensions, do not modify any interfaces
- `/core`: The main source code for Lucee Server
- `/test`: Contains the CFML Test suites

## Build & Commands

- Build, in the `/loader` directory, execute `mvn fast`
- Build and test, in the `/loader` directory, execute `mvn test`
- Build and execute a specific CFML test suite in the `/loader` directory, execute `mvn test -DtestFilter="{testFilename}"`

### Development Environment

- Build requires Java, Maven and Ant
- Build usually is run with Java 21
- All artifacts are compiled to bytecode targeting Java 11

## Contribution Workflow

- Before starting work, consider filing a proposal on the mailing list.
- File a ticket for your issue on JIRA, assuming one does not already exist.
- Fork the repository on GitHub.
- Create a feature branch off the appropriate version branch. `7.0` is the active development branch. `6.2` is the active stable branch. `5.4` is for LTS security fixes.
- Create or update unit tests for your changes.
- Make sure your branch is rebased with the latest changes from the upstream repo before submitting.
- Commit messages must include the ticket number, e.g., `LDEV-007 Add support to James Bond's watch for OSGI bundles`.
- Include a link to the JIRA ticket in your pull request description.
- All contributors must accept the LAS Contributor License Agreement (CLA).

### Documentation

If your change affects a documented feature, please also submit a pull request to the Lucee docs repo.

## Code Style

- Follow the Eclipse settings for Java code in `/org.eclipse.jdt.core.prefs`
- Use Tabs for indentation (2 spaces for YAML/JSON/MD)
- Avoid adding excessively verbose comments
- Never use Lucee in naming, use cfml instead

## Testing

- CFML Tests are written using TestBox [TestBox](https://testbox.ortusbooks.com/)
- All CFML tests should extend `org.lucee.cfml.test.LuceeTestCase`
- CFML tests should not use Java unless absolutely required, prefer CFML functionality.
- Tests should cleanup after themselves and any temporary files should be created under the directory returned from `getTempDirectory()`
- Test framework code, specifically files in the root of the `/test` directory should be compatible with Lucee 5.4, therefore, do not use newer cfml functionality.

## Security

- Use appropriate data types that limit exposure of sensitive information
- Never commit secrets or API keys to the repository
- Use environment variables for sensitive data
- Validate all user inputs on both client and server
- Follow the principle of least privilege

### Reporting a Vulnerability

Please send an email to security@lucee.org to report a vulnerability.

## Configuration

When adding new configuration options, update all relevant places:

1. Variables are always strings and should be cast to the correct type, with an appropriate default.
2. Variables should be read once into a static variable using `getSystemPropOrEnvVar(String name, String defaultValue)`
3. Document variables in `core/src/main/java/resource/setting/sysprop-envvar.json`

When updating a Java library

1. Update both the `pom.xml` files under `/loader` and `/core`
2. Update the corresponding entry under `Require-Bundle:` in `core/src/main/java\META-INF\MANIFEST.MF`

## Getting Help

- [Lucee Documentation](https://docs.lucee.org/)
- [Lucee Mailing List / Forum](https://dev.lucee.org/)
- [Lucee Bug Tracker](https://luceeserver.atlassian.net/) 

## License

Lucee Server is licensed under the Lesser GNU General Public License Version 2.1 (or later).
