package ch.epfl.sweng.swenggolf;

import android.support.annotation.NonNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.sweng.swenggolf.database.AttributeFilter;
import ch.epfl.sweng.swenggolf.database.AttributeOrdering;
import ch.epfl.sweng.swenggolf.database.CompletionListener;
import ch.epfl.sweng.swenggolf.database.Database;
import ch.epfl.sweng.swenggolf.database.DbError;
import ch.epfl.sweng.swenggolf.database.FakeDatabase;
import ch.epfl.sweng.swenggolf.database.ValueListener;
import ch.epfl.sweng.swenggolf.offer.Category;
import ch.epfl.sweng.swenggolf.offer.Offer;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class FakeDatabaseTest {

    private static final String PATH = Database.OFFERS_PATH;
    private static final String ID = "id1";
    private static final String ID2 = "ID2";
    private static final String CONTENT = "This is a string";
    private static final String CONTENT_2 = "This is a second string";
    private final Offer offer1 = (new Offer.Builder()).setUserId("uid1").setTitle("title")
            .setDescription("description").build();
    private final Offer offer2 = (new Offer.Builder(offer1)).setUserId("uid2").build();
    private final List<Offer> LIST = Arrays.asList(offer1, offer2);

    @Test
    public void writeAndReadReturnGoodValues() {
        Database d = new FakeDatabase(true);

        d.write(PATH, ID, CONTENT);

        ValueListener<String> listener = new ValueListener<String>() {
            @Override
            public void onDataChange(String value) {
                assertThat(value, is(CONTENT));
            }

            @Override
            public void onCancelled(DbError error) {
                fail();
            }
        };
        d.read(PATH, ID, listener, String.class);

    }

    @Test
    public void listenIsNotifiedWhenChangeHappens() {
        final Database d = new FakeDatabase(true);
        ValueTestListener testListener = new ValueTestListener();
        d.write(PATH, ID, CONTENT);
        d.listen(PATH, ID, testListener, String.class);
        d.write(PATH, ID, CONTENT_2);
        assertThat(testListener.calls, is(2));
    }

    @Test
    public void deafenDisablesListenning() {
        final Database d = new FakeDatabase(true);
        ValueTestListener testListener = new ValueTestListener();
        d.write(PATH, ID, CONTENT);
        d.listen(PATH, ID, testListener, String.class);
        d.deafen(PATH, ID, testListener);
        d.write(PATH, ID, CONTENT_2);
        assertThat(testListener.calls, is(1));
    }

    @Test
    public void listenNotifiesAllUpperLevels() {
        final Database d = new FakeDatabase(true);
        ValueTestListener testListener = new ValueTestListenerString();
        ValueTestListener<List> testListenerList = new ValueTestListener<>();
        d.write("/x/y", "z", CONTENT);
        d.listen( "/x/y", "z", testListener, String.class);
        d.listen("/x", "y", testListenerList, List.class);
        d.listen("", "", testListenerList, List.class);
        d.write("/x/y", "z", CONTENT_2);
        assertThat(testListener.calls, is(2));
        assertThat(testListener.doesNotExist, is(0));
        assertThat(testListenerList.calls, is(2));
        assertThat(testListenerList.doesNotExist, is(2));
    }

    @Test
    public void readReturnsDataDoesNotExist() {
        Database d = new FakeDatabase(true);
        ValueListener<String> listener = new ValueListener<String>() {
            @Override
            public void onDataChange(String value) {
                fail();
            }

            @Override
            public void onCancelled(DbError error) {
                assertEquals(DbError.DATA_DOES_NOT_EXIST, error);
            }
        };
        d.read(PATH, ID, listener, String.class);
    }

    @Test
    public void writeAndReadListReturnGoodValues() {
        Database d = new FakeDatabase(true);
        d.write(PATH, ID, CONTENT);
        d.write(PATH, ID2, CONTENT_2);

        ValueListener<List<String>> listener = new ValueListener<List<String>>() {
            @Override
            public void onDataChange(List<String> value) {

                assertThat(value.contains(CONTENT), is(true));
                assertThat(value.contains(CONTENT_2), is(true));
            }

            @Override
            public void onCancelled(DbError error) {
                fail();
            }
        };
        d.readList(PATH, listener, String.class);
    }

    @Test
    public void readListAscendingOrderingIsSorted() {
        Database d = setupDatabase();

        ValueListener<List<Offer>> listener = new ValueListener<List<Offer>>() {
            @Override
            public void onDataChange(List<Offer> value) {
                assertThat(value, is(LIST));
            }

            @Override
            public void onCancelled(DbError error) {
                fail();
            }
        };
        AttributeOrdering ordering = AttributeOrdering.ascendingOrdering("userId", 10);
        d.readList(PATH, listener, Offer.class, ordering);
    }

    @Test
    public void readListDescendingOrderingIsSorted() {
        Database d = setupDatabase();
        final List<Offer> expected = new ArrayList<>(LIST);
        Collections.reverse(expected);
        ValueListener<List<Offer>> listener = new ValueListener<List<Offer>>() {
            @Override
            public void onDataChange(List<Offer> value) {
                assertThat(value, is(expected));
            }

            @Override
            public void onCancelled(DbError error) {
                fail();
            }
        };
        AttributeOrdering ordering = AttributeOrdering.descendingOrdering("userId", 10);
        d.readList(PATH, listener, Offer.class, ordering);
    }

    @NonNull
    private Database setupDatabase() {
        Database d = new FakeDatabase(true);
        d.write(Database.OFFERS_PATH, ID, offer1);
        d.write(Database.OFFERS_PATH, ID2, offer2);
        return d;
    }

    @Test
    public void readListReturnEmptyList() {
        Database d = new FakeDatabase(true);

        ValueListener<List<String>> listener = new ValueListener<List<String>>() {
            @Override
            public void onDataChange(List<String> value) {
                assertTrue(value.isEmpty());
            }

            @Override
            public void onCancelled(DbError error) {
                fail();
            }
        };
        d.readList(PATH, listener, String.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void readListWithFilteringThrowExceptionOnInvalidAttribute() {
        FakeDatabase database = new FakeDatabase(true);
        database.readList(PATH, null, Offer.class,
                new AttributeFilter("invalid attribute", "random"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void readListWithOrderingThrowExceptionOnInvalidAttribute() {
        FakeDatabase database = new FakeDatabase(true);
        database.readList(PATH, null, Offer.class,
                AttributeOrdering.ascendingOrdering("invalid attribute", 1));
    }

    private void writeListenerError(boolean working, DbError error) {
        Database d = new FakeDatabase(working);
        CompletionListener listener = new CompletionListener() {
            @Override
            public void onComplete(DbError error) {
                assertThat(error, is(error));
            }
        };
        d.write(PATH, ID, CONTENT, listener);
    }

    @Test
    public void writeListenerReturnNone() {
        writeListenerError(true, DbError.NONE);
    }

    @Test
    public void writeListenerHasError() {
        writeListenerError(false, DbError.UNKNOWN_ERROR);
    }

    @Test
    public void readListenerHasError() {
        Database d = new FakeDatabase(false);

        ValueListener<String> listener = new ValueListener<String>() {
            @Override
            public void onDataChange(String value) {
                fail();

            }

            @Override
            public void onCancelled(DbError error) {
                assertThat(error, is(DbError.UNKNOWN_ERROR));
            }
        };
        d.read(PATH, ID, listener, String.class);
    }

    @Test
    public void readListenerListHasError() {
        Database d = new FakeDatabase(false);

        ValueListener<List<Offer>> listener = getListValueListener();
        d.readList(PATH, listener, Offer.class);
    }

    @Test
    public void readListenerListWithAttributeHasError() {
        Database d = new FakeDatabase(false);

        ValueListener<List<Offer>> listener = getListValueListener();
        d.readList(PATH, listener, Offer.class, new AttributeFilter("s1", "s2"));
    }

    @Test
    public void readListenerListOrderingHasError() {
        Database d = new FakeDatabase(false);
        AttributeOrdering ordering = AttributeOrdering.ascendingOrdering("y", 42);
        d.readList(PATH, getListValueListener(), Offer.class, ordering);
    }

    @Test
    public void readOffersOfUserHasError() {
        Database d = new FakeDatabase(false);

        ValueListener<List<Offer>> listener = getListValueListener();
        d.readOffers(listener, new ArrayList<Category>(), "user");
    }

    @NonNull
    private ValueListener<List<Offer>> getListValueListener() {
        return new ValueListener<List<Offer>>() {
            @Override
            public void onDataChange(List<Offer> value) {
                fail();
            }

            @Override
            public void onCancelled(DbError error) {
                assertThat(error, is(DbError.UNKNOWN_ERROR));
            }
        };
    }

    class ValueTestListenerString extends ValueTestListener<String> {

        @Override
        public void onDataChange(String value) {
            if (calls == 0) {
                assertThat(value, is(CONTENT));
            } else if (calls == 1) {
                assertThat(value, is(CONTENT_2));
            }
            super.onDataChange(value);
        }
    }

    class ValueTestListener<T> implements ValueListener<T> {
        int calls = 0;
        int doesNotExist =0;

        @Override
        public void onDataChange(T value) {
            ++calls;
        }

        @Override
        public void onCancelled(DbError error) {
            if (error == DbError.DATA_DOES_NOT_EXIST){
                doesNotExist++;
            } else {
                fail();
            }
        }
    }


    @Test
    public void writeAndReadThrowNoClassCastException() {

        Database database = new FakeDatabase(true);
        Offer.Builder builder = new Offer.Builder();
        database.write("/offersSaved", "9879", builder);

        ValueListener<List<Offer>> li = new ValueListener<List<Offer>>() {
            @Override
            public void onDataChange(List<Offer> value) {

            }

            @Override
            public void onCancelled(DbError error) {;

            }
        };
        database.readOffers(li);
    }

    @Test
    public void getKeysTwoKeys() {
        String path = Database.OFFERS_PATTERN_PATH + "/username";
        final String key1 = "Key1";
        final String key2 = "Key2";
        String value1 = "value1";
        String value2 = "value2";
        Database database = new FakeDatabase(true);
        database.write(path,key1,value1);
        database.write(path, key2, value2);
        database.getKeys(path, new ValueListener<List<String>>() {
            @Override
            public void onDataChange(List<String> value) {
                assertThat(value.size(), is(2));
                assertTrue(value.contains(key1));
                assertTrue(value.contains(key2));
            }

            @Override
            public void onCancelled(DbError error) {
                fail();
            }
        });
    }

    @Test
    public void getKeysNotWorking() {
        Database database = new FakeDatabase(false);
        database.getKeys(Database.OFFERS_PATTERN_PATH, new ValueListener<List<String>>() {
            @Override
            public void onDataChange(List<String> value) {
                fail();
            }

            @Override
            public void onCancelled(DbError error) {
                assertThat(error, is(DbError.UNKNOWN_ERROR));
            }
        });

    }
}
