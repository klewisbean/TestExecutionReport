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
    public final static String SHEET_URL = "https://sheetsu.com/apis/v1.0/70989698b61a";


    public static String rel;

    public static Client client = Client.create();

    public static void run(HashMap<String, HashMap<String, Double>> map, String release) throws IOException {
        trustall();

        WebResource webResource = client
                .resource(SHEET_URL);
        rel = release;

        ClientResponse response = webResource.type("application/json").get(ClientResponse.class);

        String output3 = response.getEntity(String.class);

        System.out.println("\nOutput from Server for the GET.... ");
        System.out.println(output3);
        System.out.println("Status: " + response.getStatus());


        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////


        String rowde = "{\"Title\": \"" + release + "\"}";
        System.out.println(rowde);
        response = webResource.type("application/json").post(ClientResponse.class, rowde);

        String rowderesponse = response.getEntity(String.class);

        System.out.println("\nOutput from Server for the POST .... ");
        System.out.println("rowderesponse: " + rowderesponse);
        System.out.println("Status: " + response.getStatus());


        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////
        //start the structure of the sheet using a string
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

        response = webResource.type("application/json").post(ClientResponse.class, input);

        String output2 = response.getEntity(String.class);

        System.out.println("\nOutput from Server for the POST .... ");
        System.out.println(output2);
        System.out.println("Status: " + response.getStatus() + "\n" + input);
        ///////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////



    }

    public static void clearSheet(){
        trustall();

        WebResource webResource = client
                .resource(SHEET_URL);

        rel = rel.replace("\\s", "%20");

        //delete title
        ClientResponse response = client.resource(SHEET_URL + "/Title/" + rel).type("application/json").delete(ClientResponse.class);
        if(response.getStatus() == 204){
            System.out.println(rel + " deleted");
        }
        else{
            System.out.println(SHEET_URL + "/Title/" + rel + " = " + response.getStatus());
        }
        //end delete title

        response = webResource.type("application/json").get(ClientResponse.class);

        String sheetdata = response.getEntity(String.class);

        System.out.println("\nOutput from Server .... ");
        System.out.println(sheetdata);
        System.out.println("Status: " + response.getStatus());

        String filterurl = "/Filter/";

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
