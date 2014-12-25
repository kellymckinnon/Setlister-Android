package me.kellymckinnon.setlister;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Helper class that sends a GET request through the passed url and returns
 * the JSON object provided by the API
 */
public class JSONRetriever {

    public static JSONObject getJSON(String url) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpUriRequest request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            InputStream inputStream = response.getEntity().getContent();
            String jsonString = "";

            if (inputStream != null) { //Convert inputStream to String
                Writer writer = new StringWriter();
                char[] buffer = new char[1024];
                Reader reader = new BufferedReader(new InputStreamReader(inputStream));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                jsonString = writer.toString();
            }

            return new JSONObject(jsonString);

        } catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
        } catch (JSONException e) {
            System.out.println("No results found.");
        }

        return null;
    }
}
