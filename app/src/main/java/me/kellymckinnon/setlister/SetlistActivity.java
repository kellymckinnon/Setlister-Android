package me.kellymckinnon.setlister;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import me.kellymckinnon.setlister.fragments.SetlistFragment;

/**
 * Final activity which uses a SetlistFragment to display the setlist for the selected show, and
 * gives the option to create a Spotify playlist out of this setlist.
 */
public class SetlistActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle arguments = getIntent().getExtras();
    setContentView(R.layout.activity_setlist);

    SetlistFragment sf = new SetlistFragment();
    sf.setArguments(arguments);
    getSupportFragmentManager().beginTransaction().add(R.id.activity_setlist, sf).commit();
  }
}
