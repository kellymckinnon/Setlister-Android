package me.kellymckinnon.setlister.artistsearch;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import java.util.List;
import me.kellymckinnon.setlister.R;
import me.kellymckinnon.setlister.common.Utility;
import me.kellymckinnon.setlister.models.Artist;
import me.kellymckinnon.setlister.models.SearchedArtist;

/**
 * Fragment opened by SetlisterActivity that holds a search bar and displays recent results as well
 * as suggestions as the user types
 */
public class ArtistSearchFragment extends Fragment {

  private static final int TRIGGER_SEARCH = 1;
  private static final long SEARCH_DELAY_IN_MS = 500;

  private ProgressBar mLoadingSpinner;
  private EditText mSearchEditText;
  private ListView mSuggestionListView;
  private ArrayAdapter<SearchedArtist> mSuggestionListAdapter;
  private View mRootView;
  private TextView mNoResultsTextView;
  private TextView mNoConnectionTextView;
  private TextView mSuggestionsHeader;
  private TextView mNoRecentSearchesText;
  private OnArtistSelectedListener mOnArtistSelectedListener;
  private String mCurrentSearch;
  private ArtistSearchViewModel mArtistSearchViewModel;
  private Handler mSearchHandler;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mArtistSearchViewModel = ViewModelProviders.of(this).get(ArtistSearchViewModel.class);
    mArtistSearchViewModel
        .getSearchedArtists()
        .observe(
            this,
            artists -> {
              if (mSuggestionListAdapter != null
                  && mSuggestionsHeader != null
                  && mSuggestionsHeader
                      .getText()
                      .equals(getString(R.string.recent_searches_header))) {
                showRecentSearchesView();
              }
            });
  }

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    if (mRootView != null) {
      return mRootView;
    }

    mRootView = inflater.inflate(R.layout.fragment_search, container, false);

    mSuggestionListView = mRootView.findViewById(R.id.suggestion_list);
    mLoadingSpinner = mRootView.findViewById(R.id.loading_suggestions);
    mNoResultsTextView = mRootView.findViewById(R.id.no_results_text);
    mNoConnectionTextView = mRootView.findViewById(R.id.no_connection_text);
    mSearchEditText = mRootView.findViewById(R.id.search_bar);
    mSuggestionsHeader = mRootView.findViewById(R.id.suggestion_header);
    mNoRecentSearchesText = mRootView.findViewById(R.id.no_searches_text);

    final ImageButton clearSearchButton = mRootView.findViewById(R.id.clear_search);
    clearSearchButton.setOnClickListener(
        new ImageButton.OnClickListener() {
          @Override
          public void onClick(View v) {
            mSearchEditText.setText("");
            showRecentSearchesView();

            // Stop searches that have not yet started, but are scheduled to
            mSearchHandler.removeMessages(TRIGGER_SEARCH);
            mArtistSearchViewModel.cancelSearch(mCurrentSearch);
          }
        });

    // Search when enter is pressed
    mSearchEditText.setOnKeyListener(
        new View.OnKeyListener() {
          @Override
          public boolean onKey(View v, int keyCode, KeyEvent event) {
            String text = mSearchEditText.getText().toString();

            // We only want to do this once
            if (event.getAction() != KeyEvent.ACTION_UP) {
              return false;
            }

            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_SEARCH)
                && text.length() != 0) {
              String formattedQuery = Utility.capitalizeFirstLetters(text);
              SearchedArtist searchedArtist = new SearchedArtist(null /* mbid */, formattedQuery);
              mArtistSearchViewModel.insertSearchedArtist(searchedArtist);
              mOnArtistSelectedListener.onArtistSelected(searchedArtist);
              return true;
            }
            return false;
          }
        });

    // Populate list with recent searches
    mSuggestionListAdapter = new ArrayAdapter<>(getActivity(), R.layout.single_line_list_row);
    mSuggestionListView.setAdapter(mSuggestionListAdapter);

    // Search for the clicked suggestion/recent search
    mSuggestionListView.setOnItemClickListener(
        new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SearchedArtist artist =
                (SearchedArtist) mSuggestionListView.getItemAtPosition(position);
            mArtistSearchViewModel.insertSearchedArtist(artist);
            mOnArtistSelectedListener.onArtistSelected(artist);
          }
        });

    return mRootView;
  }

  @Override
  public void onAttach(@NonNull Context context) {
    super.onAttach(context);

    try {
      mOnArtistSelectedListener = (OnArtistSelectedListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement OnArtistSelectedListener");
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(false);
    actionBar.setTitle(R.string.app_name);
    actionBar.setSubtitle(null);

    // Don't search until user has stopped typing
    mSearchHandler =
        new Handler() {
          @Override
          public void handleMessage(Message msg) {
            if (msg.what == TRIGGER_SEARCH && mSearchEditText.getText().toString().length() != 0) {
              showLoadingView();
              startSearch();
            }
          }
        };

    mSearchEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (s.toString().equals(mCurrentSearch)) {
              // This prevents us from re-searching after we return to the fragment.
              return;
            }

            // Stop any current searches
            mSearchHandler.removeMessages(TRIGGER_SEARCH);

            if (s.length() == 0) { // Empty search, show recent searches
              showRecentSearchesView();
            } else { // There is a query, get suggestions
              showLoadingView();
              mSearchHandler.sendEmptyMessageDelayed(TRIGGER_SEARCH, SEARCH_DELAY_IN_MS);
            }
          }
        });

    if (mSuggestionListAdapter != null
        && mSuggestionsHeader != null
        && mSuggestionsHeader.getText().equals(getString(R.string.recent_searches_header))) {
      showRecentSearchesView();
    }
  }

  /** Initiate a search of the selected type */
  private void startSearch() {
    if (mCurrentSearch != null && mCurrentSearch.equals(mSearchEditText.getText().toString())) {
      return; // We're already performing the correct search
    }

    mArtistSearchViewModel.cancelSearch(mCurrentSearch);

    mCurrentSearch = mSearchEditText.getText().toString();

    mArtistSearchViewModel
        .getArtistSuggestions(mCurrentSearch)
        .observe(
            this,
            artists -> {
              if (artists.isEmpty()) {
                showNullState();
                return;
              }

              mSuggestionListAdapter.clear();

              for (Artist artist : artists) {
                mSuggestionListAdapter.add(new SearchedArtist(artist));
              }

              mLoadingSpinner.setVisibility(View.GONE);
              mSuggestionListView.setVisibility(View.VISIBLE);
              mSuggestionListAdapter.notifyDataSetChanged();
            });
  }

  private void showLoadingView() {
    mSuggestionsHeader.setText(getString(R.string.best_matches_header));
    mNoResultsTextView.setVisibility(View.GONE);
    mNoConnectionTextView.setVisibility(View.GONE);
    mLoadingSpinner.setVisibility(View.VISIBLE);
    mSuggestionListView.setVisibility(View.GONE);
    mNoRecentSearchesText.setVisibility(View.GONE);
  }

  private void showRecentSearchesView() {
    mSuggestionsHeader.setText(getString(R.string.recent_searches_header));
    mLoadingSpinner.setVisibility(View.GONE);
    mNoResultsTextView.setVisibility(View.GONE);
    mNoConnectionTextView.setVisibility(View.GONE);

    List<SearchedArtist> searchedArtists = mArtistSearchViewModel.getSearchedArtists().getValue();

    if (searchedArtists == null || searchedArtists.isEmpty()) {
      mSuggestionListView.setVisibility(View.GONE);
      mNoRecentSearchesText.setVisibility(View.VISIBLE);
    } else {
      mNoRecentSearchesText.setVisibility(View.GONE);
      mCurrentSearch = null;
      mSuggestionListAdapter.clear();
      mSuggestionListAdapter.addAll(searchedArtists);
      mSuggestionListView.setVisibility(View.VISIBLE);
    }
  }

  private void showNullState() {
    mLoadingSpinner.setVisibility(View.GONE);
    mSuggestionListView.setVisibility(View.GONE);
    if (getActivity() != null && Utility.isNetworkConnected(getActivity())) {
      mNoResultsTextView.setVisibility(View.VISIBLE);
    } else {
      mNoConnectionTextView.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Callback to inform the activity that the user has selected an artist and we should display
   * relevant setlists for that artist.
   */
  public interface OnArtistSelectedListener {

    /**
     * Notifies listener that artist has been selected
     *
     * @param artist artist selected
     */
    void onArtistSelected(SearchedArtist artist);
  }
}
