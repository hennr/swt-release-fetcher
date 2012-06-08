package com.github.swt_release_fetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class Main {
	private final static boolean SURPRESS_WARNINGS = true;

	public static void main(String[] args) throws Exception {

		// suppress htmlUnit warnings
		if (SURPRESS_WARNINGS) {
			System.getProperties().put("org.apache.commons.logging.simplelog.defaultlog", "error");
		}

		// mirror that we use for all following downloads
		String mirrorUrl = "";

		// lightweight headless browser
		WebDriver driver = new HtmlUnitDriver();

		// get swt main site
		driver.get("http://www.eclipse.org/swt/");

		// find the stable release branch link and hit it
		List<WebElement> elements = driver.findElements(By.linkText("Linux"));
		final String deeplink = elements.get(0).getAttribute("href");
		// System.out.println(deeplink);
		driver.get(deeplink);

		// get the direct download link from the next page
		WebElement directDownloadLink = driver.findElement(By.linkText("Direct link to file"));
		// System.out.println(directDownloadLink.getAttribute("href"));

		// the direct link again redirects, here is our final download link!
		driver.get(directDownloadLink.getAttribute("href"));
		final String finalDownloadLink = driver.getCurrentUrl();

		// extract the mirror URL for all follwing downloads
		String[] foo = finalDownloadLink.split("\\/", 0);
		final String filename = foo[foo.length - 1];
		mirrorUrl = (String) finalDownloadLink.subSequence(0, finalDownloadLink.length() - filename.length());
		// debug output
		System.out.println("full download url: " + finalDownloadLink);
		System.out.println("mirror url: " + mirrorUrl);

		// determine current release name
		String[] releaseName = filename.split("-gtk-linux-x86.zip");
		System.out.println("current swt release: " + releaseName[0]);

		String[] dowloadNames = {
				// win32
				"win32-win32-x86.zip",
				// win64
				"win32-win32-x86_64.zip",
				// linux 32
				"gtk-linux-x86.zip",
				// linux 64
				"gtk-linux-x86_64.zip",
				// mac os X 32
				"cocoa-macosx.zip",
				// mac os X 32
				"cocoa-macosx-x86_64.zip" };

		File downloadDir = new File("downloads");
		if (!downloadDir.exists()) {
			downloadDir.mkdirs();
		}

		for (String download : dowloadNames) {
			URL downloadUrl = new URL(mirrorUrl + releaseName[0] + "-" + download);
			URL checksumUrl = new URL(mirrorUrl + "checksum/" + releaseName[0] + "-" + download + ".md5");
			System.out.println("* Downloading " + download + " ...");

			downloadAndChecksum(downloadUrl, checksumUrl, new File(downloadDir, download));
		}

		// Close the browser
		driver.quit();
	}

	private static void downloadAndChecksum(URL downloadUrl, URL checksumUrl, File file) throws IOException {
		String md5FileContent = IOUtils.toString(checksumUrl.openStream());
		String md5Hash = md5FileContent.split(" ")[0];
		OutputStream outputFile = new FileOutputStream(file);
		try {
			IOUtils.copy(downloadUrl.openStream(), outputFile);
		} finally {
			outputFile.close();
		}
		InputStream downloadedFile = new FileInputStream(file);
		try {
			String calculatedMd5 = DigestUtils.md5Hex(downloadedFile);
			if (!calculatedMd5.equals(md5Hash)) {
				throw new IOException("MD5 Validation Failed! Calculated: " + calculatedMd5 + ", expected: " + md5Hash);
			}
		} finally {
			downloadedFile.close();
		}
	}
}