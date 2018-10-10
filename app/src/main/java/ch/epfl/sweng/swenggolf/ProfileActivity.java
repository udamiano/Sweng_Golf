package ch.epfl.sweng.swenggolf;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import static android.view.Window.FEATURE_ACTION_BAR;

public class ProfileActivity extends AppCompatActivity {

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window currentWindow = getWindow();
        currentWindow.requestFeature(FEATURE_ACTION_BAR); //doesn't seem to work

        setContentView(R.layout.activity_profile);

        uid = getIntent().getStringExtra(MainActivity.EXTRA_USERID);

        TextView name = findViewById(R.id.name);
        name.setText(FakeUserDatabase.accessTable(uid, "name"));
        TextView textView = findViewById(R.id.username);
        String username = FakeUserDatabase.accessTable(uid, "username");
        if (username != null && !username.isEmpty()) {
            String usernameString = "@" + username;
            textView.setText(usernameString);
        }
        TextView offersPosted = findViewById(R.id.offers1);
        offersPosted.setText(FakeUserDatabase.accessTable(uid, "offers_posted"));
        TextView offersAnswered = findViewById(R.id.offers2);
        offersAnswered.setText(FakeUserDatabase.accessTable(uid, "offers_answered"));
    }

    /**
     * Launches the EditProfileActivity.
     * @param view the current view
     */
    public void editProfile(View view) {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra(MainActivity.EXTRA_USERID, uid);
        startActivity(intent);
    }
}
