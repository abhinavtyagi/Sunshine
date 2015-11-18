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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);
        listView = (ListView)rootview.findViewById(R.id.id_listview_forecast);
        listView.setAdapter(mForecastAdapter);
        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateWeather(){
        String zip = Utility.getPreferredLocation(getActivity());
        new FetchWeatherTask(getActivity()).execute(zip);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locSetting, System.currentTimeMillis());
        return new CursorLoader(getActivity(), weatherForLocationUri, null, null, null, sortOrder);
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


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
