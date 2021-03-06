package ch.epfl.sweng.swenggolf;

import android.content.Intent;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.contrib.DrawerMatchers;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import ch.epfl.sweng.swenggolf.database.Database;
import ch.epfl.sweng.swenggolf.database.DatabaseUser;
import ch.epfl.sweng.swenggolf.database.DbError;
import ch.epfl.sweng.swenggolf.database.FakeDatabase;
import ch.epfl.sweng.swenggolf.database.FilledFakeDatabase;
import ch.epfl.sweng.swenggolf.database.LocalDatabase;
import ch.epfl.sweng.swenggolf.database.ValueListener;
import ch.epfl.sweng.swenggolf.main.MainMenuActivity;
import ch.epfl.sweng.swenggolf.offer.Category;
import ch.epfl.sweng.swenggolf.offer.Offer;
import ch.epfl.sweng.swenggolf.profile.User;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static android.support.test.espresso.matcher.ViewMatchers.hasChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasMinimumChildCount;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.fail;


@RunWith(AndroidJUnit4.class)
public class ListOfferFragmentTest {

    public static final String LOREM = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."
            + "Nam ut quam ornare, fringilla nunc eget, facilisis lectus."
            + "Curabitur ut nunc nec est feugiat commodo. Nulla vel porttitor justo."
            + "Suspendisse potenti. Morbi vehicula ante nibh,"
            + " at tristique tortor dignissim non."
            + "In sit amet ligula tempus, mattis massa dictum, mollis sem."
            + "Mauris convallis sed mauris ut sodales."
            + "Nullam tristique vel nisi a rutrum. Sed commodo nec libero sed volutpat."
            + "Fusce in nibh pharetra nunc pellentesque tempor id interdum est."
            + "Sed rutrum mauris in ipsum consequat, nec scelerisque nulla facilisis.";
    @Rule
    public final IntentsTestRule<MainMenuActivity> mActivityRule =
            new IntentsTestRule<>(MainMenuActivity.class, false, false);
    private static final Offer offer1 = (new Offer.Builder()).setUserId("user_id")
            .setTitle("This is a title").setCreationDate(1)
            .setDescription(LOREM).setUuid("idoftheoffer1").build();
    private static final Offer offer2 = (new Offer.Builder()).setUserId("user_id")
            .setTitle("This is a title 2").setCreationDate(0)
            .setDescription(LOREM).setUuid("idoftheoffer2").build();

    public static RecyclerViewMatcher withRecyclerView(final int recyclerViewId) {
        return new RecyclerViewMatcher(recyclerViewId);
    }

    /**
     * Set up a fake database with two offers.
     */
    protected static void setUpFakeDatabase() {
        Database database = new FakeDatabase(true);
        database.write("/offers", "idoftheoffer1", offer1);
        database.write("/offers", "idoftheoffer2", offer2);
        for (Offer offer : FilledFakeDatabase.getClosedOffers()) {
            database.write("/offers", offer.getUuid(), offer);
        }
        Database.setDebugDatabase(database);
        Config.setUser(new User("aaa", "user_id", "ccc", "ddd"));
        DatabaseUser.addUser(Config.getUser());
    }

    /**
     * Configures a fake database and enables TestMode.
     */
    @Before
    public void init() {
        setUpFakeDatabase();
        Config.goToTest();
        mActivityRule.launchActivity(new Intent());
        // reinit local database to default values just in case test are not independent
        LocalDatabase localDb = new LocalDatabase(mActivityRule.getActivity(), null, 1);
        localDb.writeCategories(Arrays.asList(Category.values()));
    }

    @Test
    public void refreshActuallyRefreshes() {
        Offer newOffer = FilledFakeDatabase.getOffer(0);
        onView(withId(R.id.offers_recycler_view)).check(matches(hasChildCount(2)));
        Database.getInstance().write(Database.OFFERS_PATH, newOffer.getUuid(), newOffer);
        onView(withId(R.id.refresh_list_offer))
                .perform(withCustomConstraints(swipeDown(), isDisplayingAtLeast(80)));
        onView(withId(R.id.offers_recycler_view))
                .check(matches(hasChildCount(3)));
    }

