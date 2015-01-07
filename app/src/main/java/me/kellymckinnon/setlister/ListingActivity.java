package me.kellymckinnon.setlister;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class ListingActivity extends ActionBarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        String artist = getIntent().getStringExtra("QUERY");

        if(getIntent().getStringExtra("ID") != null) {
            // TODO: implement search by ID if it's given (via suggestion click)
            System.out.println("THE SEARCHED FOR ID IS: " + getIntent().getStringExtra("ID"));
        }

        getSupportActionBar().setTitle(artist);

        if (savedInstanceState == null) {
            ListingFragment lf = new ListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString("ARTIST_NAME", artist);
            lf.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .add(R.id.activity_list, lf)
                    .commit();
        }

    }
}
