package me.kellymckinnon.setlister;

import com.afollestad.materialdialogs.MaterialDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * The launcher activity, which uses a SearchFragment to guide the user
 * to search for an artist, tour, venue, or city.
 */
public class SearchActivity extends ActionBarActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_about) {
            new MaterialDialog.Builder(this)
                    .title("About Setlister")
                    .customView(R.layout.about_dialog, true)
                    .positiveText("OK")
                    .show();
            return true;
        } else if (id == R.id.action_feedback) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto", "setlisterapp@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Setlister Feedback");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.activity_search, new SearchFragment())
                    .commit();
        }
    }
}
