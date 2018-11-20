package ch.epfl.sweng.swenggolf.main;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ch.epfl.sweng.swenggolf.Config;
import ch.epfl.sweng.swenggolf.R;
import ch.epfl.sweng.swenggolf.offer.create.CreateOfferActivity;
import ch.epfl.sweng.swenggolf.offer.ListOfferActivity;
import ch.epfl.sweng.swenggolf.offer.ListOwnOfferActivity;
import ch.epfl.sweng.swenggolf.preference.ListPreferencesActivity;
import ch.epfl.sweng.swenggolf.profile.User;
import ch.epfl.sweng.swenggolf.tools.FragmentConverter;


public class MainMenuActivity extends AppCompatActivity {

    private final User user = Config.getUser();
    private FragmentManager manager;
    private View nav;

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        setContentView(R.layout.activity_main_menu);
        setToolBar();
        nav = ((NavigationView) (this.findViewById(R.id.drawer))).getHeaderView(0);
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadProfileActivity(null);
            }
        });
        setUserDisplay();
        if (savedInstances == null) {
            launchFragment();
        }
    }

    private void setToolBar() {
        android.support.v7.widget.Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void launchFragment() {
        Fragment offerList = new ListOfferActivity();
        manager = getSupportFragmentManager();
        FragmentTransaction transaction =
                manager.beginTransaction().add(R.id.centralFragment, offerList);
        transaction.commit();
    }

    private void setUserDisplay() {
        if (user != null) {
            setUserName();
            setUserMail();
            setUserPic();
        }
    }

    private void setValue(String textValue, TextView textField) {
        if (textValue != null && !textValue.isEmpty() && textField != null) {
            textField.setText(textValue);
        }
    }

    private void setUserName() {
        setValue(user.getUserName(), (TextView) nav.findViewById(R.id.username));
    }

    private void setUserMail() {
        setValue(user.getEmail(), (TextView) nav.findViewById(R.id.usermail));
    }

    private void setUserPic() {
        int errorDrawable = android.R.drawable.btn_dialog;
        if (!user.getPhoto().isEmpty()) {
            ImageView userpicView = nav.findViewById(R.id.userpic);
            Picasso.with(this).load(user.getPhoto()).error(errorDrawable).into(userpicView);
        }
    }

    private void replaceCentralFragment(Fragment fragment) {
        manager.beginTransaction().replace(R.id.centralFragment, fragment).commit();
        DrawerLayout drawerLayout = findViewById(R.id.side_menu);
        drawerLayout.closeDrawers();
    }

    /**
     * Launches the ProfileActivity.
     *
     * @param item the menu item that triggers the activity
     */
    public void loadProfileActivity(MenuItem item) {
        replaceCentralFragment(FragmentConverter.createShowProfileWithProfile(user));
    }

    /**
     * Launches the ShowOffersActivity.
     *
     * @param item the menu item that triggers the activity
     */
    public void loadShowOffersActivity(MenuItem item) {
        replaceCentralFragment(new ListOfferActivity());
    }

    public void loadListOwnOfferActivity(MenuItem item) {
        replaceCentralFragment(new ListOwnOfferActivity());
    }

    /**
     * Launches the PreferenceListActivity.
     *
     * @param item the menu item that triggers the activity
     */
    public void loadPreferenceListActivity(MenuItem item) {
        replaceCentralFragment(new ListPreferencesActivity());
    }

    /**
     * Launches the createOfferActivity.
     *
     * @param item the menu item that triggers the activity
     */
    public void createOfferActivity(MenuItem item) {
        replaceCentralFragment(new CreateOfferActivity());
    }
}
