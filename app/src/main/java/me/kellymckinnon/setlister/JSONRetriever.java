package me.kellymckinnon.setlister;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Helper class that sends a GET request through the passed url and returns
 * the JSON object provided by the API
 */
public class JSONRetriever {

    public static JSONObject getJSON(String url) {
        return getJSON(url, null, null);
    }

    public static JSONObject getJSON(String stringURL, String authorizationType, String authorization) {
        try {
            StringBuilder response  = new StringBuilder();
            URL url = new URL(stringURL);
            HttpURLConnection httpconn = (HttpURLConnection)url.openConnection();

            if(authorization != null) {
                httpconn.setRequestProperty("Authorization", authorizationType + " " + authorization);
            }

            httpconn.setRequestProperty("Content-Type", "application/json");

            if (httpconn.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                BufferedReader input = new BufferedReader(new InputStreamReader(httpconn.getInputStream()),8192);
                String strLine = null;
                while ((strLine = input.readLine()) != null)
                {
                    response.append(strLine);
                }
                input.close();

                return new JSONObject(response.toString());
            }
        } catch (IOException e) {
            Log.e("JSONRetriever", "IOException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("JSONRetriever", "JSONException");
            e.printStackTrace();
        }

        return null;
    }
}
