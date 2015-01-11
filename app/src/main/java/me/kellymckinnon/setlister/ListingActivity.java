package me.kellymckinnon.setlister;

import com.afollestad.materialdialogs.MaterialDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class ListingActivity extends ActionBarActivity {

    public boolean listClicked = false;

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
