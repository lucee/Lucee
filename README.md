![Lucee](https://raw.githubusercontent.com/lucee/Lucee/6.0/images/lucee-white.png#gh-dark-mode-only)
![Lucee](https://raw.githubusercontent.com/lucee/Lucee/6.0/images/lucee-black.png#gh-light-mode-only)

## Lucee Server

Lucee Server (or simply Lucee) is a dynamic, Java based, tag and scripting language used for rapid web application development.   

Lucee simplifies technologies like webservices (REST, SOAP, HTTP), ORM (Hibernate), searching (Lucene), datasources (MSSQL, Oracle, MySQL and others), caching (infinispan, ehcache, and memcached) and many more. 

Lucee provides a compatibility layer for Adobe ColdFusion &copy;  CFML using less resources and delivering better performance. 

## What's New

- [Lucee 7.1](https://docs.lucee.org/guides/lucee-7-1.html)
- [Lucee 7](https://docs.lucee.org/guides/lucee-7.html)
- [Lucee 6.2](https://docs.lucee.org/guides/lucee-6.2.html)
- [Lucee 5.3 "Kabang"](https://docs.lucee.org/guides/lucee-5.3-kabang.html)
- [Lucee 5](https://docs.lucee.org/guides/lucee-5.html)
- Lucee 8.0 (coming soon)

## Changelogs

- [Changelogs per Release](https://download.lucee.org/changelog/)
- [New Tags & Functions, Arguments and Attributes](https://docs.lucee.org/reference/changelog.html)

### Breaking Changes

See all breaking changes documentation at [docs.lucee.org/recipes](https://docs.lucee.org/recipes.html):

- [Breaking Changes Between Lucee 6.2 and 7.0](https://docs.lucee.org/recipes/breaking-changes-7.html)
- [Breaking Changes Between Lucee 7.0 and 7.1](https://docs.lucee.org/recipes/breaking-changes-7-1.html)
- [Breaking Changes Between Lucee 7.1 and 8.0](https://docs.lucee.org/recipes/breaking-changes-8-0.html) (coming soon)

## Installation

You can [build Lucee from source](https://docs.lucee.org/guides/working-with-source.html) or download a distribution from [download.lucee.org](https://download.lucee.org/). We also provide [official Docker images and Dockerfiles](https://github.com/lucee/lucee-dockerfiles).

## LuCLI

[LuCLI](https://lucli.dev/) is a command-line interface for Lucee development and deployment. It provides server lifecycle management, CFML script execution, module system, dependency management, and AI integration — all from the terminal.

**Quick Start:**

```bash
curl -LsSf https://lucli.dev/install.sh | sh
# Windows:
powershell -ExecutionPolicy Bypass -NoProfile -Command "irm https://lucli.dev/install.ps1 | iex"
```

Requires Java 17+.

**Core Features:**

- **Server Management** — Start, stop, restart, and monitor Lucee servers with `lucli server` commands
- **CFML Execution** — Run `.cfs` scripts, `.cfm` templates, or `.cfc` components directly from the CLI
- **Project Configuration** — `lucee.json` for per-project server settings (port, Lucee version, JVM memory, environment variables)
- **Module System** — Create and install reusable CLI modules in `~/.lucli/modules/`
- **Dependency Management** — Manage CFML libraries and Lucee extensions with automatic git/Maven resolution
- **Secrets Management** — Store and inject encrypted secrets into your Lucee servers
- **AI Integration** — Configure LLM endpoints and run AI prompts with skill support
- **Daemon Mode** — Run LuCLI as a background service for programmatic access
- **Batch Scripts** — Execute sequences of commands with `.lucli` batch files

**Example `lucee.json`:**

```json
{
  "name": "my-project",
  "lucee": { "version": "6.2.2" },
  "port": 8080,
  "webroot": "./",
  "jvm": { "maxMemory": "512m" },
  "dependencies": {
    "cfwheels": { "type": "cfml", "source": "git", "url": "https://github.com/cfwheels/cfwheels" }
  }
}
```

**Documentation:** [lucli.dev/docs](https://lucli.dev/docs/) | **GitHub:** [cybersonic/LuCLI](https://github.com/cybersonic/LuCLI)

## AI Assistants & Developer Tools

### Lucee Skill for AI Assistants

A machine-readable skill so your AI assistant knows modern Lucee. Drop-in context for Cursor, Copilot, Warp, Claude, ChatGPT, and more. Always aligned with the latest docs.

- [skill.lucee-services.com/main.skill](https://skill.lucee-services.com/main.skill)
- [docs.lucee.org/lucee.skill](https://docs.lucee.org/lucee.skill)

### Lucee MCP Server

Hosted by Lucee, gives your AI callable tools for Lucee documentation and CFML code analysis.

**Tools:**
- `search_lucee_docs` — search functions, tags, and recipes in the Lucee docs
- `get_lucee_function` — full descriptor for a built-in function (arguments, types, examples)
- `get_lucee_tag` — full descriptor for a tag (attributes, types, examples)
- `parse_cfml_ast` — parse CFML source into an AST (tags, calls, control flow)
- `query_cfml_ast` — query an AST by node type, name, or line number

## Building Lucee from Source

You can find detailed instructions on how to [build Lucee from source](https://docs.lucee.org/guides/working-with-source.html).

Lucee has a lot of testcases, there are a number of handy [build flags](https://docs.lucee.org/guides/working-with-source/build-from-source.html#build-performance-tips) which can be used to avoid running the entire Lucee test suite (which takes a while) when you are working on a specific feature (Lucee 6.+ only).

Lucee uses [TestBox](https://testbox.ortusbooks.com/) as our test framework, we have a customised CFML wrapper around TestBox which can be found in the root directory under [/test](https://github.com/lucee/Lucee/tree/6.0/test). 

The test suites use a range of Test Services, like s3, database, ftp, etc. You can see how they are configured in [_setupTestServices.cfc](https://github.com/lucee/Lucee/blob/6.0/test/_setupTestServices.cfc) via the source code.

## Getting Help

- [Lucee Documentation Project](https://docs.lucee.org/)
- [Lucee Mailing List / Forum ](https://dev.lucee.org); General app developer support, hacking on Lucee itself
- [Lucee Bug Tracker](https://luceeserver.atlassian.net/projects/LDEV/issues)

If you are [looking for commercial support](https://lucee.org/members.html), you might try one of the LAS Members.

## System Requirements

Lucee is a JVM language running as a servlet on just about any servlet container; including [Apache Tomcat](http://tomcat.apache.org/), [Eclipse Jetty](http://eclipse.org/jetty/), [JBoss AS](http://jbossas.jboss.org/), and [GlassFish](https://glassfish.java.net/).

_We standardise on Apache Tomcat for installers and Docker containers as there is only so much time in the day._

## Philosophy

The Lucee team “treats slowness as a bug". Many performance tests have shown Lucee to perform faster than other CFML engines. 

Lucee attempts to resolve many inconsistencies found in traditional CFML; either forcing changes in language behavior, or providing configurable options in the Lucee Administrator.

For more peruse the [Lucee Manifesto](https://dev.lucee.org/t/lucee-manifesto/183).

The Lucee team is always open to feedback and active at CFML community events, and is keen to remind people that Lucee is a community project.

Our Release approach follows the [The Tip & Tail Model of Library Development](https://openjdk.org/jeps/14) approach as used by the Java JDK

![GitHub](https://img.shields.io/github/license/lucee/Lucee)
5.3 [![Java CI](https://github.com/lucee/Lucee/actions/workflows/main.yml/badge.svg?branch=5.3)](https://github.com/lucee/Lucee/actions/workflows/main.yml)
5.4 [![Java CI](https://github.com/lucee/Lucee/actions/workflows/main.yml/badge.svg?branch=5.4)](https://github.com/lucee/Lucee/actions/workflows/main.yml)
6.0 [![Java CI](https://github.com/lucee/Lucee/actions/workflows/main.yml/badge.svg?branch=6.0)](https://github.com/lucee/Lucee/actions/workflows/main.yml)
[![Backers on Open Collective](https://opencollective.com/Lucee/backers/badge.svg)](#backers)
[![Sponsors on Open Collective](https://opencollective.com/Lucee/sponsors/badge.svg)](#sponsors) 

[![Maven Central](https://img.shields.io/maven-central/v/org.lucee/lucee)](https://mvnrepository.com/artifact/org.lucee/lucee)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/lucee/Lucee)](https://github.com/lucee/Lucee/pulls)
[![GitHub closed pull requests](https://img.shields.io/github/issues-pr-closed-raw/lucee/Lucee)](https://github.com/lucee/Lucee/pulls?utf8=%E2%9C%93&q=is%3Apr+is%3Aclosed)

[![docker pulls](https://img.shields.io/docker/pulls/lucee/lucee.svg?label=docker+pulls)](https://hub.docker.com/r/lucee/lucee/)
[![Open Collective backers and sponsors](https://img.shields.io/opencollective/all/lucee)](https://opencollective.com/lucee#section-contributors)
[![GitHub contributors](https://img.shields.io/github/contributors/lucee/Lucee)](https://github.com/lucee/Lucee/graphs/contributors)

[![Website](https://img.shields.io/website?url=https%3A%2F%2Fdownload.lucee.org%2F)](https://download.lucee.org/)


## Contributors

This project exists thanks to all the people who contribute. [[Contribute](CONTRIBUTING.md)].
<a href="https://github.com/lucee/Lucee/graphs/contributors"><img src="https://opencollective.com/Lucee/contributors.svg?width=890&button=false" /></a>


## Backers

Thank you to all our backers! 🙏 [[Become a backer](https://opencollective.com/Lucee#backer)]

<a href="https://opencollective.com/Lucee#backers" target="_blank"><img src="https://opencollective.com/Lucee/backers.svg?width=890"></a>


## Sponsors

Support this project by becoming a sponsor. Your logo will show up here with a link to your website. [[Become a sponsor](https://opencollective.com/Lucee#sponsor)]

<a class="custom-sponsor" href="https://www.mitrahsoft.com" target="_blank"><img src="https://www.mitrahsoft.com/assets/img/lucee-sponsor.svg?t=6"></a>
<a href="https://opencollective.com/Lucee/sponsor/0/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/0/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/1/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/1/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/2/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/2/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/3/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/3/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/4/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/4/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/5/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/5/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/6/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/6/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/7/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/7/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/8/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/8/avatar.svg"></a>
<a href="https://opencollective.com/Lucee/sponsor/9/website" target="_blank"><img src="https://opencollective.com/Lucee/sponsor/9/avatar.svg"></a>

## Copyright / License

Copyright 2006-2014 Various contributing authors
Copyright 2015-2021 Lucee Association Switzerland

The Lucee code base was forked from the [Railo Server Project](https://en.wikipedia.org/wiki/Railo) (Version 4.2) in January 2015. The Lucee Association Switzerland  (LAS) is the legal custodian of the code base, and contributors are required accept the [LAS Contributor License Agreement (CLA)](https://dev.lucee.org/t/las-contributor-license-agreement-cla/181).

Lucee Server is licensed under the Lesser GNU General Public License Version 2.1 (or later); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:
[http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Lucee logo and related marks belong to [Lucee Association Switzerland](https://lucee.org/).
