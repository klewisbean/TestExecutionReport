import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by klewis on 6/20/2016.
 */
public class FindFile {

    DateFormat dateformat = new SimpleDateFormat("MM-dd-yyyy");
    String FILENAME = "ZFJ-Executions-";

    public static void main(String[] args){
        FindFile main = new FindFile();
        main.findFile(main.FILENAME, "C:/users/klewis/downloads");
    }

    public void findFile(String nameToFind, String directoryToSearch){
        long newest = 0;
        long newest2 = 0;
        Date date = new Date();
        //System.out.println(dateformat.format(date));
        String filename = nameToFind;
        File newestfile = new File(filename);
        File newestfile2 = new File(filename);

        File directory = new File(directoryToSearch);

        File[] filesin = directory.listFiles();

        //System.out.println("nameToFind: " + nameToFind + "\nfilename: " + filename);


        for(File fil : filesin){
            if(fil.getName().contains(filename)){
                if(newest < fil.lastModified()){
                    //System.out.println(filename + " | " + fil);
                    newest = fil.lastModified();
                    newestfile = fil;
                }
            }
        }

        for(File fil : filesin){
            if(fil.getName().contains(filename)){
                //System.out.println(filename + " | " + fil);
                if(newest2 < fil.lastModified() && newest > fil.lastModified()){

                    newest2 = fil.lastModified();
                    newestfile2 = fil;
                }
            }
        }


        System.out.println("Newest file: " + newestfile.getName());
        System.out.println("Newest2 file: " + newestfile2.getName());


        ScrapeDataXML scrape = new ScrapeDataXML();
        try {
            scrape.run(newestfile, newestfile2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }




    }


}
