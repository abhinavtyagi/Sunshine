package aktyagi.com.sunshine;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends AppCompatActivity {

    private String mForecast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getLayoutInflater().inflate(R.layout.activity_detail, null);

        TextView textview = (TextView) view.findViewById(R.id.detailedActivityTextView);
        Intent intent = getIntent();
        mForecast = intent.getStringExtra("forecast");
        textview.setText(mForecast);
        setContentView(view);
        if(savedInstanceState==null){
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DetailActivityFragment()).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.action_share:
                onShareActionClicked();;
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void onShareActionClicked(){
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast+" #Sunshine");
        shareIntent.setType("text/plain");
        ComponentName componentName = shareIntent.resolveActivity(getPackageManager());
        if(componentName!=null){
            startActivity(shareIntent);
        }
        else {
            Context appContext = getApplicationContext();
            Toast toast = Toast.makeText(appContext, "No Intents Found", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
