package com.llbean.automation.extractor.ctrtcm;

//imports
import org.apache.log4j.Logger;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by klewis on 6/20/2016.
 */

//finds the latest files holding the zephyr executions on the local system
public class FindFileCTRTCM {

    DateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
    String FILENAME = "ZFJ-Executions-";
    final static Logger logger = Logger.getLogger(FindFileCTRTCM.class);

    public static void main(String[] args){
        logger.info("----------------START------------------");
        FindFileCTRTCM main = new FindFileCTRTCM();
        logger.info("user.dir: " + System.getProperty("user.dir"));

        //modify this depending on the environment the program is running in
        //the directory environment may change when running in different environments
        main.findFile(main.FILENAME, System.getProperty("user.dir") + "\\xml-retail");
    }

    public void findFile(String nameToFind, String directoryToSearch){
        long newest = 0;

        Date date = new Date();
        logger.info("date: " + dateformat.format(date));
        String dateStr = dateformat.format(date);
        String filename = nameToFind;
        File newestfile = new File(filename);

        //variable to hold the directory
        File directory = new File(directoryToSearch);

        //list of files withing the directory
        File[] filesin = directory.listFiles();

        //searches through the user directory for the first latest test execution xml file from zephyr for jira
        for(File fil : filesin){
            System.out.println(fil.getName());
            if(fil.getName().contains(filename)){
                if(newest < fil.lastModified()){
                    newest = fil.lastModified();
                    newestfile = fil;
                }
            }
        }

        logger.info("Newest file: " + newestfile.getName());

        if(newestfile.getName().equalsIgnoreCase(filename)){
            File directory2 = new File(directoryToSearch + "\\TestExecutionReport\\xml-retail");

            File[] filesin2 = directory2.listFiles();

            //searches through the user directory for the first latest test execution xml file from zephyr for jira
            for(File fil : filesin2){
                System.out.println(fil.getName());
                if(fil.getName().contains(filename)){
                    if(newest < fil.lastModified()){
                        newest = fil.lastModified();
                        newestfile = fil;
                    }
                }
            }
        }

        logger.info("2Newest file: " + newestfile.getName());
        ScrapeDataXMLCTRTCM scrape = new ScrapeDataXMLCTRTCM();
        try {

            scrape.run(newestfile, date.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




    }


}
