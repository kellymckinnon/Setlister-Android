package me.kellymckinnon.setlister;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import me.kellymckinnon.setlister.models.Show;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import me.kellymckinnon.setlister.fragments.SetlistFragment;
import me.kellymckinnon.setlister.utils.JSONRetriever;
import me.kellymckinnon.setlister.utils.Utility;

/**
 * Final activity which uses a SetlistFragment to display the setlist for the selected show, and
 * gives the option to create a Spotify playlist out of this setlist.
 */
public class SetlistActivity extends AppCompatActivity {


  private ShareActionProvider mShareActionProvider;
  private Show mShow;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_setlister, menu);
    MenuItem item = menu.findItem(R.id.action_share);
    item.setVisible(true);
    mShareActionProvider =
        (ShareActionProvider) MenuItemCompat.getActionProvider(item);
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    if (mShow != null) {
      updateShareIntent();
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_about) {
      Utility.showAboutDialog(this);
      return true;
    } else if (id == R.id.action_feedback) {
      Utility.startFeedbackEmail(this);
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle arguments = getIntent().getExtras();
    mShow = arguments.getParcelable(SetlisterConstants.EXTRA_SHOW);

    if (mShareActionProvider != null) {
      updateShareIntent();
    }

    setContentView(R.layout.activity_setlist);

    SetlistFragment sf = new SetlistFragment();
    sf.setArguments(arguments);
    getSupportFragmentManager().beginTransaction().add(R.id.activity_setlist, sf).commit();
  }

  /** Provide information for share button */
  private void updateShareIntent() {
    String shareTitle = getString(R.string.setlist_share_title, mShow.getBand(), mShow.getDate(), mShow.getVenue());

    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("text/plain");
    intent.putExtra(Intent.EXTRA_SUBJECT, shareTitle);
    StringBuilder text = new StringBuilder();
    text.append(shareTitle)
        .append(":\n");
    for (String s : mShow.getSongs()) {
      text.append("\n");
      text.append(s);
    }
    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    intent.putExtra(Intent.EXTRA_TEXT, text.toString());
    mShareActionProvider.setShareIntent(intent);
  }

}
