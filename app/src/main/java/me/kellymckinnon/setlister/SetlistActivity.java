package me.kellymckinnon.setlister;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class SetlistActivity extends ActionBarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setlist, menu);
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

        Intent intent = getIntent();

        String[] songs = intent.getStringArrayExtra("SONGS");
        String artist = intent.getStringExtra("ARTIST");
        String date = intent.getStringExtra("DATE");
        String venue = intent.getStringExtra("VENUE");
        String tour = intent.getStringExtra("TOUR");

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            date = Utility.formatDate(date, "MM/dd/yyyy", "MMMM d, yyyy");
            ab.setTitle(date);
            ab.setSubtitle(artist);
        }

        setContentView(R.layout.activity_setlist);
        if (savedInstanceState == null) {
            SetlistFragment sf = new SetlistFragment();
            Bundle bundle = new Bundle();
            bundle.putStringArray("SONGS", songs);
            bundle.putString("ARTIST", artist);
            bundle.putString("DATE", date);
            bundle.putString("TOUR", tour);
            bundle.putString("VENUE", venue);

            sf.setArguments(bundle);
            getFragmentManager().beginTransaction()
                    .add(R.id.activity_setlist, sf)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
