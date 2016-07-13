import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Base64;
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



        main.setUpGUI();
    }

    //method to set up the GUI
    public void setUpGUI(){
        //frame settings
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Select Fix Version");
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setSize(600, 400);

        //border to separate contents
        Border blackline = BorderFactory.createLineBorder(Color.black);
        Border redline = BorderFactory.createLineBorder(Color.red);

        //outer panels
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        JPanel leftPanel = new JPanel();
        JPanel rightPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(blackline);
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(blackline);

        //labels
        JLabel comboxLabel = new JLabel("Select release from dropdown: ");
        JLabel atlassianLoginLabel = new JLabel("Atlassian login credentials");
        JLabel usernameLabel = new JLabel("Username: ");
        JLabel passwordLabel = new JLabel("Password: ");

        //textfields
        JTextField usernameTF = new JTextField(10);
        JTextField passwordTF = new JPasswordField(10);

        //smaller panels for the labels and textfields
        JPanel usrPanel = new JPanel();
        usrPanel.add(usernameLabel);
        usrPanel.add(usernameTF);
        JPanel pwdPanel = new JPanel();
        pwdPanel.add(passwordLabel);
        pwdPanel.add(passwordTF);

        //list to display the version for a specific release
        JList<String> versionList = new JList<>();
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        versionList.setLayoutOrientation(JList.VERTICAL);



        JScrollPane listScroller = new JScrollPane(versionList);
        listScroller.setPreferredSize(new Dimension(300,90));


        //drop down for selection of the version
        JComboBox versionDropdown = new JComboBox();
        versionDropdown.addItem("");
        versionDropdown.addItem("R60");
        versionDropdown.addItem("R61");
        versionDropdown.addItem("R62");
        versionDropdown.addItem("R63");
        versionDropdown.addItem("R64");
        versionDropdown.addItem("R65");
        versionDropdown.addItem("Other");

        //smaller panels for the version drop down and buttons
        JPanel versionSelectionPanel = new JPanel();
        JPanel buttonPanel = new JPanel();

        //the automate button will automate one specific version when clicked
        JButton automateButton = new JButton("Automate");
        automateButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
        automateButton.addActionListener(new ActionListener() {
            @Override
            /*
            when the button is clicked the user selection is retrieved
            as well as the username and password entered
            then the VersionExportAutomation class is run by
            creating and instance of it
            */
            public void actionPerformed(ActionEvent e) {
                String username = usernameTF.getText();
                String password = passwordTF.getText();
                String selected = versionList.getSelectedValue();
                //if the user does not enter a username or password
                //a red line will show around the textfields
                if(username.equals("") || password.equals("")){
                    usernameTF.setBorder(redline);
                    passwordTF.setBorder(redline);
                }
                //if the user does not select a release then
                //a red line will show around the dropdown
                else if(versionDropdown.getSelectedItem().equals("")){
                    versionDropdown.setBorder(redline);
                }
                else {
                    frame.dispose();
                    runReleaseAuto(username, password, (String) versionDropdown.getSelectedItem(), vMap.get(versionDropdown.getSelectedItem()));
                }


            }
        });

        //updates the version list with the chosen release
        versionDropdown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selected = (String)versionDropdown.getSelectedItem();
                if(selected.equals("")){
                    DefaultListModel<String> listModel = new DefaultListModel<String>();
                    versionList.setModel(listModel);
                }
                else {
                    ArrayList<String> temp = vMap.get(selected);
                    DefaultListModel<String> listModel = new DefaultListModel<String>();
                    for(int i = 0; i < temp.size(); i++){
                        listModel.addElement(temp.get(i));
                    }
                    versionList.setModel(listModel);
                }
            }
        });

        //instruction text pane
        JTextPane instructions = new JTextPane();
        instructions.setText("INSTRUCTIONS:\n" +
                "Enter in your L.L.Bean Atlassian login information.\n" +
                "Select which release you desire.\n" +
                "Then:\n" +
                    "\tOption 1. Click Automate to export the whole release selected\n" +
                    "\tOption 2. Select a fix version and then click automate to export a specific fix version.");
        instructions.setEditable(false);


        //add the dropdown the the smaller panel
        versionSelectionPanel.add(comboxLabel);
        versionSelectionPanel.add(versionDropdown);

        //panel for the version list
        JPanel versionListPanel = new JPanel();
        versionListPanel.add(listScroller);


        //add the buttons to their smaller panel
        buttonPanel.add(automateButton);


        //add the top components to the larger panel
        topPanel.add(atlassianLoginLabel);
        topPanel.add(usrPanel);
        topPanel.add(pwdPanel);

        //add the bottom components to the larger panel
        bottomPanel.add(versionSelectionPanel);
        bottomPanel.add(versionListPanel);
        bottomPanel.add(buttonPanel);

        //finish adding components and pack the frame
        leftPanel.add(topPanel);
        leftPanel.add(bottomPanel);
        rightPanel.add(instructions);
        mainPanel.add(rightPanel);
        mainPanel.add(leftPanel);
        mainPanel.validate();
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    /*
    PRE: takes a username, a password, and a version as strings
    POST: returns nothing
    BRIEF: creates an instance of the VersionExportAutomation class
     */
    public void runVersionAuto(String username, String password, String version){
        VersionExportAutomation versionAuto = new VersionExportAutomation(username, password, version);

    }

    /*
    PRE: takes a username and a password as strings
    POST: returns nothing
    BRIEF: creates an instance of the ReleaseExportAutomation class
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

        ReleaseExportAutomation releaseAuto1 = new ReleaseExportAutomation(username, password, zqlsearch1);
        ReleaseExportAutomation releaseAuto2 = new ReleaseExportAutomation(username, password, zqlsearch2);
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
