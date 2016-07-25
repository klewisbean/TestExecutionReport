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
        main.findFile(main.FILENAME, System.getProperty("user.dir"));
    }

    public void findFile(String nameToFind, String directoryToSearch){
        long newest = 0;
        long newest2 = 0;
        Date date = new Date();
        logger.info("date: " + dateformat.format(date));
        String dateStr = dateformat.format(date);
        String filename = nameToFind;
        File newestfile = new File(filename);
        File newestfile2 = new File(filename);

        File directory = new File(directoryToSearch);

        File[] filesin = directory.listFiles();

        //System.out.println("nameToFind: " + nameToFind + "\nfilename: " + filename);


        for(File fil : filesin){
            if(fil.getName().contains(filename)){
                if(newest < fil.lastModified()){
                    newest = fil.lastModified();
                    newestfile = fil;
                }
            }
        }

        for(File fil : filesin){
            if(fil.getName().contains(filename)){
                if(newest2 < fil.lastModified() && newest > fil.lastModified()){

                    newest2 = fil.lastModified();
                    newestfile2 = fil;
                }
            }
        }

        Date newestd = new Date(newestfile.lastModified());
        logger.info("Newest file: " + newestfile.getName());
        logger.info("Newest2 file: " + newestfile2.getName());

        ScrapeDataXML scrape = new ScrapeDataXML();
        try {

            scrape.run(newestfile, newestfile2, newestd.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




    }


}
