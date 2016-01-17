package aktyagi.com.sunshine;

import android.content.Intent;
import android.media.tv.TvContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.ShareActionProvider;
import android.widget.TextView;

import java.sql.Time;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import aktyagi.com.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static int LOADER_DAF = 0x112211;
    private String mForecast = null;
    private ShareActionProvider mShareActionProvider;
    private static final String[] mProjection = {
            WeatherContract.WeatherEntry.TABLE_NAME+"."+WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES
    };
    private static final int
    COL_WEATHER_ID  = 0,
    COL_DATE        = 1,
    COL_DESC        = 2,
    COL_MAX_TEMP    = 3,
    COL_MIN_TEMP    = 4,
    COL_HUMIDITY    = 5,
    COL_PRESSURE    = 6,
    COL_WINDSPEED   = 7,
    COLUMN_DEGREES  = 8;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(LOADER_DAF, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder=null, selection = null;
        String[] selectionArgs = null;
        Uri uri= getActivity().getIntent().getData();
        CursorLoader cursorLoader;
        cursorLoader = new CursorLoader(getActivity(), uri, mProjection, null, null, null);
        return cursorLoader;
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + " #Sunshine");
        return shareIntent;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detailfragment, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        if(mForecast!=null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        long dateInMillSec = data.getLong(COL_DATE);
        String date = Utility.getFormattedMonthDay(getActivity(), dateInMillSec);
        String day = Utility.getDayName(getActivity(), dateInMillSec);
        String desc = data.getString(COL_DESC);
        String min = getActivity().getString(R.string.format_temperature, data.getDouble(COL_MIN_TEMP));
        String max = getActivity().getString(R.string.format_temperature, data.getDouble(COL_MAX_TEMP));
        String pressure = Utility.getFormattedPressure(getActivity(), data.getDouble(COL_PRESSURE));
        String humidity = Utility.getFormattedHumidity(getActivity(), data.getDouble(COL_HUMIDITY));
        String windSpeed  = Utility.getFormattedWind(getActivity(), data.getFloat(COL_WINDSPEED), data.getFloat(COLUMN_DEGREES));


        mForecast = "#Sunshine - "+date+" Hi/Low "+max+"/"+min+" ("+desc+")";

        View view = getView();
        TextView tv = null;
        tv = (TextView) view.findViewById(R.id.id_detailFragment_day);
        tv.setText(day);

        tv = (TextView) view.findViewById(R.id.id_detailFragment_date);
        tv.setText(date);

        tv = (TextView) view.findViewById(R.id.id_detailFragment_tempHi);
        tv.setText(max);

        tv = (TextView) view.findViewById(R.id.id_detailFragment_tempLow);
        tv.setText(min);

        tv = (TextView) view.findViewById(R.id.id_detailFragment_humidity);
        tv.setText(humidity);

        tv = (TextView) view.findViewById(R.id.id_detailFragment_wind);
        tv.setText(windSpeed);

        tv = (TextView) view.findViewById(R.id.id_detailFragment_pressure);
        tv.setText(pressure);

        tv = (TextView) view.findViewById(R.id.id_detailFragment_desc);
        tv.setText(desc);

        if(mShareActionProvider!=null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
