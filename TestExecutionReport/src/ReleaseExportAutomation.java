/**
 * Created by klewis on 6/8/2016.
 */

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class ReleaseExportAutomation {


    public WebDriver driver = new ChromeDriver();
    public String atlassianllbeanhome = "https://llbean.atlassian.net/";
    public String searchtestexecutions = "plugins/servlet/ac/com.thed.zephyr.je/general-executions-enav?project.key=CTTCM&project.id=13000#!view=list&offset=1";
    public String ZQL_SEARCH = "";
    public ReleaseExportAutomation(String username, String password, String zqlsearch){


        driver.get("https://llbean.atlassian.net/login");
        ZQL_SEARCH = zqlsearch;

        login(username, password);


        testExecutionNav();
        exportData();

    }

    public void login(String username, String password){

        driver.manage().window().maximize();
        //enter in the username to login
        driver.findElement(By.id("username")).sendKeys(username);
        //enter in the password to login
        driver.findElement(By.id("password")).sendKeys(password);
        //click the log in button
        driver.findElement(By.id("login")).click();

    }

    public void testExecutionNav(){
      //navigate to the "search test execution page"
        driver.get(atlassianllbeanhome + searchtestexecutions);

        driver.switchTo().frame(0);

        try {
            Thread.sleep(25000);
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

        WebElement advanceSearchButton  = driver.findElement(By.className("navigator-container"))
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
        exportdropdown.click();

        //find the xml export button from the drop down list
        WebElement xmlexport = driver.findElement(By.id("xmlExecutionId"));

        //click the xml export button
        xmlexport.click();


    }




}
