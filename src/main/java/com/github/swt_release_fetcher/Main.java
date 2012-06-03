package com.github.swt_release_fetcher;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class Main  {
    public static void main(String[] args) {

    	// suppress htmlUnit warnings
    	//System.getProperties().put("org.apache.commons.logging.simplelog.defaultlog", "error");
    	
    	// mirror that we use for all following downloads
    	String mirrorUrl = "";
    	
    	// lightweight headless browser    	
        WebDriver driver = new HtmlUnitDriver();
        
	    // get swt main site
	    driver.get("http://www.eclipse.org/swt/");
	
	    // find the stable release branch link and hit it
	    List<WebElement> elements = driver.findElements(By.linkText("Linux"));
	    final String deeplink = elements.get(0).getAttribute("href");
	    //System.out.println(deeplink);
	    driver.get(deeplink);
	    
	    // get the direct download link from the next page
	    WebElement directDownloadLink = driver.findElement(By.linkText("Direct link to file"));
	    //System.out.println(directDownloadLink.getAttribute("href"));
	    
	    // the direct link again redirects, here is our final download link!
	    driver.get(directDownloadLink.getAttribute("href"));
	    final String finalDownloadLink = driver.getCurrentUrl();
	    
	    // extract the mirror URL for all follwing downloads
	    String[] foo = finalDownloadLink.split("\\/", 0);
	    final String filename = foo[foo.length-1];
	    mirrorUrl = (String) finalDownloadLink.subSequence(0, finalDownloadLink.length()-filename.length());
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
	    		"cocoa-macosx-x86_64.zip"
	    };
	    
	    for (String download: dowloadNames) {
	    	System.out.println(mirrorUrl + releaseName[0] + "-" + download);
	    	// TODO md5sums
	    	System.out.println(mirrorUrl + "checksum/" + releaseName[0] + "-" +  download + ".md5");
	    }
	    
        //Close the browser
        driver.quit();
    }
}