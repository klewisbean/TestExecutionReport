package com.llbean.automation.extractor; /**
 * Created by klewis on 6/7/2016.
 */

//imports
import javax.swing.*;
import javax.swing.border.Border;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultDocument;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public class ScrapeDataXML {

    public HashMap<String, ArrayList<String[]>> versionmap = new HashMap<>();
    public HashMap<String, Integer> phasemap = new HashMap<>();
    public HashMap<String, Integer> prioritymap = new HashMap<>();
    public HashMap<String, Integer> devicemap = new HashMap<>();
    public HashMap<String, HashMap<String, Integer>> phases = new HashMap<>();
    public ArrayList<String[]> list = new ArrayList<>();
    public HashMap<String, Integer> phasepriority = new HashMap<>();
    public ArrayList<String> versions = new ArrayList<String>();

    public HashMap<String, HashMap<String, Integer>> phasestatus = new HashMap<>();
    public HashMap<String, HashMap<String, Integer>> devicestatus = new HashMap<>();
    public HashMap<String, HashMap<String, Integer>> prioritystatus = new HashMap<>();

    public String[] execstatus = {"Unexecuted", "Pass", "Fail", "WIP", "Blocked"};
    public String release = "";
    public String tempRelease = "";
    //public String date = "";
    public static int TOTAL = 0;

    final static Logger logger = Logger.getLogger(ScrapeDataXML.class);

    public void run(File file1, File file2, String date) throws FileNotFoundException {

        NodeList issueKeys = null;
        NodeList cycleNames = null;
        NodeList version = null;
        NodeList priorities = null;
        NodeList executedDate = null;
        NodeList executionStatus = null;
        NodeList issueKeys1 = null;
        NodeList cycleNames1 = null;
        NodeList version1 = null;
        NodeList priorities1 = null;
        NodeList executedDate1 = null;
        NodeList executionStatus1 = null;

        logger.info("file1 path: " + file1.getPath());
        logger.error("file1 parent: " + file1.getParent());
        fixXML(file1.getPath(), file1.getParent() + "\\fix1.xml");
        fixXML(file2.getPath(), file2.getParent() + "\\fix2.xml");

        File directory = new File(System.getProperty("user.dir"));

        File[] filesin = directory.listFiles();

        //System.out.println("nameToFind: " + nameToFind + "\nfilename: " + filename);


        for(File fil : filesin){
            System.out.println(fil.getName());
        }
        //sorts through the xml file and refine the file into and array list
        try{

            File xmlfile = new File(file1.getParent() + "\\fix1.xml");
            File xmlfile2 = new File(file2.getParent() + "\\fix2.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            logger.info("file to parse: " + xmlfile.getName());
            logger.info("file to parse: " + xmlfile2.getName());
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = null;
            Document doc1 = null;
            try{
                logger.info("trying to parse...");
                doc = builder.parse(new FileInputStream(xmlfile));
                doc1 = builder.parse(new FileInputStream(xmlfile2));
            }
            catch (Exception ex){
                logger.info("parsing failed...");
                ex.printStackTrace();
            }

            doc.getDocumentElement().normalize();
            doc1.getDocumentElement().normalize();


            System.out.println("----------------------------------");

            issueKeys = doc.getElementsByTagName("issueKey");
            cycleNames = doc.getElementsByTagName("cycleName");
            priorities = doc.getElementsByTagName("priority");
            version = doc.getElementsByTagName("versions");
            executedDate = doc.getElementsByTagName("executedOn");
            executionStatus = doc.getElementsByTagName("executedStatus");

            issueKeys1 = doc1.getElementsByTagName("issueKey");
            cycleNames1 = doc1.getElementsByTagName("cycleName");
            version1 = doc1.getElementsByTagName("versions");
            priorities1 = doc1.getElementsByTagName("priority");
            executedDate1 = doc1.getElementsByTagName("executedOn");
            executionStatus1 = doc1.getElementsByTagName("executedStatus");



        } catch (Exception e) {
            logger.info("outer exception");
            e.printStackTrace();
        }

        for(int i = 0; i < issueKeys.getLength(); i++){
            String[] temp = {issueKeys.item(i).getTextContent(),
                    cycleNames.item(i).getTextContent(),
                    version.item(i).getTextContent(),
                    priorities.item(i).getTextContent(),
                    executedDate.item(i).getTextContent(),
                    executionStatus.item(i).getTextContent()};
            list.add(temp);
        }
        for(int i = 0; i < issueKeys1.getLength(); i++){
            String[] temp = {issueKeys1.item(i).getTextContent(),
                    cycleNames1.item(i).getTextContent(),
                    version1.item(i).getTextContent(),
                    priorities1.item(i).getTextContent(),
                    executedDate1.item(i).getTextContent(),
                    executionStatus1.item(i).getTextContent()};
            list.add(temp);
        }

        //printData(list);
        System.out.println("list size: " + list.size());

        versionmap = splitIntoVersions(list);

        String first = versionmap.entrySet().iterator().next().getKey();
        release = first.substring(0,3);


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
            //filterDevice(list);
            //if the GUI is not needed just uncomment this method call
            fbpostfunction(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        File f = new File(file1.getParent() + "\\fix1.xml");
        f.delete();
        f = new File(file2.getParent() + "\\fix2.xml");
        f.delete();
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
    public HashMap<String, HashMap<String, Integer>> filterDevice(ArrayList<String[]> versionlist){

        tempRelease = "";
        int total = 0;
        int totalinit = 1;
        int totalcleanup = 1;

        //hash maps to hold the device and the count of the device
        //the hash maps are divided into the cleanup and initial launch phases
        //and then one map for a combination of both cleanup and initial
        HashMap<String, Integer> cleandevicemap = new HashMap<>();
        HashMap<String, Integer> initdevicemap = new HashMap<>();
        HashMap<String, Integer> devicemap = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> initmapwithstatus = new HashMap<>();

        //list of all possible device names
        String[] devicelist = {"Tablet", "Desktop", "Mobile App", "Mobile Web", "mweb", "app"};

        tempRelease = release + " Device";
        int count = 0;

        //loop to iterate through all of the test executions in a given list
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //string variable that holds the cycle
            String cycle = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];

            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////

            for(int l = 0; l < devicelist.length; l++){
                if(StringUtils.containsIgnoreCase(cycle, devicelist[l])){

                    for(int r = 0; r < execstatus.length; r++){

                        if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                            try{
                                HashMap<String, Integer> temp = mapwithstatus.get(devicelist[l]);
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception e){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(devicelist[l], temp);
                            }catch(Exception e){
                                HashMap<String, Integer> temp = new HashMap<>();
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception j){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(devicelist[l], temp);
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
                else if(!StringUtils.containsIgnoreCase(cycle, devicelist[0]) &&
                        !StringUtils.containsIgnoreCase(cycle, devicelist[1]) &&
                        !StringUtils.containsIgnoreCase(cycle, devicelist[2]) &&
                        !StringUtils.containsIgnoreCase(cycle, devicelist[3]) &&
                        !StringUtils.containsIgnoreCase(cycle, devicelist[4]) &&
                        !StringUtils.containsIgnoreCase(cycle, devicelist[5])){
                    System.out.println(cycle);
                    for(int r = 0; r < execstatus.length; r++){

                        if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                            try{
                                HashMap<String, Integer> temp = mapwithstatus.get(devicelist[l]);
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception e){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(devicelist[l], temp);
                            }catch(Exception e){
                                HashMap<String, Integer> temp = new HashMap<>();
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception j){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(devicelist[l], temp);
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

        System.out.println("total cases in filterDevice(): " + total);
        System.out.println("DEVICE\n" + mapwithstatus);
        TOTAL = total;
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

        //hashmaps to hold the data according by the priority and the corresponding count value
        HashMap<String, Integer> prioritymap = new HashMap<>();
        HashMap<String, Integer> cleanprioritymap = new HashMap<>();
        HashMap<String, Integer> initprioritymap = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> initmapwithstatus = new HashMap<>();

        tempRelease = release + " Priority";

        //for loop to iterate through the test executions of the given list
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //strings to hold the values of the priority and the cycle
            String priority = versionlist.get(i)[3];
            String cycle = versionlist.get(i)[1];
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

        System.out.println("total cases in filterPriority(): " + total);
        System.out.println(mapwithstatus);
        TOTAL = total;
        return createStatusTotals(mapwithstatus, total);
    }


    /*
    this function will filter the given test executions by their phase
    possible phases: Testing Weeks, PLS, Stage, Launch
    the method takes an ArrayList of String arrays that contain the test
    execution data
    there is nothing to return but that can change if necessary
     */
    public HashMap<String, HashMap<String, Integer>> filterPhase(ArrayList<String[]> versionlist){

        tempRelease = "";
        int total = 0;
        //string array to hold the possible phase types
        String[] phasetype = {"PLS", "TestingWeeks", "Stage", "Launch"};

        //hashmaps to keep track of the phase counts depending on if
        //the cycle is clean up or initial
        HashMap<String, Integer> cleanupphasemap = new HashMap<>();
        HashMap<String, Integer> initialphasemap = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> initmapwithstatus = new HashMap<>();

        tempRelease = release + " Phase";

        for(int i = 0; i < versionlist.size(); i++){
            String phase = versionlist.get(i)[1];
            if(StringUtils.containsIgnoreCase(phase, "PROD")){
                versionlist.get(i)[1] = phase.replace("PROD", "Launch");
            }

            if(StringUtils.containsIgnoreCase(phase, "testing weeks")){
                versionlist.get(i)[1] = phase.replace(" ", "");
            }
        }


        //iterate through the test executions
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //string to hold the cycle name
            String phase = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];


            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////

            for(int l = 0; l < phasetype.length; l++){
                if(StringUtils.containsIgnoreCase(phase, phasetype[l])){
                    for(int r = 0; r < execstatus.length; r++){

                        if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                            try{
                                HashMap<String, Integer> temp = mapwithstatus.get(phasetype[l]);
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception e){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(phasetype[l], temp);
                            }catch(Exception e){
                                HashMap<String, Integer> temp = new HashMap<>();
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception j){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put(phasetype[l], temp);
                            }

                        }
                    }
                    break;
                }
                else if(!StringUtils.containsIgnoreCase(phase, phasetype[0]) &&
                        !StringUtils.containsIgnoreCase(phase, phasetype[1]) &&
                        !StringUtils.containsIgnoreCase(phase, phasetype[2]) &&
                        !StringUtils.containsIgnoreCase(phase, phasetype[3])){
                    System.out.println(phase);
                    for(int r = 0; r < execstatus.length; r++){

                        if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                            try{
                                HashMap<String, Integer> temp = mapwithstatus.get("TestingWeeks");
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception e){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put("TestingWeeks", temp);
                            }catch(Exception e){
                                HashMap<String, Integer> temp = new HashMap<>();
                                try{
                                    temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                }
                                catch(Exception j){
                                    temp.put(execstatus[r], 1);
                                }
                                mapwithstatus.put("TestingWeeks", temp);
                            }

                        }
                    }

                    break;
                }
            }
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////

        }
        System.out.println("total cases in filterPhase(): " + total);
        System.out.println(mapwithstatus);
        TOTAL = total;
        return createStatusTotals(mapwithstatus, total);
    }

    /*
    #########################################################################################################
    #########################################################################################################
    #########################################################################################################
     */

    public HashMap<String, HashMap<String, Integer>> filterPriorityclean(ArrayList<String[]> versionlist){
        tempRelease = "";
        int total = 0;
        int totalclean = 0;
        int totalinit = 0;
        int count2 = 0;
        int[] countex = {0,0,0,0,0};
        //string array to hold the possible priorities
        String[] prioritylist = {"Critical", "Major", "Minor", "No Priority", "Trivial", "Blocker"};

        //hashmaps to hold the data according by the priority and the corresponding count value
        HashMap<String, Integer> prioritymap = new HashMap<>();
        HashMap<String, Integer> cleanprioritymap = new HashMap<>();
        HashMap<String, Integer> initprioritymap = new HashMap<>();



        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> initmapwithstatus = new HashMap<>();

        tempRelease = release + " Priority";

        //for loop to iterate through the test executions of the given list
        for(int i = 0; i < versionlist.size(); i++){

            //strings to hold the values of the priority and the cycle
            String priority = versionlist.get(i)[3];
            String cycle = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];


            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////


            //check if the cycle is cleanup
            if(StringUtils.containsIgnoreCase(cycle, "Cleanup") || StringUtils.containsIgnoreCase(cycle, "Clean-up")){

                totalclean++;

                //loop to iterate through the different priorities
                for(int j = 0; j < prioritylist.length; j++){
                    //check if the test execution has a certain priority
                    if(StringUtils.containsIgnoreCase(priority, prioritylist[j])){
                        for(int r = 0; r < execstatus.length; r++){
                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = cleanmapwithstatus.get(priority);
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put(priority, temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put(priority, temp);
                                }

                            }
                        }
                        //add the priority to the map or increment the count if the
                        //priority already exists in the map
                        try{
                            total++;
                            cleanprioritymap.put(priority, cleanprioritymap.get(priority) + 1);
                        }
                        catch(NullPointerException e){
                            total++;
                            cleanprioritymap.put(priority, 1);
                        }
                    } else if(!StringUtils.containsIgnoreCase(priority, prioritylist[0])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[1])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[2])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[3])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[4])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[5])){
                        System.out.println(prioritylist[j] + " | " + priority);
                    }
                }
            }
        }
        System.out.println("total clean: " + totalclean);
        System.out.println(cleanmapwithstatus);
        return createStatusTotals(cleanmapwithstatus, totalclean);
    }

    public HashMap<String, HashMap<String, Integer>> filterPriorityinit(ArrayList<String[]> versionlist){
        tempRelease = "";
        int total = 0;
        int totalclean = 0;
        int totalinit = 0;
        int count2 = 0;
        int[] countex = {0,0,0,0,0};
        //string array to hold the possible priorities
        String[] prioritylist = {"Critical", "Major", "Minor", "No Priority", "Trivial", "Blocker"};

        //hashmaps to hold the data according by the priority and the corresponding count value
        HashMap<String, Integer> prioritymap = new HashMap<>();
        HashMap<String, Integer> cleanprioritymap = new HashMap<>();
        HashMap<String, Integer> initprioritymap = new HashMap<>();



        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> initmapwithstatus = new HashMap<>();

        tempRelease = release + " Priority";

        //for loop to iterate through the test executions of the given list
        for(int i = 0; i < versionlist.size(); i++){

            //strings to hold the values of the priority and the cycle
            String priority = versionlist.get(i)[3];
            String cycle = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];

            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////




            //check if the cycle is cleanup
            if(StringUtils.containsIgnoreCase(cycle, "Cleanup") || StringUtils.containsIgnoreCase(cycle, "Clean-up")) {

                totalclean++;
            }
            //when the cycle is an initial launch
            else{
                totalinit++;
                //iterate through the possible priorities
                for(int j = 0; j < prioritylist.length; j++){

                    //check if the priority matches the test execution
                    if(StringUtils.containsIgnoreCase(priority, prioritylist[j])){

                        for(int r = 0; r < execstatus.length; r++){

                            //System.out.println(status + " | " + execstatus[r] + " = " + (status.equalsIgnoreCase(execstatus[r])));
                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = initmapwithstatus.get(priority);
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    initmapwithstatus.put(priority, temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    initmapwithstatus.put(priority, temp);
                                }

                            }
                        }

                        //add the priority to the map or increment the count if the
                        //priority already exists in the map
                        try{
                            total++;
                            prioritymap.put(priority, prioritymap.get(priority) + 1);
                            initprioritymap.put(priority, initprioritymap.get(priority) + 1);
                        }
                        catch(NullPointerException e){
                            total++;
                            prioritymap.put(priority, 1);
                            initprioritymap.put(priority, 1);
                        }
                    } else if(!StringUtils.containsIgnoreCase(priority, prioritylist[0])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[1])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[2])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[3])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[4])
                            && !StringUtils.containsIgnoreCase(priority, prioritylist[5])){
                        System.out.println(prioritylist[j] + " | " + priority);
                    }
                }
            }

        }

        System.out.println("total init: " + totalinit);
        System.out.println(initmapwithstatus);
        return createStatusTotals(initmapwithstatus, totalinit);
    }


    /*
    #########################################################################################################
    #########################################################################################################
    #########################################################################################################
     */

    public HashMap<String, HashMap<String, Integer>> filterPhaseclean(ArrayList<String[]> versionlist){

        tempRelease = "";
        int total = 0;
        int totalclean = 0;
        int totalinit = 0;
        //string array to hold the possible phase types
        String[] phasetype = {"PLS", "TestingWeeks", "Stage", "Launch"};

        //hashmaps to keep track of the phase counts depending on if
        //the cycle is clean up or initial
        HashMap<String, Integer> cleanupphasemap = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();

        tempRelease = release + " Phase";

        //iterate through the test executions
        for(int i = 0; i < versionlist.size(); i++){

            //string to hold the cycle name
            String phase = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];


            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////



            //if the test execution is cleanup
            if(StringUtils.containsIgnoreCase(phase, "Cleanup") || StringUtils.containsIgnoreCase(phase, "Clean-up")){
                totalclean++;
                //iterate through the possible phases
                for(int j = 0; j < phasetype.length; j++){
                    //check if the test execution is a certain phase
                    if(StringUtils.containsIgnoreCase(phase, phasetype[j])){

                        for(int r = 0; r < execstatus.length; r++){

                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = cleanmapwithstatus.get(phasetype[j]);
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put(phasetype[j], temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put(phasetype[j], temp);
                                }
                                break;
                            }
                        }
                    }
                    //when the phase cannot be determined
                    //add print statement to determine why
                    else if(!StringUtils.containsIgnoreCase(phase, phasetype[0]) && !StringUtils.containsIgnoreCase(phase, phasetype[1])
                            && !StringUtils.containsIgnoreCase(phase, phasetype[2])
                            && !StringUtils.containsIgnoreCase(phase, phasetype[3])){
                        for(int r = 0; r < execstatus.length; r++){

                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = cleanmapwithstatus.get(phasetype[j]);
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put(phasetype[j], temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put(phasetype[j], temp);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
        System.out.println("total clean: " + totalclean);
        System.out.println(cleanmapwithstatus);
        return createStatusTotals(cleanmapwithstatus, totalclean);
    }

    public HashMap<String, HashMap<String, Integer>> filterPhaseinit(ArrayList<String[]> versionlist){

        tempRelease = "";
        int total = 0;
        int totalclean = 0;
        int totalinit = 0;
        //string array to hold the possible phase types
        String[] phasetype = {"PLS", "TestingWeeks", "Stage", "Launch"};

        //hashmaps to keep track of the phase counts depending on if
        //the cycle is clean up or initial
        HashMap<String, Integer> cleanupphasemap = new HashMap<>();
        HashMap<String, Integer> initialphasemap = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> initmapwithstatus = new HashMap<>();

        tempRelease = release + " Phase";

        //iterate through the test executions
        for(int i = 0; i < versionlist.size(); i++){

            //string to hold the cycle name
            String phase = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];


            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////


            //if the test execution is cleanup
            if(StringUtils.containsIgnoreCase(phase, "Cleanup") || StringUtils.containsIgnoreCase(phase, "Clean-up")) {
                totalclean++;
            }
            //phase is initial
            else{
                totalinit++;
                //iterate through the possible phase types
                for(int j = 0; j < phasetype.length; j++){
                    //check if the phase is correct
                    if(StringUtils.containsIgnoreCase(phase, phasetype[j])){

                        for(int r = 0; r < execstatus.length; r++){

                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = initmapwithstatus.get(phasetype[j]);
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    initmapwithstatus.put(phasetype[j], temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    initmapwithstatus.put(phasetype[j], temp);

                                }

                            }
                        }
                        break;
                    }
                    //if the phase cannot be determined
                    //add print statement to troubleshoot
                    else if(!StringUtils.containsIgnoreCase(phase, phasetype[0]) && !StringUtils.containsIgnoreCase(phase, phasetype[1])
                            && !StringUtils.containsIgnoreCase(phase, phasetype[2])
                            && !StringUtils.containsIgnoreCase(phase, phasetype[3])){
                        //System.out.println("Initial Other: " + phase);
                        if(StringUtils.containsIgnoreCase(phase, "prod")){
                            for(int r = 0; r < execstatus.length; r++){
                                if(StringUtils.containsIgnoreCase(status, execstatus[r])){
                                    try{
                                        HashMap<String, Integer> temp = initmapwithstatus.get("Launch");
                                        try{
                                            temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                        }
                                        catch(Exception e){
                                            temp.put(execstatus[r], 1);
                                        }
                                        initmapwithstatus.put("Launch", temp);
                                    }catch(Exception e){
                                        HashMap<String, Integer> temp = new HashMap<>();
                                        try{
                                            temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                        }
                                        catch(Exception exc){
                                            temp.put(execstatus[r], 1);
                                        }
                                        initmapwithstatus.put("Launch", temp);
                                    }
                                }
                            }
                            break;
                        }else{
                            for(int r = 0; r < execstatus.length; r++){
                                if(StringUtils.containsIgnoreCase(status, execstatus[r])){
                                    try{
                                        HashMap<String, Integer> temp = initmapwithstatus.get("TestingWeeks");
                                        try{
                                            temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                        }
                                        catch(Exception e){
                                            temp.put(execstatus[r], 1);
                                        }
                                        initmapwithstatus.put("TestingWeeks", temp);
                                    }catch(Exception e){
                                        HashMap<String, Integer> temp = new HashMap<>();
                                        try{
                                            temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                        }
                                        catch(Exception exc){
                                            temp.put(execstatus[r], 1);
                                        }
                                        initmapwithstatus.put("TestingWeeks", temp);
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }

        }
        System.out.println("total init: " + totalinit);
        System.out.println(initmapwithstatus);
        return createStatusTotals(initmapwithstatus, totalinit);
    }


    /*
    #########################################################################################################
    #########################################################################################################
    #########################################################################################################
     */

    public HashMap<String, HashMap<String, Integer>> filterDeviceclean(ArrayList<String[]> versionlist){

        tempRelease = "";
        int total = 0;
        int totalinit = 0;
        int totalcleanup = 0;

        //hash maps to hold the device and the count of the device
        //the hash maps are divided into the cleanup and initial launch phases
        //and then one map for a combination of both cleanup and initial
        HashMap<String, Integer> cleandevicemap = new HashMap<>();
        HashMap<String, Integer> initdevicemap = new HashMap<>();
        HashMap<String, Integer> devicemap = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> initmapwithstatus = new HashMap<>();

        //list of all possible device names
        String[] devicelist = {"Tablet", "Desktop", "Mobile App", "Mobile Web", "mweb", "app"};

        tempRelease = release + " Device";

        //loop to iterate through all of the test executions in a given list
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //string variable that holds the cycle
            String cycle = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];

            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////


            //check if the cycle is cleanup
            if(StringUtils.containsIgnoreCase(cycle, "Cleanup") || StringUtils.containsIgnoreCase(cycle, "Clean-up")){
                totalcleanup++;
                //loop to iterate through all of the possible device names
                for(int j = 0; j < devicelist.length; j++){

                    //check if the test execution matches one of the devices
                    //then add it to the map under it's name and increment the count
                    if(StringUtils.containsIgnoreCase(cycle, devicelist[j])){

                        for(int r = 0; r < execstatus.length; r++){

                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = cleanmapwithstatus.get(devicelist[j]);
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put(devicelist[j], temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put(devicelist[j], temp);
                                }

                            }
                        }
                        break;
                    }

                    //if the cycle does not contain any of the device names
                    else if(!StringUtils.containsIgnoreCase(cycle, devicelist[0])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[1])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[2])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[3])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[4])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[5])){
                        for(int r = 0; r < execstatus.length; r++){

                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = cleanmapwithstatus.get("Desktop");
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put("Desktop", temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    cleanmapwithstatus.put("Desktop", temp);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        System.out.println("total cleanup: " + totalcleanup);
        System.out.println(cleanmapwithstatus);
        return createStatusTotals(cleanmapwithstatus, totalcleanup);

    }

    public HashMap<String, HashMap<String, Integer>> filterDeviceinit(ArrayList<String[]> versionlist){

        tempRelease = "";
        int total = 0;
        int totalinit = 1;
        int totalcleanup = 1;

        //hash maps to hold the device and the count of the device
        //the hash maps are divided into the cleanup and initial launch phases
        //and then one map for a combination of both cleanup and initial
        HashMap<String, Integer> cleandevicemap = new HashMap<>();
        HashMap<String, Integer> initdevicemap = new HashMap<>();
        HashMap<String, Integer> devicemap = new HashMap<>();

        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> cleanmapwithstatus = new HashMap<>();
        HashMap<String, HashMap<String, Integer>> initmapwithstatus = new HashMap<>();

        //list of all possible device names
        String[] devicelist = {"Tablet", "Desktop", "Mobile App", "Mobile Web", "mweb", "app"};

        tempRelease = release + " Device";

        //loop to iterate through all of the test executions in a given list
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //string variable that holds the cycle
            String cycle = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];

            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////


            //check if the cycle is cleanup
            if(StringUtils.containsIgnoreCase(cycle, "Cleanup") || StringUtils.containsIgnoreCase(cycle, "Clean-up")){
                totalcleanup++;
            }
            //when the launch is not cleanup but it is initial
            else{
                totalinit++;
                //loop to iterate through the device names
                for(int j = 0; j < devicelist.length; j++){
                    //check if the cycle contains the device name
                    if(StringUtils.containsIgnoreCase(cycle, devicelist[j])){

                        for(int r = 0; r < execstatus.length; r++){

                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = initmapwithstatus.get(devicelist[j]);
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    initmapwithstatus.put(devicelist[j], temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    initmapwithstatus.put(devicelist[j], temp);
                                }
                            }
                        }
                        break;
                    }
                    //if the cycle does not contain any of the device names
                    else if(!StringUtils.containsIgnoreCase(cycle, devicelist[0])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[1])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[2])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[3])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[4])
                            && !StringUtils.containsIgnoreCase(cycle, devicelist[5])){
                        //System.out.println("Cycle:" + cycle);
                        for(int r = 0; r < execstatus.length; r++){

                            if(StringUtils.containsIgnoreCase(status, execstatus[r])){

                                try{
                                    HashMap<String, Integer> temp = initmapwithstatus.get("Desktop");
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception e){
                                        temp.put(execstatus[r], 1);
                                    }
                                    initmapwithstatus.put("Desktop", temp);
                                }catch(Exception e){
                                    HashMap<String, Integer> temp = new HashMap<>();
                                    try{
                                        temp.put(execstatus[r], temp.get(execstatus[r]) + 1);
                                    }
                                    catch(Exception exc){
                                        temp.put(execstatus[r], 1);
                                    }
                                    initmapwithstatus.put("Desktop", temp);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }

        System.out.println("total init: " + totalinit);
        System.out.println(initmapwithstatus);
        return createStatusTotals(initmapwithstatus, totalinit);

    }

    /*
    #########################################################################################################
    #########################################################################################################
    #########################################################################################################
     */

    //method to automatically post without GUI if need be
    public void fbpostfunction(String date){
        //clear the firebase database first
        XMLtoSheets.clearFB("https://test-execution-report.firebaseio.com/");

        //try posting each filter
        /*
        PHASE
         */
        try {
            XMLtoSheets.run(filterPhase(list), tempRelease, "https://test-execution-report.firebaseio.com/phase", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            XMLtoSheets.run(filterPhaseclean(list), tempRelease + " Clean", "https://test-execution-report.firebaseio.com/phase/clean", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            XMLtoSheets.run(filterPhaseinit(list), tempRelease + " Initial", "https://test-execution-report.firebaseio.com/phase/initial", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        /*
        PHASE
         */


        /*
        PRIORITY
         */
        try {
            XMLtoSheets.run(filterPriority(list), tempRelease, "https://test-execution-report.firebaseio.com/priority", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            XMLtoSheets.run(filterPriorityclean(list), tempRelease + " Clean", "https://test-execution-report.firebaseio.com/priority/clean", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            XMLtoSheets.run(filterPriorityinit(list), tempRelease + " Initial", "https://test-execution-report.firebaseio.com/priority/initial", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        /*
        PRIORITY
         */

        /*
        DEVICE
         */
        try {
            XMLtoSheets.run(filterDevice(list), tempRelease, "https://test-execution-report.firebaseio.com/device", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            XMLtoSheets.run(filterDeviceclean(list), tempRelease + " Clean", "https://test-execution-report.firebaseio.com/device/clean", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        try {
            XMLtoSheets.run(filterDeviceinit(list), tempRelease + " Initial", "https://test-execution-report.firebaseio.com/device/initial", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        /*
        DEVICE
         */
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
        String[] execstatus = {"Unexecuted", "Pass", "Fail", "WIP", "Blocked"};
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

    public void fixXML(String path, String toPath) throws FileNotFoundException {
        logger.info("Fixing XML......");
        File xmlfile = new File(path);
        ArrayList<String> xml = readXmlAsString(xmlfile);
        ArrayList<String> newxml = new ArrayList<>();
        String amp = "& ";
        for(int i = 0; i < xml.size(); i++){
            if(xml.get(i).contains("<testSummary>")) {
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
