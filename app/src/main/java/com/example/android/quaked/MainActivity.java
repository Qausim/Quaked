package com.example.android.quaked;

import android.annotation.SuppressLint;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>,
        ContentAdapter.ListItemClickListener {

    private final String TAG = "MainActivity";
    // A final int variable for a unique identifier for the Loader
    private static final int EARTHQUAKE_SEARCH_LOADER = 9;
    // Extra for saving query url between application lifecycle
    private final String SEARCH_QUERY_URL_EXTRA = "search_url_string";
    // Extra for saving an ArrayList of earthquake magnitudes between application lifecycle
    private final String SAVED_MAGNITUDE_EXTRAS = "magnitudes";
    // Extra for saving an ArrayList of earthquake locations between application lifecycle
    private final String SAVED_LOCATION_EXTRAS = "locations";
    // Extra for saving the state of query tasks (canceled or not) between application lifecycle
    // EditText objects for the start time and end time fields
    private EditText startTimeEditText, endTimeEditText;
    // A member RecyclerView variable
    private RecyclerView mRecyclerView;
    // An ArrayList that stores a list of earthquake data
    private ArrayList<Earthquake> earthquakes;
    // mAdapter stores the Adapter for the RecyclerView
    ArrayList<String> magnitudes = new ArrayList<>();
    ArrayList<String> locations = new ArrayList<>();
    private RecyclerView.Adapter mAdapter;
    // Stores the LayoutManager for the RecyclerView
    private RecyclerView.LayoutManager mLayoutManager;
    // A Toast variable
    protected Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind the EditText variables to those on the UI
        startTimeEditText = (EditText) findViewById(R.id.ed_starttime);
        endTimeEditText = (EditText) findViewById(R.id.ed_endtime);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_content);
        // Call setHasFixedSize() with argument "true" to indicate the size of each item layout size
        mRecyclerView.setHasFixedSize(true);
        // Initialize the LayoutManager variable
        mLayoutManager = new LinearLayoutManager(this);
        // Set the layout manager on the RecyclerView
        mRecyclerView.setLayoutManager(mLayoutManager);
        // Initialize the ArrayList variable
        earthquakes = new ArrayList<>();

        // To handle configuration change and transition between the activity lifecycle
        /*if (savedInstanceState != null) {// if savedInstance is not null
            // If savedInstanceState has the keys for magnitudes and locations
            if (savedInstanceState.containsKey(SAVED_MAGNITUDE_EXTRAS) &&
                    savedInstanceState.containsKey(SAVED_LOCATION_EXTRAS)) {
                if (!(locations.isEmpty() && magnitudes.isEmpty())) {
                    locations.clear(); magnitudes.clear();
                }
                // Then get the list of the locations and save in a new ArrayList variable locations
                locations = savedInstanceState.getStringArrayList(
                        SAVED_LOCATION_EXTRAS);
                // And get the list of the locations and save in a new ArrayList variable magnitudes
                magnitudes = savedInstanceState.getStringArrayList(
                        SAVED_MAGNITUDE_EXTRAS);

                // For each item in locations and magnitudes
                for (int ind = 0; ind < locations.size(); ind++) {
                    // Create a new Earthquake object using the magnitude and location at the same
                    // level of index in each of magnitudes and locations (get from them)
                    earthquakes.add(new Earthquake(magnitudes.get(ind), locations.get(ind)));
                }
                // Set Adapter on the RecyclerView variable passing as argument an adapter with data
                // earthquakes
                mRecyclerView.setAdapter(new ContentAdapter(earthquakes, this));
            }
        }*/
        // Initialize a LoaderManager passing the unique identifier for the Loader, null and this
        // as arguments
        getSupportLoaderManager().initLoader(EARTHQUAKE_SEARCH_LOADER, null, this);
    }

    /** Inflates the menu layout resource
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "Inflating menu");
        // Inflate the menu
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /** Handles click event on menu items
     * @param item
     * @return true if implementation is handled otherwise
     * @return super.onOptionsItemSelected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get the id of the selected item
        int selectedItemId = item.getItemId();
        // If the item clicked is "SEARCH"
        if (selectedItemId == R.id.menu_search) {
            Log.d(TAG, "Search was clicked");
            // Get input from each EditText field and store as String variables in startTime and
            // endTime
            String startTime = startTimeEditText.getText().toString();
            Log.d(TAG, "Gotten data entry for start time");
            String endTime = endTimeEditText.getText().toString();
            Log.d(TAG, "Gotten data entry for end time");
            // If any of the fields is empty
            if (TextUtils.isEmpty(startTime) && TextUtils.isEmpty(endTime)) {
                Log.d(TAG, "One or all of text fields empty");
                // Prompt the user to populate all fields
                Toast.makeText(this, "Please populate all fields", Toast.LENGTH_LONG)
                        .show();
            } else {
                Log.d(TAG, "Text fields populated");
                // Build the query url using both startTime and endTime as arguments into the
                // buildUrl method of NetworkUtils class
                URL queryUrl = NetworkUtils.buildUrl(startTime, endTime);
                Log.d(TAG, "URL built: " + queryUrl.toString());
                //earthquakeQueryTask = new EarthquakeQueryTask();
                //earthquakeQueryTask.execute(queryUrl);
                // Call queryEarthquakeData passing the query url as an argument
                queryEarthquakeData(queryUrl);

                // The click has been handled.
                return true;
            }
        }
        // If conditions didn't handle the click
        return super.onOptionsItemSelected(item);
    }

    /** For the AsyncTaskLoader
     * @param id
     * @param args
     * @return a Loader object
     */
    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        // return a new AsyncTaskLoader<String>. Generic type String cuz it is the data type our
        // background task returns passing this as argument
        return new AsyncTaskLoader<String>(this) {

            /** Handles what happens when the Loader starts loading
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {// If args is null do nothing
                    Log.d(TAG, "AsyncTaskLoader onStartLoading stopped because args is null");
                    return;
                }
                // Force a load
                forceLoad();
            }

            /** Handles the background activities
             * Calls getResponseFromHttpConnection of {@link NetworkUtils}
             * @return a String format of the JSON data.
             */
            @Override
            public String loadInBackground() {
                // Initialize a String variable response and set to null
                String response = null;
                // If args has the query url string
                if (args.containsKey(SEARCH_QUERY_URL_EXTRA)) {
                    // Get the url string
                    String queryUrlString = args.getString(SEARCH_QUERY_URL_EXTRA);

                    // If the query url string is null or empty
                    if (queryUrlString == null || TextUtils.isEmpty(queryUrlString)) {
                        Log.d(TAG, "queryUrlString is null or empty, loadInBackground returns null");
                        // return is null
                        return response;
                    }
                    // Else
                    Log.d(TAG, "loadInBackground: queryUrlString = " + queryUrlString);
                    try {
                        // Create a url from the query url string
                        URL queryUrl = new URL(queryUrlString);
                        // Call getResponseFromHttpConnection method of the NetworkUtils class and
                        // pass queryUrl as the argument and save in response
                        response = NetworkUtils.getResponseFromHttpConnection(queryUrl);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // return response
                return response;
            }
        };
    }

    /** Handles what to do with completion of the task and the data returned
     * @param loader
     * @param data
     */
    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        // If data is not empty
        if (!data.equals("") && !data.isEmpty()) {
            try {
                // Get required data from the JSON starting from the root object
                JSONObject rootObject = new JSONObject(data);
                // Get the features array
                JSONArray featuresArray = rootObject.getJSONArray("features");
                // For each item in the array
                for (int ind = 0; ind < featuresArray.length(); ind++) {
                    // Get the object at index ind
                    JSONObject elementJsonObject = featuresArray.getJSONObject(ind);
                    // Get the object with key properties
                    JSONObject elementProperties = elementJsonObject.getJSONObject(
                            "properties");
                    // Get the magnitude and the location and create a new Earthquake object adding
                    // it into the ArrayList earthquakes

                    earthquakes.add(new Earthquake(elementProperties.getString("mag"),
                            elementProperties.getString("place")));
                    Log.d(TAG, "Item " + Integer.valueOf(ind) + " added to earthquake list");
                }
                // Initialize the adapter
                mAdapter = new ContentAdapter(earthquakes, this);
                Log.d(TAG, "Adapter set");
                // Set the adapter on the RecyclerView
                mRecyclerView.setAdapter(mAdapter);
                Log.d(TAG, "RecyclerView set");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Not needed
    }

    /** Starts the AsyncTaskLoader
     * @param url
     */
    private void queryEarthquakeData(URL url) {
        // Initialize a bundle
        Bundle queryBundle = new Bundle();
        // Put the query url string in the bundle
        queryBundle.putString(SEARCH_QUERY_URL_EXTRA, url.toString());
        Log.d(TAG, "URL " + url.toString() + " put in bundle");
        // Initialize a LoaderManager object
        LoaderManager loaderManager = getSupportLoaderManager();
        Log.d(TAG, "LoaderManager set");
        // Initialize a Loader object
        Loader queryLoader = loaderManager
                .getLoader(EARTHQUAKE_SEARCH_LOADER);
        Log.d(TAG, "Loader gotten");

        // If queryLoader is null
        if (queryLoader == null) {
            // Initialize it
            loaderManager.initLoader(EARTHQUAKE_SEARCH_LOADER, queryBundle,
                    this);
            Log.d(TAG, "Loader initialised");
        } else {// Restart it
            loaderManager.restartLoader(EARTHQUAKE_SEARCH_LOADER, queryBundle,
                    this);
            Log.d(TAG, "Loader restarted");
        }
    }


    /*@SuppressLint("NewApi")
    public class EarthquakeQueryTask extends AsyncTask<URL, Void, String> implements ContentAdapter.ListItemClickListener {

        @Override
        protected String doInBackground(URL... urls) {
            queryUrl = urls[0];
            String response = null;
            try {
                response = NetworkUtils.getResponseFromHttpConnection(queryUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            if (!s.equals("") && !s.isEmpty()) {
                if (!earthquakes.isEmpty()) {
                    earthquakes.clear();
                }
                try {
                    JSONObject rootObject = new JSONObject(s);
                    JSONArray featuresArray = rootObject.getJSONArray("features");
                    for (int ind = 0; ind < featuresArray.length(); ind++) {
                        JSONObject elementJsonObject = featuresArray.getJSONObject(ind);
                        JSONObject elementProperties = elementJsonObject.getJSONObject(
                                "properties");
                        String magnitude = elementProperties.getString("mag");
                        String location = elementProperties.getString("place");
                        earthquakes.add(new Earthquake(magnitude, location));
                    }
                    mAdapter = new ContentAdapter(earthquakes, this);
                    mRecyclerView.setAdapter(mAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            taskCanceled = true;
        }

        @Override
        public void onListItemClick(int clickedItemIndex) {
            if (mToast != null) {
                mToast.cancel();
            }
            StringBuilder sb = new StringBuilder("item ");
            sb.append(String.valueOf(clickedItemIndex));
            sb.append(" clicked");
            mToast = Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG);
            mToast.show();
        }
    }*/


    @Override
    public void onListItemClick(int clickedItemIndex) {
        if (mToast != null) {
            mToast.cancel();
        }
        StringBuilder sb = new StringBuilder("item ");
        sb.append(String.valueOf(clickedItemIndex));
        sb.append(" clicked");
        mToast = Toast.makeText(MainActivity.this, sb.toString(), Toast.LENGTH_LONG);
        mToast.show();
    }

    /*@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (!earthquakes.isEmpty()) {
            if (magnitudes != null && locations != null || !(magnitudes.isEmpty() &&
                    locations.isEmpty())) {
                magnitudes.clear(); locations.clear();
            }

            for (int ind = 0; ind < earthquakes.size(); ind++) {
                magnitudes.add(earthquakes.get(ind).getMagnitude());
                locations.add(earthquakes.get(ind).getLocation());
            }
            earthquakes.clear();
            outState.putStringArrayList(SAVED_MAGNITUDE_EXTRAS, magnitudes);
            outState.putStringArrayList(SAVED_LOCATION_EXTRAS, locations);
        }
    }*/
}