package ch.epfl.sweng.swenggolf;

import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserTest {

    private final String username = "Bob", id = "1234", email = "Google", photo = "Picsou";


    @Before
    public void setUp(){
        Config.goToTest();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyusername(){
        new User("", id, email, photo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyId(){
        new User(username, "", email, photo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyLogin(){
        new User(username, id, "", photo);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyPhoto() {
        new User(username, id, email, null);
    }

    @Test
    public void testGettersFull(){
        User localUser = new User(username, id, email, photo);
        assertEquals("Usernames not equal", username, localUser.getUserName());
        assertEquals("userIds not equal", id, localUser.getUserId());
        assertEquals("Logins not equal", email, localUser.getEmail());
        assertEquals("Photo is not equal", photo, localUser.getPhoto());
    }


    @Test
    public void testMockFirebaseUser(){
        FirebaseUser fu = TestHelper.getUser();
        User localUser = new User(fu);
        assertEquals("Usernames not equal", TestHelper.getName(), localUser.getUserName());
        assertEquals("userIds not equal", TestHelper.getUid(), localUser.getUserId());
        assertEquals("Logins not equal", TestHelper.getMail(), localUser.getEmail());
        assertNull("Photo is null",localUser.getPhoto());
    }

    @Test
    public void testSetters() {
        User user1 = new User(username, id, email, photo);
        User user2 = new User();
        user2.setEmail(email);
        user2.setUserName(username);
        user2.setPhoto(photo);
        user2.setUserId(id);
        assertEquals(user1.getEmail(), user2.getEmail());
        assertEquals(user1.getPhoto(), user2.getPhoto());
        assertEquals(user1.getUserId(), user2.getUserId());
        assertEquals(user1.getUserName(), user2.getUserName());
    }

    @Test
    public void testUserChanged(){
        User user1 = new User(username, id, email, photo);
        user1 = User.userChanged(user1, "username", "email");
        assertEquals(user1.getUserName(), "username");
        assertEquals(user1.getEmail(), "email");
    }

}
