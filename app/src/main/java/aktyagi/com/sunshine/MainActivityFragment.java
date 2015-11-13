package aktyagi.com.sunshine;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import java.util.ArrayList;

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
        new FetchWeatherTask(getActivity(), mForecastAdapter).execute(zip);
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
            new FetchWeatherTask(getActivity(), mForecastAdapter).execute(zip);
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
}
