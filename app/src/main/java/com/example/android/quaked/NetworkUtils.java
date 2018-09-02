package com.example.android.quaked;

import android.net.Uri;
import android.nfc.Tag;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by HP on 6/17/2018.
 */

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";
    private static final String BASE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";
    private static final String FORMAT_PARAM = "format";
    private static final String START_PARAM = "starttime";
    private static final String END_PARAM = "endtime";
    private static String formatType = "geojson";
    private static final String MAX_PARAM = "maxmagnitude";
    private static final String MIN_PARAM = "minmagnitude";
    private static String maxMagnitude = "7";
    private static String minMagitude = "4";



    public static URL buildUrl(String starttime, String endtime) {
        Uri builtUri = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(
                FORMAT_PARAM, formatType)
                .appendQueryParameter(START_PARAM, starttime)
                .appendQueryParameter(END_PARAM, endtime)
                .appendQueryParameter(MAX_PARAM, maxMagnitude)
                .appendQueryParameter(MIN_PARAM, minMagitude).build();
        URL builtUrl = null;
        try {
            builtUrl = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return builtUrl;
    }

    public static String getResponseFromHttpConnection(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        Log.d(TAG, "URL connection opened");
        InputStream inputStream;
        try {
            inputStream = urlConnection.getInputStream();
            Log.d(TAG, "Input stream gotten");

            Scanner scanner = new Scanner(inputStream);
            Log.d(TAG, "Scanner instantiated");
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            Log.d(TAG, "Scanner's hasInput = " + Boolean.valueOf(hasInput));

            if (hasInput) {
                Log.d(TAG, "Retrieving result from input stream");
                return scanner.next();
            }
        } catch (IOException e) {
            Log.d(TAG, "No response");
            e.printStackTrace();
        } finally {
            Log.d(TAG, "Disconnecting URL connection");
            urlConnection.disconnect();
        }
        return null;
    }
}
