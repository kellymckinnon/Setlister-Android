package me.kellymckinnon.setlister;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import me.kellymckinnon.setlister.fragments.ListingFragment;

/** Uses a ListingFragment to display list of shows for searched item. */
public class ListingActivity extends AppCompatActivity {

  public boolean listClicked = false;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_list, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_about) {
      new MaterialAlertDialogBuilder(this)
              .setTitle(R.string.about_setlister)
              .setView(R.layout.about_dialog)
              .show();
      return true;
    } else if (id == R.id.action_feedback) {
      Intent emailIntent =
          new Intent(
              Intent.ACTION_SENDTO, Uri.fromParts("mailto", getString(R.string.email), null));
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
    getSupportActionBar().setTitle(query);

    ListingFragment lf = new ListingFragment();
    Bundle bundle = new Bundle();
    bundle.putString("QUERY", query);

    // If search is not through suggestion, this will be null
    bundle.putString("ID", getIntent().getStringExtra("ID"));
    lf.setArguments(bundle);
    getSupportFragmentManager().beginTransaction().add(R.id.activity_list, lf).commit();
  }

  @Override
  protected void onResume() {
    super.onResume();
    listClicked = false;
  }
}
