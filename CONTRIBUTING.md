# How to contribute

Thanks for considering contributing to the Lucee Server platform.  We love community input and we'll look at your pulls as soon as we can, but keep in mind we may not be able to merge your changes right away if they require a lot of review.
If you have a large change, consider posting on our [mailing list](https://groups.google.com/forum/?hl=en#!forum/lucee) or [Discourse forum](https://lang.lucee.org/) first to get direction.

# Lucee core vs extensions

This repo contains the code for the main Lucee engine.  If you would like to improve an extension, please find the appropriate repo for it.

# Getting Started

* Make sure you have a JIRA account https://luceeserver.atlassian.net/
* Make sure you have a GitHub account
* Submit a ticket for your issue, assuming one does not already exist.
* Clearly describe the issue including steps to reproduce when it is a bug.
* Make sure you fill in the earliest version that you know has the issue.
* Fork the repository on GitHub

Please read our docs on working with the Lucee source code: https://docs.lucee.org/guides/working-with-source.html

# Submission guidelines

* Please do not send pull requests to the `master` branch.  All new development happens on a version branch such as `5.0` and is merged into `master` once it is released.
* Create a feature branch off the version branch for each pull you want to create.  
* Please include the ticket number in your commit messages.
* Make sure your branch is rebased with the latest changes  from the upstream repo before submitting your pull
* Create a new unit tests for your feature or bug fix and ensure all existing tests are passing

# Documentation

If your change affects a documented feature of Lucee, please submit a pull to our doc site as well.  The Lucee docs are stored in a GitHub repo as markdown.
https://docs.lucee.org/

# Additional Resources

* [Lucee Site](https://lucee.org/)
* [Lucee docs](https://docs.lucee.org/) 
* [Bug Tracker](https://luceeserver.atlassian.net/)
* [Mailing list](https://groups.google.com/forum/?hl=en#!forum/lucee) (For general help using Lucee)
* [Lucee discourse forum](https://lang.lucee.org/) (For discussions about improving Lucee)
