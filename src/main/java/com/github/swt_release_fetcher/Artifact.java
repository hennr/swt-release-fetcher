package com.github.swt_release_fetcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

public class Artifact {
	private final File file;
	private boolean newDownload;

	public Artifact(File file) {
		super();
		this.file = file;
	}

	private boolean validateFileMd5(File file, String expected) throws IOException {
		InputStream downloadedFile = new FileInputStream(file);
		try {
			String calculatedMd5 = DigestUtils.md5Hex(downloadedFile);
			return calculatedMd5.equals(expected);
		} finally {
			downloadedFile.close();
		}
	}

	public void downloadAndValidate(URL downloadUrl, URL checksumUrl) throws IOException {
		String md5FileContent = IOUtils.toString(checksumUrl.openStream());
		String md5Hash = md5FileContent.split(" ")[0];
		if (file.exists() && validateFileMd5(file, md5Hash)) {
			System.out.println("SKIP");
			return;
		}

		OutputStream outputFile = new FileOutputStream(file);
		try {
			IOUtils.copy(downloadUrl.openStream(), outputFile);
		} finally {
			outputFile.close();
		}
		if (!validateFileMd5(file, md5Hash)) {
			throw new IOException("MD5 Validation Failed! Expected hash: " + md5Hash);
		}
		newDownload = true;
		System.out.println("DONE");
	}

	public File getFile() {
		return file;
	}

	public boolean isNewDownload() {
		return newDownload;
	}
}
