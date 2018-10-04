package ch.epfl.sweng.sweng_golf;

import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.contrib.DrawerActions.openDrawer;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MainMenuActivityTest {
    @Rule public final ActivityTestRule<MainMenuActivity> mMenuRule = new ActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void testCanOpenDrawer() {
        DrawerLayout drawer = mMenuRule.getActivity().findViewById(R.id.side_menu);
        openDrawer(R.id.side_menu);
        assertTrue("drawer was closed",drawer.isDrawerOpen(GravityCompat.START));
    }
}
