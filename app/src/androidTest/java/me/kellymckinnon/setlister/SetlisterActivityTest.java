package me.kellymckinnon.setlister;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.view.View;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.rule.ActivityTestRule;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SetlisterActivityTest {

  @Rule
  public ActivityTestRule<SetlisterActivity> mActivityRule =
      new ActivityTestRule<>(SetlisterActivity.class);

  private MockWebServer mMockWebServer;

  @Before
  public void setUp() throws IOException {
    mMockWebServer = new MockWebServer();
    mMockWebServer.start(8080);
  }

  @After
  public void tearDown() throws IOException {
    mMockWebServer.shutdown();
  }

  @Test
  public void testAppIsLaunched() {
    onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    onView(withId(R.id.search_header)).check(matches(isDisplayed()));
    onView(withId(R.id.search_bar)).check(matches(isDisplayed()));
  }

  @Test
  public void testLoadingSpinnerShows() {
    onView(withId(R.id.search_bar)).perform(typeText("Four"));
    onView(withId(R.id.loading_suggestions)).check(matches(isDisplayed()));
  }

  @Test
  public void testSearchedArtistsAreShown() throws Exception {
    // We'll use mock JSON for the response.
    mMockWebServer.enqueue(
        new MockResponse()
            .setResponseCode(200)
            .setBody(
                AssetReader.getStringFromFile(
                    getInstrumentation().getTargetContext(), "valid_artist_search.json")));

    // Perform a search
    onView(withId(R.id.search_bar)).perform(typeText("Stray"));

    // Wait until the suggestion list is visible
    IdlingRegistry.getInstance()
        .register(
            new ViewVisibilityIdlingResource(
                mActivityRule.getActivity(), R.id.suggestion_list, View.VISIBLE));

    // Verify that the correct result from the search (aka, the mocked response) is displayed
    onView(withText("Stray From the Path")).check(matches(isDisplayed()));
  }
}
