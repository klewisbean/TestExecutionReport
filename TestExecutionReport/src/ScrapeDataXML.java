/**
 * Created by klewis on 6/7/2016.
 */

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;



public class ScrapeDataXML {

    public HashMap<String, Integer> phasemap = new HashMap<>();
    public HashMap<String, Integer> prioritymap = new HashMap<>();
    public HashMap<String, Integer> devicemap = new HashMap<>();
    public HashMap<String, HashMap<String, Integer>> phases = new HashMap<>();
    public ArrayList<String[]> list = new ArrayList<>();
    public HashMap<String, Integer> phasepriority = new HashMap<>();

    public void run(File file1, File file2) throws FileNotFoundException {
        ScrapeDataXML main = new ScrapeDataXML();
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


        /*try {
            main.readURLxmlAsString("http://383161b2.ngrok.io/ZFJ-Executions-06-17-2016.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        main.fixXML("C:\\Users\\klewis\\Downloads\\ZFJ-Executions-06-17-2016.xml", "C:\\JiraReportGadget-2\\ZFJ-Executions-06-17-2016.xml");
        //main.fixXML("https://383161b2.ngrok.io/ZFJ-Executions-06-17-2016.xml", "C:\\JiraReportGadget-2\\ZFJ-Executions-06-17-2016.xml");
        main.fixXML("C:\\Users\\klewis\\Downloads\\ZFJ-Executions-06-17-2016 (1).xml", "C:\\JiraReportGadget-2\\ZFJ-Executions-06-17-2016 (1).xml");



        try{

            //URL xmlfile = new URL("https://383161b2.ngrok.io/ZFJ-Executions-06-17-2016.xml");
            File xmlfile = new File("C:\\JiraReportGadget-2\\ZFJ-Executions-06-17-2016.xml");
            File xmlfile2 = new File("C:\\JiraReportGadget-2\\ZFJ-Executions-06-17-2016 (1).xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();


            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlfile);

            Document doc1 = builder.parse(xmlfile2);

            doc.getDocumentElement().normalize();
            doc1.getDocumentElement().normalize();

            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
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
            main.list.add(temp);
        }
        for(int i = 0; i < issueKeys1.getLength(); i++){
            String[] temp = {issueKeys1.item(i).getTextContent(),
                    cycleNames1.item(i).getTextContent(),
                    version1.item(i).getTextContent(),
                    priorities1.item(i).getTextContent(),
                    executedDate1.item(i).getTextContent(),
                    executionStatus1.item(i).getTextContent()};
            main.list.add(temp);
        }

        //main.printData(main.list);











        /*
        testing
         */
        HashMap<String, ArrayList<String[]>> versionmap = main.splitIntoVersions(main.list);



        System.out.println("RELEASE 63 PHASE FILTER\n---------------------------------");
        main.filterPhase(main.list);
        System.out.println("---------------------------------");
        System.out.println("RELEASE 63 DEVICE FILTER\n---------------------------------");
        main.filterDevice(main.list);
        System.out.println("---------------------------------");
        System.out.println("RELEASE 63 PRIORITY FILTER\n---------------------------------");
        main.filterPriority(main.list);
        System.out.println("---------------------------------\n\n");


        //test versions
        /*System.out.println("BASELINE-63 PHASE FILTER\n---------------------------------");
        main.filterPhase(versionmap.get("R63-Baseline"));
        System.out.println("---------------------------------");
        System.out.println("BASELINE-63 DEVICE FILTER\n---------------------------------");
        main.filterDevice(versionmap.get("R63-Baseline"));
        System.out.println("---------------------------------");
        System.out.println("BASELINE-63 PRIORITY FILTER\n---------------------------------");
        main.filterPriority(versionmap.get("R63-Baseline"));
        System.out.println("---------------------------------");*/

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
    public void filterDevice(ArrayList<String[]> versionlist){
        int total = 0;

        //hash maps to hold the device and the count of the device
        //the hash maps are divided into the cleanup and initial launch phases
        //and then one map for a combination of both cleanup and initial
        HashMap<String, Integer> cleandevicemap = new HashMap<>();
        HashMap<String, Integer> initdevicemap = new HashMap<>();
        HashMap<String, Integer> devicemap = new HashMap<>();

        //list of all possible device names
        String[] devicelist = {"Tablet", "Desktop", "Mobile App", "Mobile Web", "mweb", "app"};

        //loop to iterate through all of the test executions in a given list
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //string variable that holds the cycle
            String cycle = versionlist.get(i)[1];

            //check if the cycle is cleanup
            if(StringUtils.containsIgnoreCase(cycle, "Cleanup") || StringUtils.containsIgnoreCase(cycle, "Clean-up")){
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



        HashMap<String, Double> devicePercentageMap = createPercentages(combineHashMap(cleandevicemap, initdevicemap), total);


        //display the data and count the total
        System.out.println("Cleanup:");
        int countclean = printMap(cleandevicemap);
        System.out.println("\nInitial:");
        int countinit = printMap(initdevicemap);
        System.out.println("TOTAL: " + (countinit + countclean));


    }

    /*
    this function will filter the given test executions by their priority
    possible priorities: Critical, Major, Minor, Trivial, No Priority, Blocker
    the method takes an ArrayList of String arrays that contain the test
    execution data
    there is nothing to return but that can change if necessary
    */
    public void filterPriority(ArrayList<String[]> versionlist){
        int total = 0;

        //string array to hold the possible priorities
        String[] prioritylist = {"Critical", "Major", "Minor", "No Priority", "Trivial", "Blocker"};

        //hashmaps to hold the data according by the priority and the corresponding count value
        HashMap<String, Integer> prioritymap = new HashMap<>();
        HashMap<String, Integer> cleanprioritymap = new HashMap<>();
        HashMap<String, Integer> initprioritymap = new HashMap<>();

        //for loop to iterate through the test executions of the given list
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //strings to hold the values of the priority and the cycle
            String priority = versionlist.get(i)[3];
            String cycle = versionlist.get(i)[1];

            //check if the cycle is cleanup
            if(StringUtils.containsIgnoreCase(cycle, "Cleanup") || StringUtils.containsIgnoreCase(cycle, "Clean-up")){

                //loop to iterate through the different priorities
                for(int j = 0; j < prioritylist.length; j++){
                    //check if the test execution has a certain priority
                    if(StringUtils.containsIgnoreCase(priority, prioritylist[j])){
                        //add the priority to the map or increment the count if the
                        //priority already exists in the map
                        try{
                            prioritymap.put(priority, prioritymap.get(priority) + 1);
                            cleanprioritymap.put(priority, cleanprioritymap.get(priority) + 1);
                        }
                        catch(NullPointerException e){
                            prioritymap.put(priority, 1);
                            cleanprioritymap.put(priority, 1);
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
                            prioritymap.put(priority, prioritymap.get(priority) + 1);
                            initprioritymap.put(priority, initprioritymap.get(priority) + 1);
                        }
                        catch(NullPointerException e){
                            prioritymap.put(priority, 1);
                            initprioritymap.put(priority, 1);
                        }
                    }
                }
            }

        }


        HashMap<String,Double> priorityPercentageMap = createPercentages(combineHashMap(cleanprioritymap, initprioritymap), total);

        //display the data and calculate the total
        System.out.println("Cleanup: ");
        int countclean = printMap(cleanprioritymap);
        System.out.println("\nInitial: ");
        int countinit = printMap(initprioritymap);
        System.out.println("TOTAL: " + (countinit + countclean));
    }


    /*
    this function will filter the given test executions by their phase
    possible phases: Testing Weeks, PLS, Stage, Launch
    the method takes an ArrayList of String arrays that contain the test
    execution data
    there is nothing to return but that can change if necessary
     */
    public void filterPhase(ArrayList<String[]> versionlist){

        int total = 0;
        //string array to hold the possible phase types
        String[] phasetype = {"PLS", "Testing Weeks", "Stage", "Launch"};

        //hashmaps to keep track of the phase counts depending on if
        //the cycle is clean up or initial
        HashMap<String, Integer> cleanupphasemap = new HashMap<>();
        HashMap<String, Integer> initialphasemap = new HashMap<>();

        //iterate through the test executions
        for(int i = 0; i < versionlist.size(); i++){
            total++;
            //string to hold the cycle name
            String phase = versionlist.get(i)[1];

            //if the test execution is cleanup
            if(StringUtils.containsIgnoreCase(phase, "Cleanup") || StringUtils.containsIgnoreCase(phase, "Clean-up")){
                //iterate through the possible phases
                for(int j = 0; j < phasetype.length; j++){
                    //check if the test execution is a certain phase
                    if(StringUtils.containsIgnoreCase(phase, phasetype[j])){
                        //add the phase to the map or increment the phase count if the phase
                        //already exists in the map
                        try{
                            cleanupphasemap.put(phasetype[j], cleanupphasemap.get(phasetype[j]) + 1);
                            phasemap.put(phasetype[j], phasemap.get(phasetype[j]) + 1);
                        }
                        catch(NullPointerException e){
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
                        //System.out.println("Cleanup Other: " + phase);
                        try{
                            cleanupphasemap.put("Testing Weeks", cleanupphasemap.get("Testing Weeks") + 1);
                            phasemap.put("Testing Weeks", phasemap.get("Testing Weeks") + 1);
                        }
                        catch(NullPointerException e){
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
                            initialphasemap.put(phasetype[j], initialphasemap.get(phasetype[j]) + 1);
                            phasemap.put(phasetype[j], phasemap.get(phasetype[j]) + 1);
                        }
                        catch(NullPointerException e){
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
                        //System.out.println("Initial Other: " + phase);
                        try{
                            initialphasemap.put("Testing Weeks", initialphasemap.get("Testing Weeks") + 1);
                            phasemap.put("Testing Weeks", phasemap.get("Testing Weeks") + 1);
                        }
                        catch(NullPointerException e){
                            initialphasemap.put("Testing Weeks", 1);
                            phasemap.put("Testing Weeks", 1);
                        }
                        break;
                    }
                }
            }

        }

        HashMap<String, Double> phaseMapPercents = createPercentages(combineHashMap(cleanupphasemap, initialphasemap), total);

        //display the data and count the total
        System.out.println("Cleanup:");
        int countclean = printMap(cleanupphasemap);
        System.out.println("\nInitial:");
        int countinit = printMap(initialphasemap);
        System.out.println("TOTAL: " + (countinit + countclean));







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
            it.remove();
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
            it.remove();
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


}
