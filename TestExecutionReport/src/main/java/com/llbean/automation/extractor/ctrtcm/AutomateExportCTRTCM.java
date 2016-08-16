package com.llbean.automation.extractor.ctrtcm;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by klewis on 6/9/2016.
 */
public class AutomateExportCTRTCM {


    /**
     * #################################################
     * CONFIGURE
     * #################################################
     * */
    private static final String USERNAME = "klewis";
    private static final String PWD = "A13b62.91";
    private static String zql = "project = \"CT Retail Test Case Management\"";

    /**CONFIGURE FOR NEW RELEASES*/
    private static final String REL = "10.6";
    /**
     * #################################################
     * CONFIGURE
     * #################################################
     * */



    public ArrayList<String> relArray = new ArrayList<String>();
    public ArrayList<String> versions = new ArrayList<String>();
    public HashMap<String, ArrayList<String>> vMap = new HashMap<>();
    final static Logger logger = Logger.getLogger(AutomateExportCTRTCM.class);

    public static void main(String[] args){
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "\\drivers\\chromedriver.exe");
        logger.error("user.dir in main: " + System.getProperty("user.dir"));

        AutomateExportCTRTCM main = new AutomateExportCTRTCM();

        //get the versions from the jira api
        main.getJiraVersions(USERNAME, PWD);

        main.vMap.put(REL, main.relArray);

        logger.info("vmap: " + main.vMap);

        main.runReleaseAuto(USERNAME, PWD, main.createZQLSearch(main.vMap.get(REL)));
    }


    /*
    PRE: takes a username and a password as strings
    POST: returns nothing
    BRIEF: creates an instance of the java.Extrator.ReleaseExportAutomation class
     */
    public void runReleaseAuto(String username, String password, String zql){


        ReleaseExportAutomationCTRTCM releaseAuto1 = new ReleaseExportAutomationCTRTCM(username, password, zql, 1);

    }

    //creates a zql string in order to search zql given a list of versions
    public String createZQLSearch(ArrayList<String> list){
        String zqlsearch = "fixVersion in (";

        for(int i = 0; i < list.size(); i++){
            if(i == list.size() - 1) {
                zqlsearch += list.get(i) + ")";
            }
            else
                zqlsearch += list.get(i) + ", ";
        }
        return zqlsearch;
    }

    //contacts the jira rest API in order to get the current versions
    public void getJiraVersions(String username, String password) {

        String auth = new String(com.sun.jersey.core.util.Base64.encode(username + ":" + password));

        Client client = Client.create();
        WebResource webResource = client.resource("https://llbean.atlassian.net/rest/api/2/project/CTRTCM/versions");

        ClientResponse response = webResource
                .header("Authorization", "Basic " + auth)
                .accept("application/json")
                .get(ClientResponse.class);

        String output = response.getEntity(String.class);


        organizeData(output);

        logger.info("versions: " + versions);

        //creates an array based on the given search data (REL)
        //if REL = 64 then all of the versions with 64 in it will be added
        relArray = createVersionArray(versions, REL);
    }

    //organizes data so by version name, trims all other data
    public void organizeData(String output){

        //trim the string some
        output = output.substring(1, output.length()-2);
        output = output.substring(1, output.length()-2);

        //split the string by commas
        String[] results = output.split(",");
        for(int i = 0; i < results.length; i++){
            //string to compare to
            String nam = "\"name\"";

            if(!(results[i].length() < 5) && (results[i].substring(0,6).equals(nam))) {
                versions.add(results[i].substring(7));

            }
        }
    }

    //creates an arrays of specific version depending on the release
    public ArrayList<String> createVersionArray(ArrayList<String> list, String vers) {
        ArrayList<String> trimmed = new ArrayList<String>();

        for(int i = 0; i < list.size(); i++){
            if(list.get(i).contains(vers)){
                trimmed.add(list.get(i));
            }
        }

        return trimmed;
    }

    //prints all of the versions retrieved from the API
    public void printVersions(){
        for(int i = 0; i < versions.size(); i++){
            System.out.println(versions.get(i));
        }
    }

    //prints any array that is given
    public void printArray(ArrayList<String> array){
        for(int i = 0; i < array.size(); i++){
            System.out.println(array.get(i));
        }
    }


}
