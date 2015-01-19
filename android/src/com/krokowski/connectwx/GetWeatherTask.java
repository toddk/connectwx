package com.krokowski.connectwx;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by todd on 1/15/15.
 */
public class GetWeatherTask extends AsyncTask<Location,Void,JSONObject> {

    private static final String BASE_URL =  "http://api.openweathermap.org/data/2.5/weather";

    @Override
    protected JSONObject doInBackground(Location... params) {
        // code adapted from: http://www.vogella.com/tutorials/AndroidJSON/article.html
        StringBuilder builder = new StringBuilder();
        double lat = params[0].getLatitude();
        double lon = params[0].getLongitude();

        HttpClient client = new DefaultHttpClient();

        HttpGet get = new HttpGet(BASE_URL + "?lat=" + lat + "&lon=" + lon);
        JSONObject ret = null;

        try {
            HttpResponse response = client.execute(get);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }

            ret = new JSONObject(builder.toString());
        } catch (Exception ex) {
            Log.e(getClass().getSimpleName(), "Error retrieving weather data", ex);
        }

        return ret;
    }
}
