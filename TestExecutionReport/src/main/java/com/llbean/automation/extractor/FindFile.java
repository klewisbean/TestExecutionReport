package com.llbean.automation.extractor;//imports
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by klewis on 6/20/2016.
 */

//finds the latest files holding the zephyr executions on the local system
public class FindFile {

    DateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy HH:mm");
    String FILENAME = "ZFJ-Executions-";
    final static Logger logger = Logger.getLogger(FindFile.class);

    public static void main(String[] args){
        logger.info("----------------START------------------");
        FindFile main = new FindFile();
        logger.info("user.dir: " + System.getProperty("user.dir"));

        //modify this depending on the environment the program is running in
        //the directory environment may change when running in different environments
        main.findFile(main.FILENAME, System.getProperty("user.dir") + "\\xml-content");
    }

    //finds the two latest exported test execution xml files
    public void findFile(String nameToFind, String directoryToSearch){
        long newest = 0;
        long newest2 = 0;
        Date date = new Date();
        logger.info("date: " + dateformat.format(date));
        //directoryToSearch = directoryToSearch.substring(0, directoryToSearch.lastIndexOf("\\"));
        //logger.info("new directoryToSearch: " + directoryToSearch);
        String dateStr = dateformat.format(date);
        String filename = nameToFind;
        File newestfile = new File(filename);
        File newestfile2 = new File(filename);

        //variable to hold the directory to search
        File directory = new File(directoryToSearch);

        //list of the files in the desired directory
        File[] filesin = directory.listFiles();

        //searches through the user directory for the first latest test execution xml file from zephyr for jira
        for(File fil : filesin){
            if(fil.getName().contains(filename)){
                if(newest < fil.lastModified()){
                    newest = fil.lastModified();
                    newestfile = fil;
                }
            }
        }

        //searches through the user directory for the second latest test execution xml file from zephyr for jira
        for(File fil : filesin){
            if(fil.getName().contains(filename)){
                if(newest2 < fil.lastModified() && newest > fil.lastModified()){
                    newest2 = fil.lastModified();
                    newestfile2 = fil;
                }
            }
        }

        Date newestd = new Date(newestfile.lastModified()*10);
        logger.info("newestd: " + newestd);
        logger.info("Newest file: " + newestfile.getName());
        logger.info("Newest2 file: " + newestfile2.getName());

        if(newestfile.getName().equalsIgnoreCase(filename)){
            File directory2 = new File(directoryToSearch + "\\TestExecutionReport");

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

            //searches through the user directory for the second latest test execution xml file from zephyr for jira
            for(File fil : filesin2){
                System.out.println(fil.getName());
                if(fil.getName().contains(filename)){
                    if(newest2 < fil.lastModified() && newest > fil.lastModified()){

                        newest2 = fil.lastModified();
                        newestfile2 = fil;
                    }
                }
            }
        }

        logger.info("2Newest file: " + newestfile.getName());
        logger.info("2Newest2 file: " + newestfile2.getName());
        ScrapeDataXML scrape = new ScrapeDataXML();
        try {

            scrape.run(newestfile, newestfile2, date.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




    }


}
