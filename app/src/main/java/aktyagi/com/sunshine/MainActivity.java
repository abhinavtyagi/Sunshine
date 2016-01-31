package aktyagi.com.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements MainActivityFragment.Callback{

    private String mLocation = null;
    private static String FORECAST_FRAG_TAG = "FFTAG";
    private static String DETAIL_FRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane = false;

    public boolean isTwoPane() {
        return mTwoPane;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.app.ActionBar bar = getSupportActionBar();
        if(findViewById(R.id.id_weather_detail_container)!=null) {
            mTwoPane = true;
            if(savedInstanceState==null) {
                MainActivityFragment mf = new MainActivityFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.id_weather_detail_container, new DetailActivityFragment(), DETAIL_FRAGMENT_TAG)
                        .commit();
            }
        }
        mLocation = Utility.getPreferredLocation(this);
        MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.id_fragment_forecast);
        fragment.setUseTodayLayout(mTwoPane==false);
        Log.w("Info", "onCreate()"+this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("Info", "onStart()"+this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("Info", "onPause()"+this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w("Info", "onStop()"+this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.w("Info", "onDestroy()" + this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.id_fragment_forecast);
        if(fragment!=null) {
            if (mLocation.equalsIgnoreCase(Utility.getPreferredLocation(this))==false) {
                fragment.onLocationChanged();
            }
        }
        Log.w("Info", "onResume()"+this);
    }

    @Override
    public void onItemSelected(Uri uri) {
        if(mTwoPane==true) {
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, uri);
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.id_weather_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }
    }
}
