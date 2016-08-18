package com.llbean.automation.extractor; /**
 * Created by klewis on 6/22/2016.
 */

//imports
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class XMLtoSheets {

    final static Logger logger = Logger.getLogger(XMLtoSheets.class);

    //api url variable
    public static String SHEET_URL = "";

    //variable to store the release string globally
    public static String rel;

    //create the client
    public static Client client = Client.create();

    //method to create the json structure of the input
    //posts to firebase
    public static void run(HashMap<String, HashMap<String, Integer>> map, String release, String api, String date) throws IOException {
        SHEET_URL = api;

        //trust all certificates
        trustall();

        //store the release variable
        rel = release;
        String input = "";
        if(map.size() == 0){
            ///////////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////
            //start the structure of the sheet using a string
            //create the json structure of the rows for the filters and their data
            input += "{\"rows\": [";
            input += "{\"Title\": \"" + release + "\"}";
            input += ",{\"Date\": \"" + date + "\"}";
            //input += ",{\"Total\": \"" + total + "\"}";
            input += "]}";
            //end structure of the sheet
        }else{
            ///////////////////////////////////////////////////////////////////
            ///////////////////////////////////////////////////////////////////
            //start the structure of the sheet using a string
            //create the json structure of the rows for the filters and their data
            input += "{\"rows\": [";
            Iterator it = map.entrySet().iterator();

            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();

                input += "{\"Filter\": \"" + pair.getKey() + "\",";

                HashMap<String, Integer> temp = (HashMap<String, Integer>) pair.getValue();
                Iterator it2 = temp.entrySet().iterator();
                int count = 0;
                while(it2.hasNext()){
                    Map.Entry pair2 = (Map.Entry)it2.next();
                    input += " \"" + pair2.getKey() + "\": \"" + pair2.getValue();
                    if(count!=0){
                        input += "\",";
                    }
                    else{
                        input += "\",";
                    }
                    count++;
                }
                input = input.substring(0, input.length()-1);
                input += "},";

            }
            input = input.substring(0, input.length()-1);
            input += ",{\"Title\": \"" + release + "\"}";
            input += ",{\"Date\": \"" + date + "\"}";
            //input += ",{\"Total\": \"" + total + "\"}";
            input += "]}";
            //end structure of the sheet
        }



        logger.info("----------INPUT---------");
        logger.info(input);
        logger.info("----------INPUT---------");
        //call method to post to firebase
        postFB(input, SHEET_URL);


        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////



    }

    public static void runStatus(HashMap<String, Integer> map, String release, String api, String date) throws IOException {
        SHEET_URL = api;

        logger.info("map in xmltosheets runstatus: " + map);

        //trust all certificates
        trustall();


        //store the release variable
        rel = release;

        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////
        //start the structure of the sheet using a string
        //create the json structure of the rows for the filters and their data
        String input = "{\"status\": {";
        int count = 0;
        Iterator it = map.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair = (Map.Entry)it.next();
            //System.out.println("pair key: " + pair.getKey());
            if(count == 0){
                input += "\"" + pair.getKey() + "\": \"" + pair.getValue() + "\"";
                count++;
            }else{
                input += ",\"" + pair.getKey() + "\": \"" + pair.getValue() + "\"";
            }
        }
        input += ",\"Title\": \"" + release + "\"";
        input += ",\"Date\": \"" + date + "\"";
        //input += ",{\"Total\": \"" + total + "\"}";
        input += "}}";
        //end structure of the sheet

        logger.info("----------INPUT---------");
        logger.info(input);
        logger.info("----------INPUT---------");
        //call method to post to firebase
        postFB(input, SHEET_URL);


        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////
    }

    //this method will clear the firebase database
    public static void clearFB(String url){
        System.out.println("\nCLEARFIREBASE");
        trustall();

        //database roots
        String[] selArr = {"device", "phase", "priority"};

        //iterate through the database roots to delete each entry
        for(int i = 0; i < selArr.length; i++){
            WebResource webResource = client
                    .resource(url + selArr[i] + ".json");

            ClientResponse response = webResource.type("application/json")
                    .delete(ClientResponse.class);

            //print the status of the delete to the console
            System.out.println("status from delete: " + response.getStatus());
        }
        System.out.println("CLEARFIREBASE");
    }

    //this method will clear the firebase database
    public static void clearFBv(String url){
        System.out.println("\nCLEARFIREBASE");
        trustall();

        WebResource webResource = client
                .resource(url + ".json");

        ClientResponse response = webResource.type("application/json")
                .delete(ClientResponse.class);

        //print the status of the delete to the console
        System.out.println("status from delete: " + response.getStatus());

        System.out.println("CLEARFIREBASE");
    }

    //this method will post a json structure to the firebase database
    public static void postFB(String input, String url) throws IOException {
        System.out.println("\nPOSTFIREBASE");
        trustall();
        //create websource from the api url
        WebResource webResource = client
                .resource(url + ".json");

        ClientResponse response = webResource.type("application/json")
                .post(ClientResponse.class, input);

        //get the response
        String output = response.getEntity(String.class);
        System.out.println("output: " + output);

        //print the status of the post
        System.out.println("status: " + response.getStatus());
        System.out.println("POSTFIREBASE");
    }

    //this method will trust all certificates
    public static void trustall(){

        TrustManager[] trustcerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                    }
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        try{
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustcerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

    }

}
