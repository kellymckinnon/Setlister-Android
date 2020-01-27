package me.kellymckinnon.setlister;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SetlisterActivityTest {

  @Rule
  public ActivityTestRule<SetlisterActivity> mActivityRule =
      new ActivityTestRule<>(SetlisterActivity.class);

  @Test
  public void appIsLaunched() {
    onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    onView(withId(R.id.search_header)).check(matches(isDisplayed()));
    onView(withId(R.id.search_bar)).check(matches(isDisplayed()));
  }

  @Test
  public void loadingSpinnerShows() {
    onView(withId(R.id.search_bar)).perform(typeText("Four"));
    onView(withId(R.id.loading_suggestions)).check(matches(isDisplayed()));
  }
}
