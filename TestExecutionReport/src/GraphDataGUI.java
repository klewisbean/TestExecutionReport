import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by klewis on 6/21/2016.
 */
public class GraphDataGUI {

    public ArrayList<String> versions = new ArrayList<>();
    public ArrayList<String[]> releaselist = new ArrayList<>();
    public HashMap<String, ArrayList<String[]>> versionmap = new HashMap<>();
    public ArrayList<HashMap<String,Double>> hashmaplist = new ArrayList<>();

    ScrapeDataXML scrape = null;
    /*public GraphDataGUI(ArrayList<String> v, ArrayList<String[]> re, HashMap<String, ArrayList<String[]>> vm){
        this.versions = v;
        this.releaselist = re;
        this.versionmap = vm;

    }*/

    public GraphDataGUI(){

    }

    public GraphDataGUI(File file1, File file2){
        scrape = new ScrapeDataXML();
        try {
            scrape.run(file1, file2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.versions = scrape.versions;
        this.versionmap = scrape.versionmap;
    }

    public void setUp(){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle("Display Data Charts");
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setSize(400,200);

        JPanel mainPanel = new JPanel();
        JPanel radioPanel = new JPanel();
        JPanel dropdownPanel = new JPanel();
        JPanel buttonPanel = new JPanel();

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));

        JLabel label = new JLabel("Display charts by: ");

        JRadioButton releaseRadio = new JRadioButton("Release");
        JRadioButton versionRadio = new JRadioButton("Fix Version");
        releaseRadio.setSelected(true);

        ButtonGroup group = new ButtonGroup();
        group.add(releaseRadio);
        group.add(versionRadio);

        JButton generateButton = new JButton("Generate");

        JComboBox dropdown = new JComboBox();
        for(int i = 0; i < versions.size(); i++){
            dropdown.addItem(versions.get(i));
        }

        dropdown.setVisible(false);

        mainPanel.add(label);
        radioPanel.add(releaseRadio);
        radioPanel.add(versionRadio);

        dropdownPanel.add(dropdown);

        buttonPanel.add(generateButton);

        mainPanel.add(radioPanel);
        mainPanel.add(dropdownPanel);
        mainPanel.add(buttonPanel);
        frame.add(mainPanel);
        frame.setVisible(true);

        releaseRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(releaseRadio.isSelected()){
                    dropdown.setVisible(false);
                }
            }
        });

        versionRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(versionRadio.isSelected()){
                    dropdown.setVisible(true);
                }
            }
        });

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(releaseRadio.isSelected()){
                    //run graphs for overall release filters
                    //device, phase, priority
                    System.out.println("button clicked with release selected");
                    runForRelease();
                }
                else if(versionRadio.isSelected()){
                    String versionSelected = (String)dropdown.getSelectedItem();
                    runForVersion(versionSelected);
                }
            }
        });

    }

    public void runForRelease(){
        System.out.println("in release function");
        hashmaplist = new ArrayList<>();

        HashMap<String, Double> overallphase = scrape.filterPhase(scrape.list);
        HashMap<String, Double> overalldevice = scrape.filterDevice(scrape.list);
        HashMap<String, Double> overallpriority = scrape.filterPriority(scrape.list);

        hashmaplist.add(overalldevice);
        hashmaplist.add(overallphase);
        hashmaplist.add(overallpriority);


        CreateGraph.setHashmaplist(hashmaplist);
        CreateGraph.launch(CreateGraph.class);

        //create.setHashmaplist(hashmaplist);
        System.out.println("created CreateGraph instance, hashmaplist size: " + hashmaplist.size());

    }

    public void runForVersion(String version){

    }

    public void getVersions(String username, String password){
        String auth = new String(com.sun.jersey.core.util.Base64.encode(username + ":" + password));

        Client client = Client.create();
        WebResource webResource = client.resource("https://llbean.atlassian.net/rest/api/2/project/CTTCM/versions");

        ClientResponse response = webResource
                .header("Authorization", "Basic " + auth)
                .accept("application/json")
                .get(ClientResponse.class);

        String output = response.getEntity(String.class);

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

}
