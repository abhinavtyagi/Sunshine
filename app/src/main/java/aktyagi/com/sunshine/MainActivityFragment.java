package aktyagi.com.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;
    public MainActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ArrayList<String> weekForecast = new ArrayList<String>();
        if(weekForecast!=null && weekForecast.size()==0)
        {
            weekForecast.add("Today 33 C");
            weekForecast.add("Tomorrow 33 C");
            weekForecast.add("Wednesday 33 C");
            weekForecast.add("Thursday 33 C");
            weekForecast.add("Friday 33 C");
            weekForecast.add("Saturday 33 C");
            weekForecast.add("Sunday 33 C");
        }
        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forecast,
                R.id.id_list_item_forecast_textview,
                weekForecast);
        View rootview = inflater.inflate(R.layout.fragment_main, container, false);
        ListView listView = (ListView)rootview.findViewById(R.id.id_listview_forecast);
        listView.setAdapter(mForecastAdapter);
        return rootview;
    }
}
