![Lucee](https://bitbucket.org/repo/rX87Rq/images/3392835614-logo-1-color-black-small.png)

![GitHub](https://img.shields.io/github/license/lucee/Lucee)
5.3 [![Java CI](https://github.com/lucee/Lucee/actions/workflows/main.yml/badge.svg?branch=5.3)](https://github.com/lucee/Lucee/actions/workflows/main.yml)
6.0 [![Java CI](https://github.com/lucee/Lucee/actions/workflows/main.yml/badge.svg?branch=6.0)](https://github.com/lucee/Lucee/actions/workflows/main.yml)
[![Backers on Open Collective](https://opencollective.com/Lucee/backers/badge.svg)](#backers)
[![Sponsors on Open Collective](https://opencollective.com/Lucee/sponsors/badge.svg)](#sponsors) 

[![Maven Central](https://img.shields.io/maven-central/v/org.lucee/lucee)](https://mvnrepository.com/artifact/org.lucee/lucee)
[![GitHub pull requests](https://img.shields.io/github/issues-pr-raw/lucee/Lucee)](https://github.com/lucee/Lucee/pulls)
[![GitHub closed pull requests](https://img.shields.io/github/issues-pr-closed-raw/lucee/Lucee)](https://github.com/lucee/Lucee/pulls?utf8=%E2%9C%93&q=is%3Apr+is%3Aclosed)

[![docker pulls](https://img.shields.io/docker/pulls/lucee/lucee.svg?label=docker+pulls)](https://hub.docker.com/r/lucee/lucee/)
[![Open Collective backers and sponsors](https://img.shields.io/opencollective/all/lucee)](https://opencollective.com/lucee#section-contributors)
[![GitHub contributors](https://img.shields.io/github/contributors/lucee/Lucee)](https://github.com/lucee/Lucee)

[![Website](https://img.shields.io/website?url=https%3A%2F%2Fdownload.lucee.org%2F)](https://download.lucee.org/)

## Lucee Server

Lucee Server (or simply Lucee) is a dynamic, Java based, tag and scripting language used for rapid web application development.   

Lucee simplifies technologies like webservices (REST, SOAP, HTTP), ORM (Hibernate), searching (Lucene), datasources (MSSQL, Oracle, MySQL and others), caching (infinispan, ehcache, and memcached) and many more. 

Lucee provides a compatibility layer for Adobe ColdFusion &copy;  CFML using less resources and delivering better performance. 

## Installation

You can [build Lucee from source](https://docs.lucee.org/guides/working-with-source.html) or grab one of our distributions:

- [Lucee Express](https://lucee.org/downloads.html) (just unzip and run; delete to clean up)
- [Installers for Windows, Linux and OSX](https://lucee.org/downloads.html)
- [Official Dockerfiles and Docker images](https://github.com/lucee/lucee-dockerfiles)

Alternatively try the super-useful [CommandBox](https://www.ortussolutions.com/products/commandbox) standalone developer tools for CFML powered by Lucee.

## Building Lucee from Source

You can find detailed instructions on how to build Lucee [build Lucee from source](https://docs.lucee.org/guides/working-with-source.html)

Lucee has a lot of testcases, there are a number of handy [build flags](https://docs.lucee.org/guides/working-with-source/build-from-source.html#build-performance-tips) which can be used to avoid running the entire Lucee test suite (which takes a while) when you are working on a specific feature (Lucee 6.+ only).

Lucee uses [Testbox](https://testbox.ortusbooks.com/) as our test framework, we have a customised cfml wrapper around testbox which can be found in the root directory under [/test](https://github.com/lucee/Lucee/tree/6.0/test). 

The test suites use a range of Test Services, like s3, database, ftp, etc. You can see how they are configured in [_setupTestServices.cfc](https://github.com/lucee/Lucee/blob/6.0/test/_setupTestServices.cfc) via the source code.

## Getting Help

- [Lucee Documentation Project](https://docs.lucee.org/)
- [Lucee Google Group](https://groups.google.com/forum/#!forum/lucee); general app developer support
- [Lucee Lang Forum](http://lang.lucee.org/); hacking on Lucee itself
- [Lucee Bug Tracker](https://luceeserver.atlassian.net/projects/LDEV/issues)

If you are [looking for commercial support](https://lucee.org/members.html), you might try one of the LAS Members.

## System Requirements

Lucee is a JVM language running as a servlet on just about any servlet container; including [Apache Tomcat](http://tomcat.apache.org/), [Eclipse Jetty](http://eclipse.org/jetty/), [JBoss AS](http://jbossas.jboss.org/), and [GlassFish](https://glassfish.java.net/).

_We standardise on Apache Tomcat for installers and Docker containers as there is only so much time in the day._

## Philosophy

The Lucee team “treats slowness as a bug". Many performance tests have shown Lucee to perform faster than other CFML engines. 

Lucee attempts to resolve many inconsistencies found in traditional CFML; either forcing changes in language behavior, or providing configurable options in the Lucee Administrator.

For more peruse the [Lucee Manifesto](http://lang.lucee.org/t/lucee-manifesto/183).

The Lucee team is always open to feedback and active at CFML community events, and is keen to remind people that Lucee is a community project.

## Contributors

This project exists thanks to all the people who contribute. [[Contribute](CONTRIBUTING.md)].
<a href="https://github.com/lucee/Lucee/graphs/contributors"><img src="https://opencollective.com/Lucee/contributors.svg?width=890&button=false" /></a>


## Backers

Thank you to all our backers! 🙏 [[Become a backer](https://opencollective.com/Lucee#backer)]

<a href="https://opencollective.com/Lucee#backers" target="_blank"><img src="https://opencollective.com/Lucee/backers.svg?width=890"></a>


## Sponsors

Support this project by becoming a sponsor. Your logo will show up here with a link to your website. [[Become a sponsor](https://opencollective.com/Lucee#sponsor)]

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

The Lucee code base was forked from the [Railo Server Project](https://en.wikipedia.org/wiki/Railo) (Version 4.2) in January 2015. The Lucee Association Switzerland  (LAS) is the legal custodian of the code base, and contributors are required accept the [LAS Contributor License Agreement (CLA)](http://lang.lucee.org/t/las-contributor-license-agreement-cla/181).

Lucee Server is licensed under the Lesser GNU General Public License Version 2.1 (or later); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:
[http://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt](https://www.gnu.org/licenses/old-licenses/lgpl-2.1.txt)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

Lucee logo and related marks belong to [Lucee Association Switzerland](https://lucee.org/).
