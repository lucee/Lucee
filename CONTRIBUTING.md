# How to contribute

Thanks for considering contributing to the Lucee Server platform.

We love community input and we'll look at your pulls as soon as we can, but keep in mind we may not be able to merge your changes right away if they require a lot of review.

If you want to make a large change, consider posting on our [Developer Forum](https://dev.lucee.org/) first to get direction.

# Lucee Core vs Extensions

This repo contains the code for the main Lucee engine.

If you would like to help improve an extension, please find the appropriate repo for it.

## Getting Started

* Make sure you have a [JIRA account](https://luceeserver.atlassian.net/)
* Make sure you have a GitHub account
* File a proposal on the developer forum first
* Then once it's been discussed, file a ticket for your issue, assuming one does not already exist.
* Clearly describe the issue, including steps to reproduce when it is a bug, use the labels in jira.
* Make sure you fill in the earliest version that you know has the issue.
* Fork the repository on GitHub, start coding and running tests!

Please read our docs on [working with the Lucee source code](https://docs.lucee.org/guides/working-with-source.html).

## Branch Status

* `7.0` — active stable branch
* `7.1` / `8.0` — in-flight development branches for upcoming releases
* `6.2` — active LTS branch, bugfixes only
* `6.0`, `5.x`, `4.x` — EOL

## Java Version

Java 11 is the baseline — all artifacts are compiled to bytecode targeting Java 11.

Builds and distributions use Java 21.

## Submission guidelines

* Please do not send pull requests to a `master`/`main` branch.
* New work targets the appropriate version branch — `7.0` for the active stable, `6.2` for LTS bugfixes, `7.1`/`8.0` for upcoming-release work.
* Create a feature branch off the version branch for each pull you want to create.
* Please include the ticket number in your commit messages.
* Commit messages use the following style `LDEV-007 Add support to James Bond's watch for OSGI bundles`
* Please include a link to the ticket number in your pull request.
* Make sure your branch is rebased with the latest changes from the upstream repo before submitting your pull
* Create or update **unit tests** for your feature/change/bug fix and ensure all existing tests are passing

## Documentation

If your change affects a documented feature of Lucee, please submit a pull to our [doc site](https://docs.lucee.org/) as well. The Lucee docs are stored in a GitHub repo as markdown.

## Additional Resources

* [Lucee Site](https://lucee.org/)
* [Lucee docs](https://docs.lucee.org/)
* [Bug Tracker](https://luceeserver.atlassian.net/)
* [Lucee Developer forum](https://dev.lucee.org/) (For discussions about improving Lucee)
