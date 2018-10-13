package ch.epfl.sweng.swenggolf.database;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import ch.epfl.sweng.swenggolf.Database;
import ch.epfl.sweng.swenggolf.DatabaseFirebase;
import ch.epfl.sweng.swenggolf.ExistsOnData;
import ch.epfl.sweng.swenggolf.TestMode;
import ch.epfl.sweng.swenggolf.User;
import ch.epfl.sweng.swenggolf.main.MainMenuActivity;

public class WaitingActivity extends AppCompatActivity {

    User user;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ch.epfl.sweng.swenggolf.R.layout.activity_waiting);
        user = TestMode.getUser();
        myRef = TestMode.getRef();
        changeActivity();
    }

    /**
     * Switch the actual activity to the MainMenuActivity.
     */
    private void goToMainMenu(){
        startActivity(new Intent(WaitingActivity.this, MainMenuActivity.class));
    }

    /**
     * Switch the actual activity to CreateUserActivity.
     */
    private void goToCreate(){
        startActivity(new Intent(WaitingActivity.this, CreateUserActivity.class));
    }

    /**
     * Choose which activity to launch next.
     */
    public void changeActivity(){
        Database d = new DatabaseFirebase(myRef.child("users").child(user.getUserId()));
        d.containsUser(new ExistsOnData() {
            @Override
            public void onSuccess(Boolean exists) {
                if(exists) {
                    goToMainMenu();
                } else {
                    goToCreate();
                }
            }
            @Override
            public void onStart() {
                //when starting
                Log.d("ONSTART", "Started");
            }

            @Override
            public void onFailure() {
                Log.d("onFailure", "Failed");
            }
        }, user);
    }

}
