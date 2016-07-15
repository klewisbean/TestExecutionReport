package Extrator; /**
 * Created by klewis on 6/9/2016.
 */
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;



public class VersionExportAutomation {

    public WebDriver driver = new ChromeDriver();
    public final String atlassianllbeanhome = "https://llbean.atlassian.net/";
    public final String searchtestexecutions = "plugins/servlet/ac/com.thed.zephyr.je/general-executions-enav?project.key=CTTCM&project.id=13000#!view=list&offset=1";

    public VersionExportAutomation(String username, String password, String version){
        System.setProperty("webdriver.chrome.driver", "C:\\JiraReportGadget-2\\TestExecutionReport\\chromedriver.exe");

        driver.get("https://llbean.atlassian.net/login");
        login(username, password);
        testExecutionNav(version);
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

    public void testExecutionNav(String version){
        //navigate to the "search test execution page"
        driver.get(atlassianllbeanhome + searchtestexecutions);

        WebElement executions = driver.findElement(By.id("com.thed.zephyr.je__viewissue-executions-projectCentric"));
        System.out.println(executions);

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

        advanceInput.sendKeys("fixVersion in (" + version + ")");

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
