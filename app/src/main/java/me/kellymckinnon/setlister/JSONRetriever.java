package me.kellymckinnon.setlister;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Helper class that sends a GET request through the passed url and returns
 * the JSON object provided by the API
 */
public class JSONRetriever {

    public static JSONObject getRequest(String url) {
        return getRequest(url, null, null);
    }

    public static JSONObject postRequest(String url) {
        return postRequest(url, null, null, null);
    }

    public static JSONObject getRequest(String stringURL,
            String authorizationType,
            String authorization) {
        try {
            URL url = new URL(stringURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (authorization != null) {
                connection.setRequestProperty("Authorization",
                        authorizationType + " " + authorization);
            }

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("GET");

            return readHttpResponse(connection);
        } catch (IOException e) {
            Log.e("JSONRetriever", "IOException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("JSONRetriever", "JSONException");
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject postRequest(String stringURL,
            String authorizationType,
            String authorization,
            JSONObject data) {
        try {
            URL url = new URL(stringURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            if (authorization != null) {
                connection.setRequestProperty("Authorization",
                        authorizationType + " " + authorization);
            }

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestMethod("POST");

            if (data != null) {
                OutputStreamWriter os = new OutputStreamWriter(connection.getOutputStream());
                os.write(data.toString());
                os.close();
            }

            return readHttpResponse(connection);
        } catch (IOException e) {
            Log.e("JSONRetriever", "IOException");
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e("JSONRetriever", "JSONException");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Helper method that converts a successful response to a JSONObject
     */
    public static JSONObject readHttpResponse(HttpURLConnection connection)
            throws IOException, JSONException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK
                && responseCode != HttpsURLConnection.HTTP_CREATED) {
            Log.e("JSONRetriever",
                    "Request failed: response code was " + connection.getResponseCode() + ".");
            return null;
        }

        StringBuilder response = new StringBuilder();

        BufferedReader input = new BufferedReader(
                new InputStreamReader(connection.getInputStream()), 8192);
        String strLine = null;
        while ((strLine = input.readLine()) != null) {
            response.append(strLine);
        }
        input.close();

        return new JSONObject(response.toString());
    }
}
