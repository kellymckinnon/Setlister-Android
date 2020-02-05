package me.kellymckinnon.setlister;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import me.kellymckinnon.setlister.artistsearch.ArtistSearchFragment;
import me.kellymckinnon.setlister.common.Utility;
import me.kellymckinnon.setlister.models.SearchedArtist;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.setlistdetail.SetlistFragment;
import me.kellymckinnon.setlister.showlist.ShowListFragment;

/**
 * The launcher activity, which uses a ArtistSearchFragment to guide the user to search for an
 * artist, venue, or city.
 */
public class SetlisterActivity extends AppCompatActivity
    implements ArtistSearchFragment.OnArtistSelectedListener,
        ShowListFragment.OnSetlistSelectedListener {

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_setlister, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_licenses:
        startActivity(new Intent(this, OssLicensesMenuActivity.class));
        return true;
      case R.id.action_feedback:
        Utility.startFeedbackEmail(this);
        return true;
      case android.R.id.home:
        onBackPressed();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .add(R.id.fragment_container, new ArtistSearchFragment())
          .commit();
    }
  }

  @Override
  public void onArtistSelected(SearchedArtist artist) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, ShowListFragment.newInstance(artist))
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onSetlistSelected(Show show) {
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, SetlistFragment.newInstance(show))
        .addToBackStack(null)
        .commit();
  }
}