    /**
     * <@see https://stackoverflow.com/questions/33505953/espresso-how-to-test-swiperefreshlayout>
     */
    private static ViewAction withCustomConstraints(
            final ViewAction action, final Matcher<View> constraints) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return constraints;
            }

            @Override
            public String getDescription() {
                return action.getDescription();
            }

            @Override
            public void perform(UiController uiController, View view) {
                action.perform(uiController, view);
            }
        };
    }

    @Test
    public void offerCorrectlyDisplayedInTheList() {
        Offer offer = offer1;

        onView(withRecyclerView(R.id.offers_recycler_view).atPosition(0))
                .check(matches(hasDescendant(withText(offer.getTitle()))));
        onView(withRecyclerView(R.id.offers_recycler_view).atPosition(0))
                .check(matches(hasDescendant(withText(offer.getShortDescription()))));

        DatabaseUser.getUser(new ValueListener<User>() {
            @Override
            public void onDataChange(User value) {
                onView(withRecyclerView(R.id.offers_recycler_view).atPosition(0))
                        .check(matches(hasDescendant(withText(value.getUserName()))));
            }

            @Override
            public void onCancelled(DbError error) {
                fail();
            }
        }, offer.getUserId());
    }

    @Test
    public void canOpenMenu() {
        onView(withContentDescription("abc_action_bar_home_description")).perform(click());
        onView(withId(R.id.side_menu)).check(matches(DrawerMatchers.isOpen()));
    }

    @Test
    public void offerCorrectlyDisplayedAfterClickOnList() {
        onView(withId(R.id.offers_recycler_view)).perform(actionOnItem(hasDescendant(
                ViewMatchers.withText(offer1.getTitle())),
                click()));
    }

    @Test
    public void offerCorrectlyExpandedAndRetractedAfterLongPressOnList() {
        Offer offerToTest = offer1;
        Offer otherOffer = offer2;

        String longDescription = offerToTest.getDescription();
        String shortDescription = offerToTest.getShortDescription();

        // Check if short description is displayed, then expand.
        onView(withRecyclerView(R.id.offers_recycler_view).atPosition(0))
                .check(matches(hasDescendant(withText(shortDescription))));

        onView(withId(R.id.offers_recycler_view)).perform(actionOnItem(
                hasDescendant(withText(offerToTest.getTitle())), longClick()));

        // Check that the long description is displayed, and the sort to another offer, then expand
        // other offer.
        onView(withRecyclerView(R.id.offers_recycler_view).atPosition(0))
                .check(matches(hasDescendant(withText(longDescription))));
        onView(withRecyclerView(R.id.offers_recycler_view).atPosition(1))
                .check(matches(hasDescendant(withText(otherOffer.getShortDescription()))));

        closeSoftKeyboard();
        onView(withId(R.id.offers_recycler_view)).perform(actionOnItem(
                hasDescendant(withText(otherOffer.getTitle())), longClick()));

        // Check that the second is expanded.
        onView(withRecyclerView(R.id.offers_recycler_view).atPosition(1))
                .check(matches(hasDescendant(withText(otherOffer.getDescription()))));

        // Close second offer and check if closed.
        onView(withId(R.id.offers_recycler_view)).perform(actionOnItem(
                hasDescendant(withText(otherOffer.getTitle())), longClick()));

        onView(withRecyclerView(R.id.offers_recycler_view).atPosition(1))
                .check(matches(hasDescendant(withText(otherOffer.getShortDescription()))));
    }

    @Test
    public void backFromListIsMenu() {
        onView(withContentDescription("abc_action_bar_home_description")).perform(click());
        onView(withId(R.id.side_menu)).check(matches(DrawerMatchers.isOpen()));
    }

    @Test
    public void allCategoriesUncheckedMessageShown() {
        checkAll();
        onView(withId(R.id.no_offers_to_show)).check(matches(isDisplayed()));
        checkAll();
        onView(withId(R.id.offers_recycler_view))
                .check(matches(hasMinimumChildCount(1)));
    }

    private void checkAll() {
        for (Category cat : Category.values()) {
            clickOnCategoryInMenu(cat);
        }
    }

    private void clickOnCategoryInMenu(Category cat) {
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());
        onView(withText(cat.toString())).perform(click());
    }

    @Test
    public void displaysOnlyNonClosedOffers() {
        onView(withId(R.id.offers_recycler_view)).check(matches(hasChildCount(2)));
    }

    @Test
    public void sortedByCreationDate() {
        RecyclerViewMatcher offersMatcher = new RecyclerViewMatcher(R.id.offers_recycler_view);
        onView(offersMatcher.atPosition(1))
                .check(matches(hasDescendant(withText(offer2.getTitle()))));
    }
}
