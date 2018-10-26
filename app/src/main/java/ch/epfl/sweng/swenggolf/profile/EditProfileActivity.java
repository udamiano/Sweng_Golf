package ch.epfl.sweng.swenggolf.profile;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import ch.epfl.sweng.swenggolf.Config;
import ch.epfl.sweng.swenggolf.R;
import ch.epfl.sweng.swenggolf.User;
import ch.epfl.sweng.swenggolf.database.DatabaseUser;

import ch.epfl.sweng.swenggolf.tools.FragmentConverter;


public class EditProfileActivity extends FragmentConverter {
    private User user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHomeIcon(R.drawable.ic_baseline_arrow_back_24px);
        setHasOptionsMenu(true);
        View inflated = inflater.inflate(R.layout.activity_edit_profile, container, false);
        inflated.findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChangesAndReturn(v);
            }
        });
        createUserView(inflated);
        return inflated;
    }

    public void createUserView(View view) {
        user = Config.getUser();
        if (user != null) {
            displayElement((EditText) view.findViewById(R.id.edit_name), user.getUserName());
            displayElement((EditText) view.findViewById(R.id.edit_pref), user.getPreference());
            ImageView imageView = findViewById(R.id.ivProfile);
            ProfileActivity.displayPicture(imageView, user, this.getContext());
        }
    }

    private void displayElement(EditText editText, String elem) {
        editText.setText(elem);
        editText.setSelection(elem.length());
    }

    /**
     * Saves the changes and returns to the profile activity.
     */
    public void saveChangesAndReturn(View view) {
        // save new name
        EditText editedName = findViewById(R.id.edit_name);
        String name = editedName.getText().toString();
        user.setUserName(name);

        // save new preferences
        EditText editedPref = findViewById(R.id.edit_pref);
        String pref = editedPref.getText().toString();
        user.setPreference(pref);

        DatabaseUser.addUser(user);

        replaceCentralFragment(new ProfileActivity());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home : {
                replaceCentralFragment(new ProfileActivity());
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
