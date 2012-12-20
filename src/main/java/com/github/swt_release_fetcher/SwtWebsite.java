/*
 * Copyright 2012 Jan-Hendrik Peters
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
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;

/**
 * @author Jan-Hendrik Peters
 */
public class SwtWebsite {

    /**
     * Determines if the website has changed since our last visit. Uses a file
     * called ''pageSource'' to persist the site
     *
     * @param driver
     * @param websiteUrl URL to SWT's project site
     * @return true if the site has changed, false if not. Also returns true on
     * the first run
     * @throws IOException If the file ''pageSource'' coudln't be read or
     * written
     */
    public boolean hasChanged(WebDriver driver, String websiteUrl) throws IOException {

        driver.get(websiteUrl);
        String pageSoruce = driver.getPageSource();
        String persistedPageSource = "";
        File f = new File("pageSource");
        // create a new file to persist page source
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ieo) {
                throw new IOException("Unable to create file " + f.getAbsolutePath(), ieo);
            }
            // if it was already there, this is not the first run of swt-release-fetcher
            // read in the file content
        } else {
            try {
                persistedPageSource = FileUtils.readFileToString(f, "utf-8");
            } catch (IOException ieo) {
                throw new IOException("Unable to read file " + f.getAbsolutePath(), ieo);
            }
        }

        // check if the page has changed
        if (persistedPageSource.equals(pageSoruce)) {
            return false;
            // NOTE: If this is the first run of swt-release-fethcer the file 
            // will be empty and thus filled with content here
        } else {
            try {
                FileUtils.writeStringToFile(f, pageSoruce);
                return true;
            } catch (IOException ieo) {
                throw new IOException("Unable to write to file " + f.getAbsolutePath(), ieo);
            }

        }
    }
}
