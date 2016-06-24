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
import java.util.Arrays;
import java.util.List;

public class XMLtoSheets {

    public static void run() throws IOException {
        trustall();

        Client client = Client.create();

        WebResource webResource = client
                .resource("https://sheetsu.com/apis/v1.0/bb2e19ec323b");

        //input into the sheet
        /*String input = "\"rows\": [" +
                "{ \"Header 1\": \"data1\", \"Header 2\": \"data2\"}" +
                " ]}";*/

        String input = "{ \"rows\": [" +
                "{ \"Quien\": \"6\", \"Para que\": \"Glenn\", \"email\": \"69\" }," +
                " { \"Quien\": \"7\", \"Para que\": \"Joe\", \"email\": \"98\" }" +
                " ]}";

        ClientResponse response = webResource.type("application/json")
                .post(ClientResponse.class, input);


        if(response.getStatus() != 201){
            throw new RuntimeException("Failed : HTTP error code : " +
                    response.getStatus());
        }

        String output = response.getEntity(String.class);

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
