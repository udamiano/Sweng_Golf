package ch.epfl.sweng.swenggolf;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;

// Just a temporary placeholder class in order to complete the Firebase Implementation
public class UserLocal implements User{
    private final String userName;
    private final String userId;
    private final String email;
    private final Uri photo;

    /**
     * Empty constructor for the listeners of Firebase.
     */
    UserLocal(){
        this.userName = "";
        this.userId = "";
        this.email = "";
        this.photo = Uri.parse("");
    }

    /**
     * Construct a local user from FirebaseUser.
     * @param fu the FirebaseUser
     */
    UserLocal(FirebaseUser fu){
        userName = fu.getDisplayName();
        email = fu.getEmail();
        userId = fu.getUid();
        photo = fu.getPhotoUrl();
    }

    /**
     * Constructor for a user -> will change only placeholder.
     * @param username the usrename
     * @param userId a unique identifier
     * @param email the login method
     * @param photo user photo
     */

    UserLocal(String username, String userId, String email, Uri photo){
        Boolean pho;
        if(!TestMode.isTest()) {
            pho = false;
        }
        else {
            pho = (photo==null);
        }
        if (username.isEmpty() || userId.isEmpty() || email.isEmpty() || pho) {
            throw new IllegalArgumentException("Invalid arguments for UserLocal");
        }
        this.userName = username;
        this.userId = userId;
        this.email = email;
        this.photo = photo;
    }


    /**
     * Constructor for a user with a null photo-> will change only placeholder.
     * @param username the usrename
     * @param userId a unique identifier
     * @param email the login method
     */

    UserLocal(String username, String userId, String email){
        if (username.isEmpty() || userId.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Invalid arguments for UserLocal");
        }
        this.userName = username;
        this.userId = userId;
        this.email = email;
        this.photo = null;
    }

    @Override
    public String getUserId(){
        return this.userId;
    }

    @Override
    public String getUserName(){
        return this.userName;
    }

    @Override
    public String getEmail(){
        return this.email;
    }

    @Override
    public Uri getPhoto() {
        return photo;
    }

}
