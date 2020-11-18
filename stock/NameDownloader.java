package com.wisaterhunep.stock;

import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NameDownloader {

    private static final String TAG = "NameDownloader";
    private static final String URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    public static HashMap<String, String> nameList = new HashMap<>();
    private JSONArray allResults = new JSONArray();

    protected String doInBackground(String... dataIn) {

        Uri.Builder buildURL = Uri.parse(URL).buildUpon();
        String urlToUse = buildURL.build().toString();

        while (urlToUse != null) {

            Log.d(TAG, "doInBackground: " + urlToUse);

            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(urlToUse);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return null;
                }

//                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                urlToUse = parseJSONResults(sb.toString());

            } catch (Exception e) {
                Log.e(TAG, "doInBackground: ", e);
                return null;
            }
        }

        return allResults.toString();
    }

    private String parseJSONResults(String s) {

        try {

            JSONArray dataResults = new JSONArray(s);

            Log.d(TAG, "parseJSONResults: " + dataResults.length());
            for (int i = 0; i < dataResults.length(); i++) {
                JSONObject stockSymbol = dataResults.getJSONObject(i);
                allResults.put(stockSymbol);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(String s) {


        try {
            JSONArray jsonArray = new JSONArray(s);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String symbol = jsonObject.getString("symbol").trim();
                String name = jsonObject.getString("name").trim();

                nameList.put(symbol, name);
            }
            return;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public List<String> matchInput(String input){

        String fixInput = input.toLowerCase();
        List<String> result = new ArrayList<String>();

        for(String key: nameList.keySet()){
            if(key.toLowerCase().contains(fixInput) || nameList.get(key).toLowerCase().contains(fixInput)){
                result.add(key + " - " + nameList.get(key));
            }
        }
        java.util.Collections.sort(result);

        return result;
    }
}

