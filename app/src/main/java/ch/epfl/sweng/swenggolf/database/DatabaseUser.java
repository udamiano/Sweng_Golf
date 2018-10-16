package ch.epfl.sweng.swenggolf.database;

import ch.epfl.sweng.swenggolf.User;

public class DatabaseUser {

    private static ch.epfl.sweng.swenggolf.database.Database db = ch.epfl.sweng.swenggolf.database.Database.getInstance();


    public static void addUser(User user) {
        db.write("/users", user.getUserId(), user);
    }


    public static void getUser(final ValueListener listener, User user) {
        db.read("/users", user.getUserId(), listener, User.class);
    }
}
