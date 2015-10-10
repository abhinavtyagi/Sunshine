package aktyagi.com.sunshine;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import junit.framework.Assert;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;


//import org.json.simple.JSONObject;
//import org.json.simple.JSONArray;
//import org.json.simple.parser.ParseException;
import org.json.*;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;
    public MainActivityFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.action_refresh:
                                        updateWeather();
                                        return true;
            case R.id.action_maplocation:
                onPreferredLocationInMap();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void onPreferredLocationInMap(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.preference_location), "94043");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW);
        PackageManager packageManager = getActivity().getPackageManager();
        Uri uri = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",location).build();
        mapIntent.setData(uri);
        ComponentName componentName = mapIntent.resolveActivity(packageManager);
        if(componentName==null){
            Log.e(this.getClass().getName(), "No intent handlers available");
            Toast toast = Toast.makeText(getActivity(), "Error: No app found to show map!", Toast.LENGTH_SHORT);
            toast.show();
        }
        else{
            Log.i(this.getClass().getName(), "IntentFound:" + componentName.toString());
            startActivity(mapIntent);
        }
    }

    private void updateWeather(){
        String[] zip = {"0"};
        SharedPreferences pref =  PreferenceManager.getDefaultSharedPreferences(getActivity());;
        if(pref.contains(getString(R.string.preference_location)))
            zip[0] = pref.getString(getString(R.string.preference_location), "94043");
        FetchWeatherTaskDataInput threadArg = new FetchWeatherTaskDataInput(zip,mForecastAdapter);
        new FetchWeatherTask().execute(threadArg);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArrayList<String> weekForecast = new ArrayList<String>();
        if(weekForecast!=null && weekForecast.size()==0)
        {
            weekForecast.add("Fetching");
            weekForecast.add("Fetching");
            weekForecast.add("Fetching");
            weekForecast.add("Fetching");
            weekForecast.add("Fetching");
            weekForecast.add("Fetching");
            weekForecast.add("Fetching");
        }
        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.id_list_item_forecast_textview,
                weekForecast);
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView)rootview.findViewById(R.id.id_listview_forecast);
        listView.setAdapter(mForecastAdapter);

        {
            String[] zip = {"94043"};
            FetchWeatherTaskDataInput threadArg = new FetchWeatherTaskDataInput(zip, mForecastAdapter);
            new FetchWeatherTask().execute(threadArg);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("forecast", forecast);
                startActivity(intent);
            }
        });

        return rootview;
    }
    public class FetchWeatherTaskDataInput {
        private String[] mZipCodes;
        private ArrayAdapter<String> mListAdapter;
        public FetchWeatherTaskDataInput(String[] zip, ArrayAdapter<String> adapter) {
            mZipCodes = zip;
            mListAdapter = adapter;
        }
        public String[] getZipCodes(){
            return mZipCodes;
        }
        public ArrayAdapter<String> getListAdapter(){
            return mListAdapter;
        }
    }
    public class FetchWeatherTaskOutput {
        String[] mArrStrings;
        ArrayAdapter<String> mListAdapter;
        FetchWeatherTaskOutput(String[] arrStrings, ArrayAdapter<String> listAdapter){
            mArrStrings = arrStrings;
            mListAdapter = listAdapter;
        }
        public String[] getStringsArray(){
            return mArrStrings;
        }
        public ArrayAdapter<String> getListAdapter(){
            return mListAdapter;
        }
    }
    public class FetchWeatherTask extends AsyncTask<FetchWeatherTaskDataInput,Void,FetchWeatherTaskOutput>
    {
        @Override
        protected FetchWeatherTaskOutput doInBackground(FetchWeatherTaskDataInput... input) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            String[] params = input[0].getZipCodes();
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            Context context = getActivity();
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            NetworkInfo.State nwState = networkInfo.getState();
            Assert.assertEquals(nwState, NetworkInfo.State.CONNECTED);
            String[] output = null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                // "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7"
                URIBuilder uriBuilder = new URIBuilder();
                String strURL = uriBuilder.buildURI(params[0]);
                URL url = new URL(strURL);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (Exception e) {
                Log.e("PlaceholderFragment", "Error ", e);

                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                output = new String[7];
                output[0]=output[1]=output[2]=output[3]=output[4]=output[5]=output[6]="Error";
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            if(forecastJsonStr!=null) {
                Log.i("JSON String Messsage", forecastJsonStr);

                try {
                    JSONHelper jHelper = new JSONHelper();
                    output = jHelper.getWeatherDataFromJson(forecastJsonStr, 7);
                } catch (JSONException je) {
                    System.out.println("Catch JSON->" + je);

                } finally {
                    System.out.println("Finally JSON Parsing");
                }
            }
            FetchWeatherTaskOutput out = new FetchWeatherTaskOutput(output, input[0].getListAdapter());
            return out;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(FetchWeatherTaskOutput taskOutput) {
            if(taskOutput==null)
                return;
            String[] strings = taskOutput.getStringsArray();
            ArrayAdapter<String> adapter = taskOutput.getListAdapter();
            if(strings==null)
                return;
            super.onPostExecute(taskOutput);
            mForecastAdapter.clear();
            for(String s: strings)
            {
                mForecastAdapter.add(s);
            }
        }
    }

    /**
     * Builds a URL String
     * @param postCode : postcode string
     * @return String : URL as String
     */
    public class URIBuilder {
        public String buildURI(String postCode)
        {
            String strURLPrefix = "http://api.openweathermap.org/data/2.5/forecast/daily?q=";
            String strURLPostFix = "&mode=json&units=metric&cnt=7&APPID="+getString(R.string.openweather_apikey);
            String strURL = strURLPrefix+postCode+strURLPostFix;
            System.out.println("["+strURL+"]");
            return strURL;
        }
    }

    public class JSONHelper {
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unit = pref.getString(getString(R.string.preference_unit), getString(R.string.preferences_metric_default_value));
            double scaleFactor = 1.0;
            if(unit.equals(getString(R.string.preferences_metric_default_value))==false)
            {
                high = high*1.8+32;
                low  = low *1.8+32;
            }
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v("LOG_TAG", "Forecast entry: " + s);
            }
            return resultStrs;

        }
    }
}
