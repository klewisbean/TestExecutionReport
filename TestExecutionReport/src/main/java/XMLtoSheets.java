package main.java;

/**
 * Created by klewis on 6/22/2016.
 */
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.Json;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.Sheets;
import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang3.StringUtils;


import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class XMLtoSheets {

    //actual api
    public static String SHEET_URL = "https://sheetsu.com/apis/v1.0/ff05010c60f3";

    //https://sheetsu.com/apis/v1.0/bb2e19ec323b --> version

    //variable to store the release string globally
    public static String rel;

    //create the client
    public static Client client = Client.create();

    public static void run(HashMap<String, HashMap<String, Integer>> map, String release, String api) throws IOException {
        SHEET_URL = api;
        System.out.println(SHEET_URL);
        //trust all certificates
        trustall();

        //create websource from the sheetsu api url
        WebResource webResource = client
                .resource(SHEET_URL);

        //store the release variable
        rel = release;



        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////


        //create the title row
        String rowde = "{\"Title\": \"" + release + "\"}";
        System.out.println(rowde);

        //attempt to post the title row to the api
        ClientResponse response = webResource.type("application/json")
                .post(ClientResponse.class, rowde);

        //get the response
        String rowderesponse = response.getEntity(String.class);

        //print the response
        System.out.println("\nOutput from Server for the POST of title.... ");
        System.out.println("rowderesponse: " + rowderesponse);
        System.out.println("Status: " + response.getStatus());


        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////
        //start the structure of the sheet using a string
        //create the json structure of the rows for the filters and their data
        String input = "{\"rows\": [";
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
        input += "]}";
        //end structure of the sheet

        //attempt to post the data to the api
        response = webResource.type("application/json")
                .post(ClientResponse.class, input);

        //get the response
        String output2 = response.getEntity(String.class);

        //print out the response
        System.out.println("\nOutput from Server for the POST of the data .... ");
        System.out.println(output2);
        System.out.println("Status: " + response.getStatus() + "\n" + input);
        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////



    }

    //this function will clear the sheet api
    public static void clearSheet(String release, String api){
        SHEET_URL = api;
        //trust all certificates
        trustall();

        //create websource from the api url
        WebResource webResource = client
                .resource(SHEET_URL);

        //get release
        release = release.replace(" ", "%20");


        ClientResponse response = webResource.type("application/json")
                .get(ClientResponse.class);

        //get the response
        String output = response.getEntity(String.class);

        ArrayList<String> title = new ArrayList<>();
        //StringUtils.substringsBetween(output.substring(output.indexOf("Title") + 7), "\"", "\"");
        System.out.println(output.length());
        if(output.length() < 7){
            System.out.println("sheet is already empty");
        }
        else {
            int index = output.indexOf("Title");
            title.add(StringUtils.substringBetween(output.substring(index + 7), "\"", "\""));
            while (index >= 0) {

                index = output.indexOf("Title", index + 1);
                title.add(StringUtils.substringBetween(output.substring(index + 7), "\"", "\""));
            }

            for (int i = 0; i < title.size(); i++) {
                if (title.get(i).length() < 5 || title.get(i) == null) {
                    title.remove(i);
                }
            }

            for (int i = 0; i < title.size(); i++) {
                System.out.println("\"" + title.get(i) + "\"");
            }
            System.out.println(title.size());

            for (int i = 0; i < title.size(); i++) {
                //attempt to delete title

                response = client.resource(SHEET_URL + "/Title/" + title.get(i).replace(" ", "%20"))
                        .type("application/json")
                        .delete(ClientResponse.class);

                if (response.getStatus() == 204) {
                    System.out.println(release + " deleted");
                } else {
                    System.out.println(SHEET_URL + "/Title/" + title + " = " + response.getStatus());
                }
                //end delete title
            }
        }
        //get the rest of the current data from the api to delete
        response = webResource.type("application/json").get(ClientResponse.class);

        //get response
        String sheetdata = response.getEntity(String.class);


        //print the response
        System.out.println("\nOutput from Server .... ");
        System.out.println(sheetdata);
        System.out.println("Status: " + response.getStatus());

        //beginning of string structure to delete
        String filterurl = "/Filter/";

        //iterate through the data stored in the sheet and delete each row
        String temp = sheetdata;
        for(int i = 0; i < StringUtils.countMatches(sheetdata, "Filter"); i++){
            temp = StringUtils.substring(temp, StringUtils.indexOf(temp, "Filter") + "Filter".length() + 1);

            String sub = StringUtils.substringBetween(temp, "\"", "\"");
            sub = sub.replaceAll("\\s", "%20");
            webResource = client.resource(SHEET_URL + filterurl + sub);

            response = webResource.type("application/json").delete(ClientResponse.class);

            if(response.getStatus() == 204){
                System.out.println(sub + " deleted");
            }
            else{
                System.out.println(SHEET_URL + filterurl + sub + " = " + response.getStatus());
            }
        }



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
