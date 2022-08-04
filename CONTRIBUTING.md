# How to contribute

Thanks for considering contributing to the Lucee Server platform.  

We love community input and we'll look at your pulls as soon as we can, but keep in mind we may not be able to merge your changes right away if they require a lot of review.

If you want to make a large change, consider posting on our [mailing list](https://dev.lucee.org/) first to get direction.

# Lucee Core vs Extensions

This repo contains the code for the main Lucee engine.  

If you would like to help improve an extension, please find the appropriate repo for it.

# Getting Started

* Make sure you have a JIRA account https://luceeserver.atlassian.net/
* Make sure you have a GitHub account
* File a proposal on the mailing list first
* Then once it's been discussed, file a ticket for your issue, assuming one does not already exist.
* Clearly describe the issue, including steps to reproduce when it is a bug, use the labels in jira.
* Make sure you fill in the earliest version that you know has the issue.
* Fork the repository on GitHub, start coding and running tests!

Please read our docs on working with the Lucee source code: https://docs.lucee.org/guides/working-with-source.html

# Branch Status

6.0 is the active development branch, any new cool stuff should be done against this branch

5.3 is our LTS branch, mainly only bugfixes

4.5 is totally EOL

# Java Version

Java 8 is still our base line, we recommend Java 11 for production, but as long as we can support Java 8, we will.

# Submission guidelines

* Please do not send pull requests to the `master` branch.  
* All new development happens on a major version branch such as `6.0`.
* Create a feature branch off the version branch for each pull you want to create.  
* Please include the ticket number in your commit messages. 
* Commit messages use the following style `LDEV-007 Add support to James Bond's watch for OSGI bundles`
* Please include a link to the ticket number in your pull request. 
* Make sure your branch is rebased with the latest changes from the upstream repo before submitting your pull
* Create or update **unit tests** for your feature/change/bug fix and ensure all existing tests are passing

# Documentation

If your change affects a documented feature of Lucee, please submit a pull to our doc site as well.  The Lucee docs are stored in a GitHub repo as markdown.
https://docs.lucee.org/

# Additional Resources

* [Lucee Site](https://lucee.org/)
* [Lucee docs](https://docs.lucee.org/) 
* [Bug Tracker](https://luceeserver.atlassian.net/)
* [Lucee discourse forum](https://dev.lucee.org/) (For discussions about improving Lucee)
