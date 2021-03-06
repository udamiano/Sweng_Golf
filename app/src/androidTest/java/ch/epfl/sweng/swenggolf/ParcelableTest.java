package ch.epfl.sweng.swenggolf;

import android.os.Parcel;
import android.os.Parcelable;

import org.junit.Test;

import ch.epfl.sweng.swenggolf.database.FilledFakeDatabase;
import ch.epfl.sweng.swenggolf.offer.Offer;
import ch.epfl.sweng.swenggolf.profile.PointType;
import ch.epfl.sweng.swenggolf.profile.User;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ParcelableTest {
    private static final Offer parceledOffer = FilledFakeDatabase.getOffer(0);
    private static final User parceledUser = new User("a", "b", "c", "d", "e", "f",
            PointType.CLOSE_OFFER.getValue());

    private static Parcel writeToParcel(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        return parcel;
    }

    private static <T> void parcelCreator(Parcelable.Creator<T> c, Parcelable p) {
        assertEquals(p, c.createFromParcel(writeToParcel(p)));
    }

    private static <T extends Parcelable> void newArrayHasGoodSize(Parcelable.Creator<T> creator) {
        int size = 10;
        assertThat(creator.newArray(size).length, is(size));
    }

    @Test
    public void parcelConstructor() {
        User u = new User(writeToParcel(parceledUser));
        assertEquals(parceledUser, u);
    }

    @Test
    public void parcelOfferCreator() {
        parcelCreator(Offer.CREATOR, parceledOffer);
    }

    @Test
    public void parcelUserCreator() {
        parcelCreator(User.CREATOR, parceledUser);
    }

    @Test
    public void testDescribeContentsUser() {
        User u = new User();
        assertEquals(0, u.describeContents());
    }

    @Test
    public void testDescribeContentsOffer() {
        Offer o = new Offer();
        assertEquals(0, o.describeContents());
    }

    @Test
    public void newArrayHasGoodSizeOffer() {
        newArrayHasGoodSize(Offer.CREATOR);
    }

    @Test
    public void newArrayHasGoodSizeUser() {
        newArrayHasGoodSize(User.CREATOR);
    }
}
