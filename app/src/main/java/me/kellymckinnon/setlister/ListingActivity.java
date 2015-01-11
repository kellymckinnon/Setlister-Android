package me.kellymckinnon.setlister;

import com.afollestad.materialdialogs.MaterialDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import me.kellymckinnon.setlister.fragments.ListingFragment;

/**
 * Uses a ListingFragment to display list of shows for searched item.
 */
public class ListingActivity extends ActionBarActivity {

    public boolean listClicked = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
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
                    "mailto", getString(R.string.email), null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        String query = getIntent().getStringExtra("QUERY");
        String searchType = getIntent().getStringExtra("SEARCH_TYPE");
        getSupportActionBar().setTitle(query);

        ListingFragment lf = new ListingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("QUERY", query);
        bundle.putString("SEARCH_TYPE", searchType);

        // If search is not through suggestion, this will be null
        bundle.putString("ID", getIntent().getStringExtra("ID"));
        lf.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .add(R.id.activity_list, lf)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        listClicked = false;
    }
}
