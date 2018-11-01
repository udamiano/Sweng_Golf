package ch.epfl.sweng.swenggolf;

import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.swenggolf.database.Database;
import ch.epfl.sweng.swenggolf.database.FakeDatabase;
import ch.epfl.sweng.swenggolf.database.FilledFakeDatabase;
import ch.epfl.sweng.swenggolf.main.MainActivity;
import ch.epfl.sweng.swenggolf.storage.FakeStorage;
import ch.epfl.sweng.swenggolf.storage.Storage;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4.class)
public class DeleteOfferTest {

    @Rule
    public final ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    /**
     * Configures a fake database and enables TestMode.
     */
    @Before
    public void init() {
        Database.setDebugDatabase(FakeDatabase.fakeDatabaseCreator());

        Storage storage = new FakeStorage(true);
        Storage.setDebugStorage(storage);

        Config.setUser(FilledFakeDatabase.getUser(0));
        Config.goToTest();
    }

    /**
     * Opens the list activity.
     */
    public void openListActivity() {
        onView(withId(R.id.show_offers_button)).perform(click());
    }

    @Test
    public void cancelDialog() {
        openListActivity();
        onView(withId(R.id.offers_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        ViewInteraction buttonDelete = onView(withId(R.id.button_delete_offer));

        buttonDelete.perform(scrollTo(), click());
        onView(withText(android.R.string.no)).perform(click());
        buttonDelete.perform(scrollTo(), click());
        onView(withText("Delete entry")).check(matches(isDisplayed()));
    }

    @Test
    public void deleteElementTest() {
        openListActivity();
        onView(withId(R.id.offers_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.button_delete_offer)).perform(scrollTo(), click());
        onView(withText(android.R.string.yes)).perform(click());
    }

}