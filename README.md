[![Build Status](https://secure.travis-ci.org/hennr/swt-release-fetcher.png?branch=master)](http://travis-ci.org/hennr/swt-release-fetcher)

swt-release-fetcher
===================

fetches SWT releases for the three major platforms

swt-release-fetcher makes use of selenium to scrape www.eclipse.org/swt/ and get all SWT download links for Linux, Windows and Mac OS X (32 and 64 bit versions).
The goal is to download, unpack and release the included jar files to a configurable mvn repository.

Use like this:
=============
mvn clean package<br>
java -jar target/swt-release-fetcher-<version>-jar-with-dependencies.jar