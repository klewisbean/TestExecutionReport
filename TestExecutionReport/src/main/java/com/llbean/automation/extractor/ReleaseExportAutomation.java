package com.llbean.automation.extractor; /**
 * Created by klewis on 6/8/2016.
 */

import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;


import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.HashMap;


public class ReleaseExportAutomation {

    public static int COUNT;
    public WebDriver driver = setUpDriver();

    public String atlassianllbeanhome = "https://llbean.atlassian.net/";
    public String searchtestexecutions = "plugins/servlet/ac/com.thed.zephyr.je/general-executions-enav?project.key=CTTCM&project.id=13000#!view=list&offset=1";
    public String ZQL_SEARCH = "";
    final static Logger logger = Logger.getLogger(ReleaseExportAutomation.class);
    public ReleaseExportAutomation(String username, String password, String zqlsearch, int count){

        COUNT = count;
        driver.get("https://llbean.atlassian.net/login");
        ZQL_SEARCH = zqlsearch;

        login(username, password);


        testExecutionNav();
        exportData();

    }

    public WebDriver setUpDriver(){
        logger.info("setting up driver...");
        logger.info("download path: " + System.getProperty("user.dir"));
        String downloadFilepath = System.getProperty("user.dir");//path where files will be download when automating in chrome
        HashMap<String, Object> chromePrefs = new HashMap<String, Object>();
        chromePrefs.put("profile.default_content_settings.popups", 0);
        chromePrefs.put("download.default_directory", downloadFilepath);
        chromePrefs.put("safebrowsing.enabled", "true");
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);
        options.addArguments("--no-sandbox");//fixes the "unable to discover pages error" in jenkins
        DesiredCapabilities cap = DesiredCapabilities.chrome();
        cap.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        cap.setCapability(ChromeOptions.CAPABILITY, options);
        WebDriver driver = new ChromeDriver(cap);
        logger.info("custom driver has been set up");
        return driver;
    }

    public void login(String username, String password){

        logger.info("logging in...");
        driver.manage().window().maximize();
        //enter in the username to login
        driver.findElement(By.id("username")).sendKeys(username);
        //enter in the password to login
        driver.findElement(By.id("password")).sendKeys(password);
        //click the log in button
        driver.findElement(By.id("login")).click();



    }

    public void testExecutionNav(){
        logger.info("Login Successful");
        logger.info("Title: " + driver.getTitle() + " URL: " + driver.getCurrentUrl());
      //navigate to the "search test execution page"
        driver.get(atlassianllbeanhome + searchtestexecutions);

        logger.info("Title: " + driver.getTitle() + " URL: " + driver.getCurrentUrl());
        try {
            driver.switchTo().frame(0);
        }
        catch(NoSuchFrameException e){
            System.out.println("no such frame");
        }

        logger.info("waiting for element to appear...");
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        WebElement navitatorContainer = driver.findElement(By.className("navigator-container"))
                                            .findElement(By.id("zqlcomponent"))
                                            .findElement(By.className("contained-content"))
                                            .findElement(By.id("navigator-wrapper"))
                                            .findElement(By.className("zql-autocomplete"))
                                            .findElement(By.className("search-wrap"))
                                            .findElement(By.id("search-container"))
                                            .findElement(By.className("search-options-container"))
                                            .findElement(By.className("mode-switcher"))
                                            .findElement(By.id("search-mode-advanced"));

        //click the advanced search button
        navitatorContainer.click();
        logger.info("element found");

        WebElement advanceInput  = driver.findElement(By.className("navigator-container"))
                                        .findElement(By.id("zqlcomponent"))
                                        .findElement(By.className("contained-content"))
                                        .findElement(By.id("navigator-wrapper"))
                                        .findElement(By.className("zql-autocomplete"))
                                        .findElement(By.className("search-wrap"))
                                        .findElement(By.id("search-container"))
                                        .findElement(By.className("search-options-container"))
                                        .findElement(By.id("search-field-container"))
                                        .findElement(By.className("advanced-search-container"))
                                        .findElement(By.className("aui-group"))
                                        .findElement(By.id("zql-autocomplete"))
                                        .findElement(By.id("zqltext"));

        advanceInput.sendKeys(ZQL_SEARCH);

        WebElement advanceSearchButton = null;
        try {
            advanceSearchButton = driver.findElement(By.className("navigator-container"))
                    .findElement(By.id("zqlcomponent"))
                    .findElement(By.className("contained-content"))
                    .findElement(By.id("navigator-wrapper"))
                    .findElement(By.className("zql-autocomplete"))
                    .findElement(By.className("search-wrap"))
                    .findElement(By.id("search-container"))
                    .findElement(By.className("search-options-container"))
                    .findElement(By.id("search-field-container"))
                    .findElement(By.className("advanced-search-container"))
                    .findElement(By.className("aui-group"))
                    .findElement(By.id("zephyr-transform-all"));
        }
        catch (Exception e) {
            System.out.println("Element is not present yet");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e2) {
                e.printStackTrace();
            }
        }

        advanceSearchButton.click();

    }

    //click the export xml button which will download the xml file to the local system "downloads" folder
    public void exportData(){

        //driver.switchTo().frame(driver.findElement(By.xpath("//*[@id=\"easyXDM_embedded-com.thed.zephyr.je__general-executions-enav_provider\"]")));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //find the export dropdown and click it
        WebElement exportdropdown = driver.findElement(By.className("navigator-container"))
                .findElement(By.id("zqlcomponent"))
                .findElement(By.className("contained-content"))
                .findElement(By.id("saved-search-wrapper"))
                .findElement(By.className("saved-search-operations"))
                .findElement(By.className("operations"))
                .findElement(By.id("enav-export-wrapper"))
                .findElement(By.id("export-dropdown2-link"));

        try{
            exportdropdown.click();
        } catch(Exception e){
            System.out.println(e.getMessage());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException j) {
                j.printStackTrace();
            }
            exportdropdown.click();
        }

        //find the xml export button from the drop down list
        WebElement xmlexport = driver.findElement(By.id("xmlExecutionId"));

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //click the xml export button
        xmlexport.click();
        waitfordownload();
        driver.quit();

    }

    public static void waitfordownload(){
        logger.trace("count:"+COUNT);

        if(COUNT == 1){
            for(int i = 1; i < 150; i++){
                System.out.println((5*i) + " seconds");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("done");
        }
        else if(COUNT == 2){
            for(int i = 1; i < 48; i++){
                System.out.println((5*i) + " seconds");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("done");
        }

    }







}
