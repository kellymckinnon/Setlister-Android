package me.kellymckinnon.setlister.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Helper class that sends a GET or POST request through the passed url and returns
 * the JSON object provided by the API
 */
public class JSONRetriever {

    public static JSONObject getRequest(String url) throws IOException, JSONException {
        return getRequest(url, null, null);
    }

    public static JSONObject postRequest(String url) {
        return postRequest(url, null, null, null);
    }

    public static JSONObject getRequest(String stringURL,
            String authorizationType,
            String authorization) throws IOException, JSONException {
        URL url = new URL(stringURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        if (authorization != null) {
            connection.setRequestProperty("Authorization",
                    authorizationType + " " + authorization);
        }

        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("GET");

        return readHttpResponse(connection);
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
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Helper method that converts a successful response to a JSONObject
     */
    private static JSONObject readHttpResponse(HttpURLConnection connection)
            throws IOException, JSONException {
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK
                && responseCode != HttpsURLConnection.HTTP_CREATED) {
            return null;
        }

        StringBuilder response = new StringBuilder();

        BufferedReader input = new BufferedReader(
                new InputStreamReader(connection.getInputStream()), 8192);
        String strLine;
        while ((strLine = input.readLine()) != null) {
            response.append(strLine);
        }
        input.close();

        return new JSONObject(response.toString());
    }
}
