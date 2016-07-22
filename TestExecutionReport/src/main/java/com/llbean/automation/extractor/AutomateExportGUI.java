package com.llbean.automation.extractor;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by klewis on 6/9/2016.
 */
public class AutomateExportGUI {


    public ArrayList<String> R60 = new ArrayList<String>();
    public ArrayList<String> R61 = new ArrayList<String>();
    public ArrayList<String> R62 = new ArrayList<String>();
    public ArrayList<String> R63 = new ArrayList<String>();
    public ArrayList<String> R64 = new ArrayList<String>();
    public ArrayList<String> R65 = new ArrayList<String>();
    public ArrayList<String> other = new ArrayList<String>();
    public ArrayList<String> versions = new ArrayList<String>();

    public HashMap<String, ArrayList<String>> vMap = new HashMap<>();

    public String[] releases = {"60", "61", "62", "63", "64", "65"};

    final static Logger logger = Logger.getLogger(AutomateExportGUI.class);

    public static void main(String[] args){
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir") + "\\TestExecutionReport\\drivers\\chromedriver.exe");


        AutomateExportGUI main = new AutomateExportGUI();


        main.getJiraVersions("klewis", "A13b62.91");

        main.vMap.put("R60", main.R60);
        main.vMap.put("R61", main.R61);
        main.vMap.put("R62", main.R62);
        main.vMap.put("R63", main.R63);
        main.vMap.put("R64", main.R64);
        main.vMap.put("R65", main.R65);
        main.vMap.put("Other", main.other);


        //default set to export Release 64
        main.runReleaseAuto("klewis", "A13b62.91", "R64", main.vMap.get("R64"));
    }


    /*
    PRE: takes a username and a password as strings
    POST: returns nothing
    BRIEF: creates an instance of the java.Extrator.ReleaseExportAutomation class
     */
    public void runReleaseAuto(String username, String password, String release, ArrayList<String> releaseList){
        ArrayList<String> half1 = new ArrayList<>();
        ArrayList<String> half2 = new ArrayList<>();

        for(int i = 0; i < releaseList.size(); i++){
            if(i < releaseList.size()/2-1){
                half1.add(releaseList.get(i));
            }
            else{
                half2.add(releaseList.get(i));
            }
        }
        //System.out.println("Half1 size: " + half1.size() + " Half2 size: " + half2.size());
        String zqlsearch1 = createZQLSearch(half1);
        String zqlsearch2 = createZQLSearch(half2);

        ReleaseExportAutomation releaseAuto1 = new ReleaseExportAutomation(username, password, zqlsearch1, 1);
        ReleaseExportAutomation releaseAuto2 = new ReleaseExportAutomation(username, password, zqlsearch2, 2);
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
        WebResource webResource = client.resource("https://llbean.atlassian.net/rest/api/2/project/CTTCM/versions");

        ClientResponse response = webResource
                .header("Authorization", "Basic " + auth)
                .accept("application/json")
                .get(ClientResponse.class);

        String output = response.getEntity(String.class);


        //System.out.println(output);

        organizeData(output);
        //printVersions();

        R60 = createVersionArray(versions, "60");
        R61 = createVersionArray(versions, "61");
        R62 = createVersionArray(versions, "62");
        R63 = createVersionArray(versions, "63");
        R64 = createVersionArray(versions, "64");
        R65 = createVersionArray(versions, "65");
        other = createOtherVersionArray(versions);

        //printArray(R63);

    }


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

    //creates the array that consists of versions without a specified release
    public ArrayList<String> createOtherVersionArray(ArrayList<String> list) {
        ArrayList<String> trimmed = new ArrayList<String>();

        Boolean check = false;
        int count;
        for(int i = 0; i < list.size(); i++){
            count = 0;
            for(int j = 0; j < releases.length; j++){
                if(!list.get(i).contains(releases[j])){
                    count++;
                }
                if(count == releases.length)
                {
                    trimmed.add(list.get(i));
                }
            }
        }

        return trimmed;
    }

}
