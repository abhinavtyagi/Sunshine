package aktyagi.com.sunshine;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import aktyagi.com.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ForecastAdapter mForecastAdapter;
    private ListView listView = null;
    final static int MY_LOADER_ID = 0x12121212;
    private int mPosition;
    final static String LIST_POS="LIST_POS";
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    public MainActivityFragment() {
        boolean b = false;
        if (null!=(MainActivity)getActivity()) {
            b = ((MainActivity)getActivity()).isTwoPane();
        }
        mPosition = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        mForecastAdapter.setUseTodayLayout(((MainActivity)getActivity()).isTwoPane()==false);   // two pane dont use special layout for today
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView)rootview.findViewById(R.id.id_listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String location = Utility.getPreferredLocation(getActivity());
                    Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, cursor.getLong(COL_WEATHER_DATE));
                    ((Callback) getActivity()).onItemSelected(uri);
                }
                mPosition = position;
            }
        });
        if (savedInstanceState!=null) {
            mPosition = savedInstanceState.getInt(LIST_POS);
        }
        return rootview;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(LIST_POS, mPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void updateWeather(){
        String zip = Utility.getPreferredLocation(getActivity());
        new FetchWeatherTask(getActivity()).execute(zip);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        if(mForecastAdapter!=null)
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
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

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(MY_LOADER_ID, null, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if(mPosition!=0) {
            listView.setSelection(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public interface Callback {
        public void onItemSelected(Uri uri);
    }
}
