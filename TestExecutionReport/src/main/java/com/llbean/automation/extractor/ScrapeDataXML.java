package com.llbean.automation.extractor; /**
 * Created by klewis on 6/7/2016.
 */

//imports
import javax.swing.*;
import javax.swing.border.Border;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

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



        fixXML(file1.getPath(), file1.getPath());
        fixXML(file2.getPath(), file2.getPath());


        //sorts through the xml file and refine the file into and array list
        try{

            File xmlfile = new File(file1.getPath());
            File xmlfile2 = new File(file2.getPath());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlfile);
            Document doc1 = builder.parse(xmlfile2);

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

        versionmap = splitIntoVersions(list);

        String first = versionmap.entrySet().iterator().next().getKey();
        release = first.substring(0,3);


        try {
            //setUpGUI();

            //if the GUI is not needed just uncomment this method call
            fbpostfunction(date);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //this function will setup the GUI for the data the user wants in the api
    public void setUpGUI(String date){
        //creates the frame and it's structure
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle(release + " Data to Sheet");
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setSize(300, 100);

        //borders
        Border blackline = BorderFactory.createLineBorder(Color.black);
        Border redline = BorderFactory.createLineBorder(Color.red);

        //larger panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JPanel toptop = new JPanel();
        toptop.setLayout(new BoxLayout(toptop, BoxLayout.Y_AXIS));
        JPanel botbot = new JPanel();

        //filter panel
        //////////////////////////////////////////////////////////////////
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBorder(blackline);
        JLabel filterLabel = new JLabel("Select Filter");

        JComboBox<String> filtercombox = new JComboBox();
        filtercombox.addItem("Phase");
        filtercombox.addItem("Priority");
        filtercombox.addItem("Device");

        filterPanel.add(filterLabel);
        filterPanel.add(filtercombox);

        //data panel
        //////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////
        JPanel dataPanel = new JPanel();
        dataPanel.setBorder(blackline);
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.Y_AXIS));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        JLabel dataLabel = new JLabel("Select Data");

        JRadioButton releaseRadio = new JRadioButton("Full Release");
        JRadioButton versionRadio = new JRadioButton("Version");

        ButtonGroup group = new ButtonGroup();
        group.add(releaseRadio);
        group.add(versionRadio);

        JComboBox datacombox = new JComboBox();
        Iterator it = versionmap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            datacombox.addItem(pair.getKey());
        }
        datacombox.setEnabled(false);
        JPanel labelp = new JPanel();
        labelp.add(dataLabel);

        topPanel.add(releaseRadio);
        topPanel.add(versionRadio);
        bottomPanel.add(datacombox);

        dataPanel.add(labelp);
        dataPanel.add(topPanel);
        dataPanel.add(bottomPanel);

        //button panel
        //////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////
        JPanel buttonPanel = new JPanel();

        JButton action = new JButton("Add Data to Sheets API");
        JButton clearsheet = new JButton("Clear the Sheet");
        buttonPanel.add(action);
        //buttonPanel.add(clearsheet);

        //////////////////////////////////////////////////////////////////


        //add panels to larger panels
        //toptop.add(filterPanel);
        //toptop.add(dataPanel);

        botbot.add(buttonPanel);

        //mainPanel.add(toptop);
        mainPanel.add(botbot);


        //finish frame
        frame.add(mainPanel);
        frame.setVisible(true);



        //action listeners
        //if the release radio button is selected, disable the version
        //combo box
        /*releaseRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(releaseRadio.isSelected()){
                    datacombox.setEnabled(false);
                }
            }
        });*/

        //if the version radio button is selected, enable the version
        //combo box
        /*versionRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(versionRadio.isSelected()){
                    datacombox.setEnabled(true);
                }
            }
        });*/

        //add the clearsheet button action
        /*clearsheet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    java.Extrator.XMLtoSheets.clearFB("https://test-execution-report.firebaseio.com/");
                } catch (Exception e1){
                    e1.printStackTrace();
                }
            }
        });*/

        //will run java.Extrator.XMLtoSheets.java based on what data the user wants
        //to post to the api
        action.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                XMLtoSheets.clearFB("https://test-execution-report.firebaseio.com/");
                try {
                    XMLtoSheets.run(filterPhase(list), tempRelease, "https://test-execution-report.firebaseio.com/phase", date);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }


                try {
                    XMLtoSheets.run(filterPriority(list), tempRelease, "https://test-execution-report.firebaseio.com/priority", date);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }


                try {
                    XMLtoSheets.run(filterDevice(list), tempRelease, "https://test-execution-report.firebaseio.com/device", date);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });




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

            for(int l = 0; l < devicelist.length; l++){
                if(StringUtils.containsIgnoreCase(cycle, devicelist[l])){

                    for(int r = 0; r < execstatus.length; r++){

                        //System.out.println(status + " | " + execstatus[r] + " = " + (status.equalsIgnoreCase(execstatus[r])));
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
                                mapwithstatus.put(devicelist[l], new HashMap<>());
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
            }
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
                        try{
                            cleandevicemap.put(devicelist[j], cleandevicemap.get(devicelist[j]) + 1);
                            devicemap.put(devicelist[j], devicemap.get(devicelist[j]) + 1);
                        }
                        catch(NullPointerException e){
                            cleandevicemap.put(devicelist[j], 1);
                            devicemap.put(devicelist[j], 1);
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
                        //System.out.println("Cleanup: " + cycle + " | " + versionlist.get(i)[2]);
                        try{
                            cleandevicemap.put("Desktop", cleandevicemap.get("Desktop") + 1);
                            devicemap.put("Desktop", devicemap.get("Desktop") + 1);
                        }
                        catch(NullPointerException e){
                            devicemap.put("Desktop", 1);
                            cleandevicemap.put("Desktop", 1);
                        }
                        break;
                    }
                }
            }
            //when the launch is not cleanup but it is initial
            else{
                totalinit++;
                //loop to iterate through the device names
                for(int j = 0; j < devicelist.length; j++){
                    //check if the cycle contains the device name
                    if(StringUtils.containsIgnoreCase(cycle, devicelist[j])){
                        //add the device to the map or increment the count if the device already
                        //exists in the map
                        try{
                            initdevicemap.put(devicelist[j], initdevicemap.get(devicelist[j]) + 1);
                            devicemap.put(devicelist[j], devicemap.get(devicelist[j]) + 1);
                        }
                        catch(NullPointerException e){
                            initdevicemap.put(devicelist[j], 1);
                            devicemap.put(devicelist[j], 1);
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
                        //System.out.println("Initial: " + cycle + " | " + versionlist.get(i)[2]);
                        try{
                            initdevicemap.put("Desktop", initdevicemap.get("Desktop") + 1);
                            devicemap.put("Desktop", devicemap.get("Desktop") + 1);
                        }
                        catch(NullPointerException e){
                            initdevicemap.put("Desktop", 1);
                            devicemap.put("Desktop", 1);
                        }
                        break;
                    }
                }
            }

        }

        //variables and code to combine the different names for Mobile Web
        //do the same for Mobile App
        int mwebinit = 0;
        int mwebclean = 0;
        int bigminit = 0;
        int bigmclean = 0;
        try{mwebinit = initdevicemap.get("mweb");}catch(Exception e){initdevicemap.put("mweb", 0);}
        try{mwebclean = cleandevicemap.get("mweb");}catch(Exception e){cleandevicemap.put("mweb", 0);}
        try{bigminit = initdevicemap.get("Mobile Web");}catch(Exception e){initdevicemap.put("Mobile Web", 0);}
        try{bigmclean = cleandevicemap.get("Mobile Web");}catch(Exception e){cleandevicemap.put("Mobile Web", 0);}

        int mobilewebtotal = mwebinit + mwebclean + bigminit + bigmclean;

        cleandevicemap.remove("mweb");
        cleandevicemap.put("Mobile Web", mwebclean + bigmclean);

        initdevicemap.remove("mweb");
        initdevicemap.put("Mobile Web", mwebinit + bigminit);

        devicemap.remove("mweb");
        devicemap.put("Mobile Web", mobilewebtotal);

        int appinit = 0;
        int appclean = 0;
        int mappinit = 0;
        int mappclean = 0;
        try{appinit = initdevicemap.get("app");}catch(Exception e){initdevicemap.put("app", 0);}
        try{appclean = cleandevicemap.get("app");}catch(Exception e){cleandevicemap.put("app", 0);}
        try{mappinit = initdevicemap.get("Mobile App");}catch(Exception e){initdevicemap.put("Mobile App", 0);}
        try{mappclean = cleandevicemap.get("Mobile App");}catch(Exception e){cleandevicemap.put("Mobile App", 0);}

        int mobileapptotal = appinit + appclean + mappinit + mappclean;

        cleandevicemap.remove("app");
        cleandevicemap.put("Mobile App", appclean + mappclean);

        initdevicemap.remove("app");
        initdevicemap.put("Mobile App", appinit + mappinit);

        devicemap.remove("app");
        devicemap.put("Mobile App", mobileapptotal);
        ////////////////////////////////////////////////////////////////////////////////



        HashMap<String, Double> deviceInitialPercentageMap = createPercentages(initdevicemap, totalinit);

        HashMap<String, Double> deviceCleanupPercentageMap = createPercentages(cleandevicemap, totalcleanup);

        HashMap<String, Double> devicePercentageMap = createPercentages(combineHashMap(cleandevicemap, initdevicemap), total);

        //printMap(combineHashMap(cleandevicemap, initdevicemap));

        try{
            HashMap<String, Integer> combweb = combineHashMap(mapwithstatus.get("mweb"), mapwithstatus.get("Mobile Web"));

            mapwithstatus.remove("mweb");

            mapwithstatus.put("Mobile Web", combweb);
        }catch(Exception e){
            //System.out.println("could not combine");
        }


        try {
            HashMap<String, Integer> combapp = combineHashMap(mapwithstatus.get("app"), mapwithstatus.get("Mobile App"));


            mapwithstatus.remove("app");

            mapwithstatus.put("Mobile App", combapp);

        }catch(Exception e){
            //System.out.println("could not combine");
        }
        /*Iterator it = mapwithstatus.entrySet().iterator();

        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " | " + pair.getValue());
        }*/


        devicestatus = mapwithstatus;
        TOTAL = total;

        HashMap<String, Integer> tempmap = combineHashMap(cleandevicemap, initdevicemap);

        //System.out.println("device total: " + (totalinit + totalcleanup));

        tempmap.put("total", TOTAL);
        //printMap(tempmap);
        //System.out.println("mapwithstatus size: " + mapwithstatus.size());
        return createStatusTotals(mapwithstatus, total + mapwithstatus.size());
        //display the data and count the total
        /*System.out.println("Cleanup:");
        int countclean = printMap(cleandevicemap);
        System.out.println("\nInitial:");
        int countinit = printMap(initdevicemap);
        System.out.println("TOTAL: " + (countinit + countclean));

        System.out.println("\n---------------------------------");
        printMapDouble(devicePercentageMap);*/

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
        int count2 = 0;
        int[] countex = {0,0,0,0,0};
        //string array to hold the possible priorities
        String[] prioritylist = {"Critical", "Major", "Minor", "No Priority", "Trivial", "Blocker"};

        //hashmaps to hold the data according by the priority and the corresponding count value
        HashMap<String, Integer> prioritymap = new HashMap<>();
        HashMap<String, Integer> cleanprioritymap = new HashMap<>();
        HashMap<String, Integer> initprioritymap = new HashMap<>();



        HashMap<String, HashMap<String, Integer>> mapwithstatus = new HashMap<>();

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

            for(int l = 0; l < prioritylist.length; l++){
                if(StringUtils.containsIgnoreCase(priority, prioritylist[l])){

                    for(int r = 0; r < execstatus.length; r++){

                        //System.out.println(status + " | " + execstatus[r] + " = " + (status.equalsIgnoreCase(execstatus[r])));
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
                                mapwithstatus.put(priority, new HashMap<>());
                            }

                        }
                    }

                }
            }
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////




            //check if the cycle is cleanup
            if(StringUtils.containsIgnoreCase(cycle, "Cleanup") || StringUtils.containsIgnoreCase(cycle, "Clean-up")){

                //loop to iterate through the different priorities
                for(int j = 0; j < prioritylist.length; j++){
                    //check if the test execution has a certain priority
                    if(StringUtils.containsIgnoreCase(priority, prioritylist[j])){
                        //add the priority to the map or increment the count if the
                        //priority already exists in the map
                        try{
                            total++;
                            prioritymap.put(priority, prioritymap.get(priority) + 1);
                            cleanprioritymap.put(priority, cleanprioritymap.get(priority) + 1);
                        }
                        catch(NullPointerException e){
                            total++;
                            prioritymap.put(priority, 1);
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

                    for(int k = 0; k < execstatus.length; k++){

                        if(status.equalsIgnoreCase(execstatus[k])){
                            countex[k] += 1;
                        }
                    }

                }

            }
            //when the cycle is an initial launch
            else{
                //iterate through the possible priorities
                for(int j = 0; j < prioritylist.length; j++){

                    //check if the priority matches the test execution
                    if(StringUtils.containsIgnoreCase(priority, prioritylist[j])){
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

                    for(int k = 0; k < execstatus.length; k++){

                        if(status.equalsIgnoreCase(execstatus[k])){
                            countex[k] += 1;
                        }
                    }
                }
            }

        }


        HashMap<String,Double> priorityPercentageMap = createPercentages(combineHashMap(cleanprioritymap, initprioritymap), total);


        //printMap(combineHashMap(cleanprioritymap, initprioritymap));

        Iterator it = mapwithstatus.entrySet().iterator();

        /*while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " | " + pair.getValue());
        }*/
        HashMap<String, Integer> tempmap = combineHashMap(cleanprioritymap, initprioritymap);
        prioritystatus = mapwithstatus;
        System.out.println("priority total: " + total);
        TOTAL = total;
        tempmap.put("total", TOTAL);
        //System.out.println("mapwithstatus size: " + mapwithstatus.size());
        return createStatusTotals(mapwithstatus, total + mapwithstatus.size());


        //display the data and calculate the total
        /*System.out.println("Cleanup: ");
        int countclean = printMap(cleanprioritymap);
        System.out.println("\nInitial: ");
        int countinit = printMap(initprioritymap);
        System.out.println("TOTAL: " + (countinit + countclean));*/
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

        tempRelease = release + " Phase";

        //iterate through the test executions
        for(int i = 0; i < versionlist.size(); i++){

            //string to hold the cycle name
            String phase = versionlist.get(i)[1];
            String status = versionlist.get(i)[5];

            //System.out.println("phase: " + phase + " | status: " + status);

            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////

            for(int l = 0; l < phasetype.length; l++){
                if(StringUtils.containsIgnoreCase(phase, phasetype[l])){

                    for(int r = 0; r < execstatus.length; r++){

                        //System.out.println(status + " | " + execstatus[r] + " = " + (status.equalsIgnoreCase(execstatus[r])));
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
                                mapwithstatus.put(phasetype[l], new HashMap<>());
                            }

                        }
                    }

                }
            }
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////



            //if the test execution is cleanup
            if(StringUtils.containsIgnoreCase(phase, "Cleanup") || StringUtils.containsIgnoreCase(phase, "Clean-up")){
                //iterate through the possible phases
                for(int j = 0; j < phasetype.length; j++){
                    //check if the test execution is a certain phase
                    if(StringUtils.containsIgnoreCase(phase, phasetype[j])){
                        //add the phase to the map or increment the phase count if the phase
                        //already exists in the map
                        try{
                            total++;
                            cleanupphasemap.put(phasetype[j], cleanupphasemap.get(phasetype[j]) + 1);
                            phasemap.put(phasetype[j], phasemap.get(phasetype[j]) + 1);
                        }
                        catch(NullPointerException e){
                            total++;
                            cleanupphasemap.put(phasetype[j], 1);
                            phasemap.put(phasetype[j], 1);
                        }
                        break;
                    }
                    //when the phase cannot be determined
                    //add print statement to determine why
                    else if(!StringUtils.containsIgnoreCase(phase, phasetype[0]) && !StringUtils.containsIgnoreCase(phase, phasetype[1])
                            && !StringUtils.containsIgnoreCase(phase, phasetype[2])
                            && !StringUtils.containsIgnoreCase(phase, phasetype[3])){
                        System.out.println("Cleanup Other: " + phase);
                        try{
                            total++;
                            cleanupphasemap.put("Testing Weeks", cleanupphasemap.get("Testing Weeks") + 1);
                            phasemap.put("Testing Weeks", phasemap.get("Testing Weeks") + 1);
                        }
                        catch(NullPointerException e){
                            total++;
                            cleanupphasemap.put("Testing Weeks", 1);
                            phasemap.put("Testing Weeks", 1);
                        }
                        break;
                    }
                }
            }
            //phase is initial
            else{
                //iterate through the possible phase types
                for(int j = 0; j < phasetype.length; j++){
                    //check if the phase is correct
                    if(StringUtils.containsIgnoreCase(phase, phasetype[j])){
                        //add the phase to the map or increment the phase count if
                        //the phase already exists in the map
                        try{
                            total++;
                            initialphasemap.put(phasetype[j], initialphasemap.get(phasetype[j]) + 1);
                            phasemap.put(phasetype[j], phasemap.get(phasetype[j]) + 1);
                        }
                        catch(NullPointerException e){
                            total++;
                            initialphasemap.put(phasetype[j], 1);
                            phasemap.put(phasetype[j], 1);
                        }
                        break;
                    }
                    //if the phase cannot be determined
                    //add print statement to troubleshoot
                    else if(!StringUtils.containsIgnoreCase(phase, phasetype[0]) && !StringUtils.containsIgnoreCase(phase, phasetype[1])
                            && !StringUtils.containsIgnoreCase(phase, phasetype[2])
                            && !StringUtils.containsIgnoreCase(phase, phasetype[3])){
                        System.out.println("Initial Other: " + phase);
                        try{
                            total++;
                            initialphasemap.put("Testing Weeks", initialphasemap.get("Testing Weeks") + 1);
                            phasemap.put("Testing Weeks", phasemap.get("Testing Weeks") + 1);
                        }
                        catch(NullPointerException e){
                            total++;
                            initialphasemap.put("Testing Weeks", 1);
                            phasemap.put("Testing Weeks", 1);
                        }
                        break;
                    }
                }
            }

        }
        phasestatus = mapwithstatus;


        HashMap<String, Integer> temp = combineHashMap(cleanupphasemap, initialphasemap);

        System.out.println("phase total: " + total );
        //System.out.println("mapwithstatus size: " + mapwithstatus.size());
        TOTAL = total;
        temp.put("total", TOTAL);


        //printNestedMap(createStatusTotals(mapwithstatus, total));
        return createStatusTotals(mapwithstatus, total + mapwithstatus.size());

        //display the data and count the total
        /*System.out.println("Cleanup:");
        int countclean = printMap(cleanupphasemap);
        System.out.println("\nInitial:");
        int countinit = printMap(initialphasemap);
        System.out.println("TOTAL: " + (countinit + countclean));*/
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
        try {
            XMLtoSheets.run(filterPhase(list), tempRelease, "https://test-execution-report.firebaseio.com/phase", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        try {
            XMLtoSheets.run(filterPriority(list), tempRelease, "https://test-execution-report.firebaseio.com/priority", date);
        } catch (IOException e1) {
            e1.printStackTrace();
        }


        try {
            XMLtoSheets.run(filterDevice(list), tempRelease, "https://test-execution-report.firebaseio.com/device", date);
        } catch (IOException e1) {
            e1.printStackTrace();
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
        File xmlfile = new File(path);
        ArrayList<String> xml = readXmlAsString(xmlfile);
        ArrayList<String> newxml = new ArrayList<>();
        String amp = "& ";
        for(int i = 0; i < xml.size(); i++){
            if(xml.get(i).contains("<testSummary>")) {
                xml.remove(i);
            }

        }



        /*PrintWriter writer = new PrintWriter("ZFJ-Executions-06-13-2016-new.xml");
        for(int i = 0; i < xml.size(); i++){
            writer.println(xml.get(i));
        }*/
        try {
            writeFile(xml, toPath);
        } catch (IOException e) {
            e.printStackTrace();
        }


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
