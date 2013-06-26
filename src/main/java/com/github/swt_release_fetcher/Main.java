/*
 * Copyright 2012 Jan-Hendrik Peters
 * Copyright 2012 Uri Shaked
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.swt_release_fetcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class Main {
	private static boolean deployArtifacts = false;
	private static boolean debug = false;
	private static boolean silentMode = false;
	private static final String WEBSITE_URL = "http://www.eclipse.org/swt/";

	public static void main(String[] args) throws Exception {

		for (String arg : args) {
			if (arg.equals("--deploy")) {
				deployArtifacts = true;
			}
			if (arg.equals("--help")) {
				showHelp();
			}
			if (arg.equals("--debug")) {
				debug = true;
			}
			if (arg.equals("--silent")) {
				silentMode = true;
			}
		}
		
		// the mirror we use for all following downloads
		String mirrorUrl = "";

		// lightweight headless browser
		WebDriver driver = new HtmlUnitDriver();

		// determine if the website has changed since our last visit
		// stop if no change was detected
		// Ignore this check if we just want to deploy
		if (! deployArtifacts) {
			SwtWebsite sw = new SwtWebsite();

			try {
				if (! sw.hasChanged(driver, WEBSITE_URL)) {
					// exit if no change was detected
					printSilent("SWT website hasn't changed since our last visit. Stopping here.");
					driver.quit();
					System.exit(0);
					} else {
					// proceed if the site has changed
					System.out.println("Page change detected! You may want to run the script in deploy mode again.");
				}
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}

		// get SWT's main site
		printDebug("Parsing eclipse.org/swt to find a mirror");
		driver.get(WEBSITE_URL);
		printDebug(WEBSITE_URL);

		// find the stable release branch link and hit it
		final List<WebElement> elements = driver.findElements(By.linkText("Linux"));
		final String deeplink = elements.get(0).getAttribute("href");
		printDebug("deeplink: " + deeplink);
		driver.get(deeplink);

		// get the direct download link from the next page
		final WebElement directDownloadLink = driver.findElement(By.linkText("Direct link to file"));
		printDebug("direct download link: " + directDownloadLink.getAttribute("href"));

		// the direct link again redirects, here is our final download link!
		driver.get(directDownloadLink.getAttribute("href"));
		final String finalDownloadLink = driver.getCurrentUrl();
		printDebug("final download link: " + finalDownloadLink);

		// Close the browser
		driver.quit();

		// extract the mirror URL for all following downloads
		String[] foo = finalDownloadLink.split("\\/", 0);
		final String filename = foo[foo.length - 1];
		mirrorUrl = (String) finalDownloadLink.subSequence(0, finalDownloadLink.length() - filename.length());
		// debug output
		printDebug("full download url: " + finalDownloadLink);
		printDebug("mirror url: " + mirrorUrl);

		// determine current release name
		String[] releaseName = filename.split("-gtk-linux-x86.zip");
		String versionName = releaseName[0].split("-")[1];
		System.out.println("current swt version: " + versionName);

		// TODO move to properties file
		PackageInfo[] packages = {
				// Win32
				new PackageInfo("win32-win32-x86.zip", "org.eclipse.swt.win32.win32.x86"),
				new PackageInfo("win32-win32-x86_64.zip", "org.eclipse.swt.win32.win32.x86_64"),
				// Linux
				new PackageInfo("gtk-linux-ppc.zip", "org.eclipse.swt.gtk.linux.ppc"),
				new PackageInfo("gtk-linux-ppc64.zip", "org.eclipse.swt.gtk.linux.ppc64"),
				new PackageInfo("gtk-linux-x86.zip", "org.eclipse.swt.gtk.linux.x86"),
				new PackageInfo("gtk-linux-x86_64.zip", "org.eclipse.swt.gtk.linux.x86_64"),
				new PackageInfo("gtk-linux-s390.zip", "org.eclipse.swt.gtk.linux.s390"),
				new PackageInfo("gtk-linux-s390x.zip", "org.eclipse.swt.gtk.linux.s390x"),
				// OSX
				new PackageInfo("cocoa-macosx.zip", "org.eclipse.swt.cocoa.macosx"),
				new PackageInfo("cocoa-macosx-x86_64.zip", "org.eclipse.swt.cocoa.macosx.x86_64"),
				// Additional platforms
				new PackageInfo("gtk-aix-ppc.zip", "org.eclipse.swt.gtk.aix.ppc"),
				new PackageInfo("gtk-aix-ppc64.zip", "org.eclipse.swt.gtk.aix.ppc64"),
				new PackageInfo("gtk-hpux-ia64.zip", "org.eclipse.swt.gtk.hpux.ia64"),
				new PackageInfo("gtk-solaris-sparc.zip", "org.eclipse.swt.gtk.solaris.sparc"),
				new PackageInfo("gtk-solaris-x86.zip", "org.eclipse.swt.gtk.solaris.x86") };

		File downloadDir = new File("downloads");
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}

		for (PackageInfo pkg : packages) {
			final String zipFileName = releaseName[0] + "-" + pkg.zipName;
			final URL downloadUrl = new URL(mirrorUrl + zipFileName);
			final URL checksumUrl = new URL(mirrorUrl + "checksum/" + zipFileName + ".md5");

			System.out.print("* Downloading " + pkg.zipName + " ... ");
			Artifact artifact = new Artifact(new File(downloadDir, zipFileName), versionName, pkg.artifactId);
			artifact.downloadAndValidate(downloadUrl, checksumUrl);

			if (deployArtifacts) {
				artifact.deploy();
			}
		}
	}

	/**
	 * Prints given debug message if debug mode is enabled.
	 * @param string
	 */
	private static void printDebug(String string) {
		if (debug) {
			System.out.println(string);
		}
	}

	/**
	 * print help dialogue
	 */
	private static void showHelp() {
		System.out.println("" +
				"--help		show this help messages \n" +
				"--deploy	check (m5sum) new and already existing artefacts and deploy them to http://swt-repo.googlecode.com/svn/repo/ \n" + 
				"--debug	print debug output while fetching releases. Per default no output is given. \n" + 
                                "--silent	suppress unnecessary output, useful for running as cron job. \n"
				);
		System.exit(0);
	}

    /**
     * prints messages to stdout if silentMode is disabled.
     * @param message The message to print
     */
    private static void printSilent(String message) {
        if (!silentMode) {
            System.out.println(message);
        }
    }
}