package me.kellymckinnon.setlister;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import me.kellymckinnon.setlister.fragments.ListingFragment;
import me.kellymckinnon.setlister.fragments.SearchFragment;
import me.kellymckinnon.setlister.models.Show;
import me.kellymckinnon.setlister.utils.Utility;

/**
 * The launcher activity, which uses a SearchFragment to guide the user to search for an artist,
 * venue, or city.
 */
public class SearchActivity extends AppCompatActivity implements SearchFragment.OnArtistSelectedListener, ListingFragment.OnSetlistSelectedListener {

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_setlister, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch(item.getItemId()) {
      case R.id.action_about:
        Utility.showAboutDialog(this);
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
          .add(R.id.fragment_container, new SearchFragment())
          .commit();
    }
  }

  @Override
  public void onArtistSelected(String artistName, String artistId) {
    ListingFragment listingFragment = new ListingFragment();
    Bundle args = new Bundle();
    args.putString(SetlisterConstants.EXTRA_ARTIST_NAME, artistName);

    // TODO: Change artistId to be @Nullable, instead of passing "0" everywhere
    if (!artistId.equals("0")) {
      args.putString(SetlisterConstants.EXTRA_ARTIST_ID, artistId);
    }

    listingFragment.setArguments(args);
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, listingFragment)
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onSetlistSelected(Show show) {
    Intent intent = new Intent(this, SetlistActivity.class);
    intent.putExtra(SetlisterConstants.EXTRA_SHOW, show);
    startActivity(intent);
  }
}
