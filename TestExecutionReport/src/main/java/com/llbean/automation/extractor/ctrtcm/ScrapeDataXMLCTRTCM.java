package com.llbean.automation.extractor.ctrtcm; /**
 * Created by klewis on 6/7/2016.
 */

//imports
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ScrapeDataXMLCTRTCM {

    public HashMap<String, ArrayList<String[]>> versionmap = new HashMap<>();
    public HashMap<String, ArrayList<String[]>> mapofphases = new HashMap<>();
    public ArrayList<String[]> list = new ArrayList<>();
    public ArrayList<String> versions = new ArrayList<String>();

    public String[] execstatus = {"Unexecuted", "Pass", "Fail", "WIP", "Blocked"};
    public String release = "";
    public String tempRelease = "";
    final static Logger logger = Logger.getLogger(ScrapeDataXMLCTRTCM.class);

    public void run(File file1, String date) throws FileNotFoundException {

        NodeList issueKeys = null;
        NodeList cycleNames = null;
        NodeList version = null;
        NodeList priorities = null;
        NodeList executedDate = null;
        NodeList executionStatus = null;

        logger.info("file1 path: " + file1.getPath());
        logger.error("file1 parent: " + file1.getParent());

        //fix the xml file (get rid of special characters and unwanted data)
        fixXML(file1.getPath(), file1.getParent() + "\\fix1.xml");


        //sorts through the xml file and refine the file into and array list
        try{
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            File xmlfile = new File(file1.getParent() + "\\fix1.xml");

            logger.info("file to parse: " + file1.getParent() + "\\fix1.xml");
            Document doc = null;
            try{
                logger.info("trying to parse doc...");
                doc = builder.parse(new FileInputStream(xmlfile), "UTF-8");
                logger.info("parsing doc success...");
            }
            catch (Exception ex){
                logger.info("parsing failed...");
                ex.printStackTrace();
            }

            doc.getDocumentElement().normalize();

            System.out.println("----------------------------------");

            issueKeys = doc.getElementsByTagName("issueKey");
            cycleNames = doc.getElementsByTagName("cycleName");
            priorities = doc.getElementsByTagName("priority");
            version = doc.getElementsByTagName("versions");
            executedDate = doc.getElementsByTagName("executedOn");
            executionStatus = doc.getElementsByTagName("executedStatus");



        } catch (Exception e) {
            logger.info("outer exception");
            logger.error(e.getMessage());
        }

        //add the data in the first xml file to the list
        for(int i = 0; i < issueKeys.getLength(); i++){
            //System.out.println(i + " : " + issueKeys.item(i).getTextContent());
            String[] temp = {issueKeys.item(i).getTextContent(),
                    cycleNames.item(i).getTextContent(),
                    version.item(i).getTextContent(),
                    priorities.item(i).getTextContent(),
                    executedDate.item(i).getTextContent(),
                    executionStatus.item(i).getTextContent()};
            list.add(temp);
        }
        System.out.println("List.size: " + list.size());
        //split list into versions
        versionmap = splitIntoVersions(list);
        logger.info("versionmap: " + versionmap.keySet());
        String first = versionmap.entrySet().iterator().next().getKey();
        System.out.println("Release 10.7 - Go Live size: " + versionmap.get("Release 10.7 - Go Live").size());
        System.out.println("Release 10.7 - Regression size: " + versionmap.get("Release 10.7 - Regression").size());
        release = first.substring(0, first.lastIndexOf("-"));
        release = release.trim();

        logger.info("issuekeys size: " + issueKeys.getLength());

        mapofphases = getMapOfPhases(list);
        Iterator it = mapofphases.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            ArrayList<String[]> temp = (ArrayList)pair.getValue();
            System.out.println(pair.getKey() + " | size: " + temp.size());
        }
        //printData(list);

        try {
            //setUpGUI();
            //filterPriorityclean(list);
            //filterPriorityinit(list);
            //filterPhaseclean(list);
            //filterPhaseinit(list);
            //filterDeviceclean(list);
            //filterDeviceinit(list);
            //filterPhase(list);
            //filterPriority(list);
            //filterCycle(list);

            //post the data into firebase database
            fbpostfunction(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //delete the fix files once they are used
        File f = new File(file1.getParent() + "\\fix1.xml");
        f.delete();
        logger.info("fix1.xml deleted");
        logger.info("\n---------------------------------------------------\nfinished");
    }

    /*
    #########################################################################################################
    FILTERS
    #########################################################################################################
     */

    /*
    this function will filter the given test executions by their device
    devices include: Desktop, Tablet, Mobile Web, Mobile App
    the method takes an ArrayList of String arrays that contain the test
    execution data
    there is nothing to return but that can change if necessary
    */
    public HashMap<String, HashMap<String, Integer>> filterCycle(ArrayList<String[]> versionlist){

        tempRelease = "";
        int total = 0;

        //hash maps to hold the device and the count of the device
        //the hash maps are divided into the cleanup and initial launch phases
        //and then one map for a combination of both cleanup and initial

        HashMap<String, Integer> cyclemap = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();

        //list of all possible device names
        String[] cyclelist = {"Regression", "GoLive", "SystemDown", "Draw999", "CustomerCollection"};

        tempRelease = release + " Cycle";

        logger.info("versionlist.size: " + versionlist.size());

        //loop to iterate through all of the test executions in a given list
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //string variable that holds the cycle
            String cycle = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];


            try{
                cyclemap.put(cycle, cyclemap.get(cycle) + 1);
            }
            catch(Exception j){
                cyclemap.put(cycle, 1);
            }
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////

            for(int l = 0; l < cyclelist.length; l++){
                if(StringUtils.containsIgnoreCase(cycle, cyclelist[l])){

                    for(int r = 0; r < execstatus.length; r++){

                        if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                            try{
                                HashMap<String, Integer> temp = mapwithstatus.get(cyclelist[l]);
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception e){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(cyclelist[l], temp);
                            }catch(Exception e){
                                HashMap<String, Integer> temp = new HashMap<>();
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception j){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(cyclelist[l], temp);
                            }

                        } else if(!StringUtils.containsIgnoreCase(status, execstatus[0])
                                && !StringUtils.containsIgnoreCase(status, execstatus[1])
                                && !StringUtils.containsIgnoreCase(status, execstatus[2])
                                && !StringUtils.containsIgnoreCase(status, execstatus[3])
                                && !StringUtils.containsIgnoreCase(status, execstatus[4])){
                            System.out.println(status + " | " + execstatus[r]);
                        }
                    }

                }
                else if(!StringUtils.containsIgnoreCase(cycle, cyclelist[0]) &&
                        !StringUtils.containsIgnoreCase(cycle, cyclelist[1]) &&
                        !StringUtils.containsIgnoreCase(cycle, cyclelist[2]) &&
                        !StringUtils.containsIgnoreCase(cycle, cyclelist[3]) &&
                        !StringUtils.containsIgnoreCase(cycle, cyclelist[4])){
                    System.out.println(cycle);
                    for(int r = 0; r < execstatus.length; r++){

                        if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                            try{
                                HashMap<String, Integer> temp = mapwithstatus.get(cyclelist[l]);
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception e){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(cyclelist[l], temp);
                            }catch(Exception e){
                                HashMap<String, Integer> temp = new HashMap<>();
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception j){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(cyclelist[l], temp);
                            }

                        } else if(!StringUtils.containsIgnoreCase(status, execstatus[0])
                                && !StringUtils.containsIgnoreCase(status, execstatus[1])
                                && !StringUtils.containsIgnoreCase(status, execstatus[2])
                                && !StringUtils.containsIgnoreCase(status, execstatus[3])
                                && !StringUtils.containsIgnoreCase(status, execstatus[4])){
                            System.out.println(status + " | " + execstatus[r]);
                        }
                    }
                    break;
                }
            }
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
        }

        System.out.println("total cases in filterCycle(): " + total);
        System.out.println("CYCLE\n" + mapwithstatus);
        printMap(cyclemap);
        logger.info("---------mapofphases");
        logger.info(getMapOfPhases(versionlist).keySet());
        logger.info("---------mapofphases");
        return createStatusTotals(mapwithstatus, total);

    }

    /*
    this function will filter the given test executions by their priority
    possible priorities: Critical, Major, Minor, Trivial, No Priority, Blocker
    the method takes an ArrayList of String arrays that contain the test
    execution data
    there is nothing to return but that can change if necessary
    */
    public HashMap<String, HashMap<String, Integer>> filterPriority(ArrayList<String[]> versionlist){
        tempRelease = "";
        int total = 0;
        //string array to hold the possible priorities
        String[] prioritylist = {"Critical", "Major", "Minor", "No Priority", "Trivial", "Blocker"};

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();

        tempRelease = release + " Priority";

        //for loop to iterate through the test executions of the given list
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //strings to hold the values of the priority and the cycle
            String priority = versionlist.get(i)[3];
            String status = versionlist.get(i)[5];

            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////

            for(int l = 0; l < prioritylist.length; l++){
                if(StringUtils.containsIgnoreCase(priority, prioritylist[l])){

                    for(int r = 0; r < execstatus.length; r++){

                        if(StringUtils.containsIgnoreCase(status, execstatus[r])){
                            try{
                                HashMap<String, Integer> temp = mapwithstatus.get(priority);
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception e){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(priority, temp);
                            }catch(Exception e){
                                HashMap<String, Integer> temp = new HashMap<>();
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception j){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(priority, temp);
                            }

                        }
                    }

                }
            }
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
        }

        logger.info("total cases in filterPriority(): " + total);
        logger.info(mapwithstatus);
        return createStatusTotals(mapwithstatus, total);
    }


    /*
    this function will filter the given test executions by their phase
    possible phases: Testing Weeks, PLS, Stage, Launch
    the method takes an ArrayList of String arrays that contain the test
    execution data
    there is nothing to return but that can change if necessary
     */
    public HashMap<String, Integer> filterStatus(ArrayList<String[]> versionlist){

        tempRelease = "";
        int total = 0;

        HashMap<String, Integer> mapwithstatus = new HashMap<>();

        tempRelease = release + " Phase";


        //iterate through the test executions
        for(int i = 0; i < versionlist.size(); i++){
            total++;

            String status = versionlist.get(i)[5];


            for(int r = 0; r < execstatus.length; r++){

                if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                    try{
                        int temp = mapwithstatus.get(status);
                        mapwithstatus.put(status, temp + 1);
                    }catch(Exception e){
                        mapwithstatus.put(status, 1);
                    }

                }
            }
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////

        }
        System.out.println("total cases in filterStatus(): " + total);
        System.out.println(mapwithstatus);
        mapwithstatus.put("total", total);
        return mapwithstatus;
    }

    /*
    #########################################################################################################
    #########################################################################################################
    #########################################################################################################
     */

    public HashMap<String, ArrayList<String[]>> getMapOfPhases(ArrayList<String[]> versionlist){
        HashMap<String, ArrayList<String[]>> mapofphases = new HashMap<>();
        int len = versionlist.size();
        for(int i = 0; i < len; i++){
            String cycle = versionlist.get(i)[1];
            String[] tempcycle = cycle.split("-");
            try {
                String phase = tempcycle[1];
                try{
                    ArrayList<String[]> templist = mapofphases.get(phase);
                    templist.add(versionlist.get(i));
                    mapofphases.put(phase, templist);
                }catch (Exception err){
                    ArrayList<String[]> templist = new ArrayList<>();
                    templist.add(versionlist.get(i));
                    mapofphases.put(phase, templist);
                }

            } catch(ArrayIndexOutOfBoundsException e){
                //logger.error("No release version: " + cycle);
            }
        }
        return mapofphases;
    }


    //method to automatically post without GUI if need be
    public void fbpostfunction(String date){
        //clear the firebase database first
        XMLtoSheetsCTRTCM.clearFB("https://test-execution-report-ctrtcm.firebaseio.com/");

        //try posting each filter
        Iterator it = mapofphases.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            String phase = (String)pair.getKey();
            ArrayList<String[]> phaselist = (ArrayList)pair.getValue();
            /*
        PRIORITY
         */
            try {
                XMLtoSheetsCTRTCM.run(filterPriority(phaselist), phase, "https://test-execution-report-ctrtcm.firebaseio.com/priority/", date);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        /*
        PRIORITY
         */

        /*
        CYCLE
         */
            try {
                XMLtoSheetsCTRTCM.run(filterCycle(phaselist), phase, "https://test-execution-report-ctrtcm.firebaseio.com/cycle", date);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        /*
        CYCLE
         */

        /*
        PHASE
         */
            try {
                //filterStatus(phaselist);
                XMLtoSheetsCTRTCM.runStatus(filterStatus(phaselist), phase, "https://test-execution-report-ctrtcm.firebaseio.com/status", date);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        /*
        PHASE
         */


        }
    }

    /*
    #########################################################################################################
    #########################################################################################################
    #########################################################################################################
     */


    public HashMap<String, Integer> combineHashMap(HashMap<String, Integer> map1, HashMap<String, Integer> map2){
        HashMap<String, Integer> newmap = new HashMap<>();

        Iterator it1 = map1.entrySet().iterator();
        Iterator it2 = map2.entrySet().iterator();

        while(it1.hasNext()){
            Map.Entry pair1 = (Map.Entry)it1.next();
            Map.Entry pair2 = (Map.Entry)it2.next();

            if(pair1.getKey().equals(pair2.getKey())){
                int val = (Integer)pair1.getValue() + (Integer)pair2.getValue();
                newmap.put((String) pair1.getKey(), val);
            }

        }

        return newmap;
    }

    public HashMap<String, HashMap<String,Double>> createStatusPercentages(HashMap<String, HashMap<String, Integer>> mws, int total){
        HashMap<String, HashMap<String,Double>> percmap = new HashMap<>();
        String[] execstatus = {"Unexecuted", "Pass", "Fail", "WIP", "Blocked"};
        Iterator it = mws.entrySet().iterator();
        DecimalFormat df = new DecimalFormat(".##");
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();

            HashMap<String, Integer> temp = (HashMap<String, Integer>) pair.getValue();

            Iterator it2 = temp.entrySet().iterator();
            double totalex = 0;
            int[] exarray = {0,0,0,0,0};
            int count = 0;
            while(it2.hasNext()){
                Map.Entry pair2 = (Map.Entry)it2.next();
                //System.out.println(pair2.getKey() + " | " + pair2.getValue());

                totalex += (Integer)pair2.getValue();
                exarray[count] = (Integer)pair2.getValue();
                count++;
            }

            HashMap<String, Double> tempdub = new HashMap<>();
            for(int i = 0; i < exarray.length; i++){
                double percent = (exarray[i] / totalex) * 100;
                tempdub.put(execstatus[i], (double)Math.round(percent * 100) / 100);
            }
            tempdub.put("total", totalex);
            percmap.put((String) pair.getKey(), tempdub);

        }


        return percmap;
    }

    public HashMap<String, HashMap<String,Integer>> createStatusTotals(HashMap<String, HashMap<String, Integer>> mws, int total){
        HashMap<String, HashMap<String,Integer>> percmap = new HashMap<>();
        String[] execstatus = {"Pass", "Unexecuted", "Blocked", "Fail", "WIP"};
        Iterator it = mws.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();

            HashMap<String, Integer> temp = (HashMap<String, Integer>) pair.getValue();

            Iterator it2 = temp.entrySet().iterator();
            int totalex = 0;
            int[] exarray = {0,0,0,0,0};
            int count = 0;
            while(it2.hasNext()){
                Map.Entry pair2 = (Map.Entry)it2.next();
                //System.out.println(pair2.getKey() + " | " + pair2.getValue());

                totalex += (Integer)pair2.getValue();
                exarray[count] = (Integer)pair2.getValue();
                count++;
            }

            HashMap<String, Integer> tempdub = new HashMap<>();
            for(int i = 0; i < exarray.length; i++){

                tempdub.put(execstatus[i], exarray[i]);
            }
            tempdub.put("total", totalex);
            percmap.put((String) pair.getKey(), tempdub);

        }


        return percmap;
    }

    public HashMap<String, Double> createPercentages(HashMap<String, Integer> map, int total){

        Iterator it = map.entrySet().iterator();

        HashMap<String, Double> percentages = new HashMap<>();

        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();

            double percent = (double)((Integer)pair.getValue() * 100) / total;
            percent = (double)Math.round(percent * 100) / 100;
            percentages.put((String) pair.getKey(), percent);
        }

        return percentages;

    }

    public HashMap<String, ArrayList<String[]>> splitIntoVersions(ArrayList<String[]> listsplit){
        HashMap<String, ArrayList<String[]>> versionmap = new HashMap<>();
        for(int i = 0; i < listsplit.size(); i++){
            String version = listsplit.get(i)[2];
            try{
                ArrayList<String[]> temp = versionmap.get(version);
                temp.add(listsplit.get(i));
                versionmap.put(version, temp);
            }
            catch (NullPointerException e){
                ArrayList<String[]> temp = new ArrayList<>();
                versionmap.put(version, temp);
            }

        }

        return versionmap;
    }

    //creates and returns a list from an xml file
    public ArrayList<String> readXmlAsString(File xmlfile){
        ArrayList<String> stringbuilds = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(xmlfile))){
            String line;
            while((line = reader.readLine()) != null){
                //System.out.println(line);
                stringbuilds.add(line);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }


        return stringbuilds;
    }

    //given a path of a test execution xml file it removes unwanted data and special characters
    public void fixXML(String path, String toPath) throws FileNotFoundException {
        logger.info("Fixing XML......");
        File xmlfile = new File(path);
        ArrayList<String> xml = readXmlAsString(xmlfile);
        for(int i = 0; i < xml.size(); i++){
            if(xml.get(i).contains("<testSummary>")) {
                xml.remove(i);
            }
            if(xml.get(i).contains("<result>")) {
                xml.remove(i);
            }
            if(xml.get(i).contains("<data>")) {
                xml.remove(i);
            }
            if(xml.get(i).contains("<step>")) {
                xml.remove(i);
            }
            if(xml.get(i).contains("<stepComment>")) {
                xml.remove(i);
            }
            if(xml.get(i).contains("<stepId>")) {
                xml.remove(i);
            }
        }
        try {
            writeFile(xml, toPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("XML fixed at path: " + toPath);

    }

    public void writeFile(ArrayList<String> list, String path) throws IOException {
        File file = new File(path);
        if(!file.exists()){
            file.createNewFile();
        }

        FileWriter write = new FileWriter(file.getAbsoluteFile());
        BufferedWriter writer = new BufferedWriter(write);

        for(int i = 0; i < list.size(); i++){
            writer.write(list.get(i));
            writer.newLine();
        }

        writer.close();
    }


    //deprecated methods -- could still be useful
    public void printData(ArrayList<String[]> listToPrint) {
        System.out.println("size: " + listToPrint.size());
        for(int i = 0; i < listToPrint.size(); i++){
            String[] temp = listToPrint.get(i);
            for(int j = 0; j < temp.length; j++){
                System.out.print(temp[j] + " | ");
            }
            System.out.println();
        }
    }

    public int printMapDouble(HashMap<String, Double> map){
        Iterator it = map.entrySet().iterator();
        int count = 0;
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            count += (double)pair.getValue();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }

        return count;
    }

    public int printMap(HashMap<String, Integer> map){
        Iterator it = map.entrySet().iterator();
        int count = 0;
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            count += (Integer)pair.getValue();
            System.out.println(pair.getKey() + " = " + pair.getValue());
        }

        return count;
    }

    public void readURLxmlAsString(String url) throws Exception {
        ArrayList<String[]> stringbuilds = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document doc = factory.newDocumentBuilder().parse(new URL(url).openStream());

        doc.getDocumentElement().normalize();

        NodeList issueKeys = doc.getElementsByTagName("issueKey");
        NodeList cycleNames = doc.getElementsByTagName("cycleName");
        NodeList priorities = doc.getElementsByTagName("priority");
        NodeList version = doc.getElementsByTagName("versions");
        NodeList executedDate = doc.getElementsByTagName("executedOn");
        NodeList executionStatus = doc.getElementsByTagName("executedStatus");

        for(int i = 0; i < issueKeys.getLength(); i++){
            String[] temp = {issueKeys.item(i).getTextContent(),
                    cycleNames.item(i).getTextContent(),
                    version.item(i).getTextContent(),
                    priorities.item(i).getTextContent(),
                    executedDate.item(i).getTextContent(),
                    executionStatus.item(i).getTextContent()};
            stringbuilds.add(temp);
        }

        for(int i = 0; i < stringbuilds.size(); i++){
            System.out.println(stringbuilds.get(i)[1]);
        }



    }

    public void getVersions(HashMap<String, ArrayList<String[]>> versionm){
        HashMap<String, ArrayList<String[]>> temp = versionm;
        Iterator it = temp.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            versions.add((String)pair.getKey());

        }
    }

    public void printNestedMap(HashMap<String, HashMap<String, Integer>> map){
        Iterator it = map.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " | " + pair.getValue());
        }
        System.out.println();
    }

}
