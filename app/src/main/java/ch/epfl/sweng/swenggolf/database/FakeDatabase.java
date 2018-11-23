package ch.epfl.sweng.swenggolf.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ch.epfl.sweng.swenggolf.offer.Category;
import ch.epfl.sweng.swenggolf.offer.Offer;

/**
 * Mocked Database which works locally.
 */
public class FakeDatabase extends Database {
    private final Map<String, Object> database;
    private Set<String> workingOnEntry;
    private boolean working;

    /**
     * Create a new FakeDatabase that can be used to mock the Database.
     *
     * @param working the working state of the Database, the DataBase will send
     *                error when working is set at false and will work as
     *                expected otherwise.
     */
    public FakeDatabase(boolean working) {
        this.database = new TreeMap<>();
        this.working = working;
        workingOnEntry = new HashSet<>();
    }

    private static void handleError(String attribute) {
        throw new IllegalArgumentException("The attribute " + attribute + " doesn't exist");
    }

    /**
     * Creates a Database already filled with users and offers.
     *
     * @return an instance of FilledFakeDatabase.
     */
    public static Database fakeDatabaseCreator() {
        return new FilledFakeDatabase();
    }

    @Override
    public void write(@NonNull String path, @NonNull String id, @NonNull Object object) {
        if (working) {
            database.put(path + "/" + id, object);
        }
    }

    @Override
    public void write(@NonNull String path, @NonNull String id, @NonNull Object object,
                      @NonNull CompletionListener listener) {
        if (working) {
            write(path, id, object);
            listener.onComplete(DbError.NONE);
        } else {
            listener.onComplete(DbError.UNKNOWN_ERROR);
        }
    }

    @Override
    public <T> void read(@NonNull String path, @NonNull String id,
                         @NonNull ValueListener<T> listener, @NonNull Class<T> c) {
        String key = path + "/" + id;
        if (isWorkingforEntry(key)) {

            if (database.containsKey(key)) {
                listener.onDataChange((T) database.get(key));
            } else {
                listener.onDataChange(null);
            }
        } else {
            listener.onCancelled(DbError.UNKNOWN_ERROR);
        }
    }

    private boolean isWorkingforEntry(String key) {
        return working && !workingOnEntry.contains(key);
    }

    @Override
    public <T> void readList(@NonNull String path, @NonNull ValueListener<List<T>> listener,
                             @NonNull Class<T> c) {
        if (working) {
            List<T> list = getList(path);
            listener.onDataChange(list);
        } else {
            listener.onCancelled(DbError.UNKNOWN_ERROR);
        }
    }

    @Override
    public <T> void readList(@NonNull String path, @NonNull ValueListener<List<T>> listener,
                             @NonNull Class<T> c, AttributeFilter filter) {
        if (working) {

            List<T> list = getList(path);

            List<T> newList = filterList(c, filter.getAttribute(), filter.getValue(), list);
            listener.onDataChange(newList);
        } else {
            listener.onCancelled(DbError.UNKNOWN_ERROR);
        }
    }

    @Override
    public <T> void readList(@NonNull String path, @NonNull ValueListener<List<T>> listener,
                             @NonNull Class<T> c, AttributeOrdering ordering) {
        if (working) {
            List<T> unsortedList = getList(path);
            List<T> list = sortList(c, ordering, unsortedList);
            listener.onDataChange(list);
        } else {
            listener.onCancelled(DbError.UNKNOWN_ERROR);
        }
    }

    private String getGetter(String attribute) {
        return "get" + Character.toUpperCase(attribute.charAt(0))
                + attribute.substring(1, attribute.length());
    }

    @NonNull
    private <T> List<T> sortList(@NonNull Class<T> c, AttributeOrdering ordering,
                                 List<T> unsortedList) {
        final Method method;
        String getterName = getGetter(ordering.getAttribute());
        try {
            method = c.getDeclaredMethod(getterName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The getter for the attribute does not exist");
        }
        Comparator<T> comparator = getComparator(method);
        Collections.sort(unsortedList, comparator);
        if (ordering.isDescending()) {
            Collections.reverse(unsortedList);
        }
        int minSize = Math.min(ordering.getNumberOfElements(), unsortedList.size());
        return unsortedList.subList(0, minSize);
    }

    @NonNull
    private <T> Comparator<T> getComparator(final Method method) {
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                Object attribute1;
                Object attribute2;
                try {
                    attribute1 = method.invoke(o1);
                    attribute2 = method.invoke(o2);
                } catch (IllegalAccessException e) {
                    throw new IllegalArgumentException("Can't access the attribute");
                } catch (InvocationTargetException e) {
                    throw new IllegalArgumentException("Cannot call method on generic parameter T");
                }
                if (attribute1 instanceof Comparable && attribute2 instanceof Comparable) {
                    return ((Comparable) attribute1).compareTo(attribute2);
                }
                throw new IllegalArgumentException("The attribute is not comparable");
            }
        };
    }

    private <T> List<T> filterList(@NonNull Class<T> c, String attribute, String value,
                                   List<T> list) {
        List<T> newList = new ArrayList<>();
        try {

            //Use reflection to check the attribute
            Method method = c.getDeclaredMethod(getGetter(attribute));

            for (T object : list) {
                if (method.invoke(object).equals(value)) {
                    newList.add(object);
                }
            }

        } catch (IllegalAccessException e) {
            handleError(attribute);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No getter for " + attribute + " attribute");
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Generic type T has no getter for this attribute");
        }
        return newList;
    }

    @Override
    public void remove(@NonNull String path, @NonNull String id,
                       @NonNull CompletionListener listener) {
        if (working) {
            database.remove(path + "/" + id);
            listener.onComplete(DbError.NONE);
        } else {
            listener.onComplete(DbError.UNKNOWN_ERROR);
        }
    }

    @Override
    public void readOffers(@NonNull final ValueListener<List<Offer>> listener,
                           final List<Category> categories) {
        List<Offer> offers = getList(Database.OFFERS_PATH);

        if (working) {
            offers = removeOffersWrongCategories(offers, categories);
            listener.onDataChange(offers);
        } else {
            listener.onCancelled(DbError.UNKNOWN_ERROR);
        }
    }

    private List<Offer> removeOffersWrongCategories(List<Offer> offers, List<Category> categories) {
        List<Offer> list = new ArrayList<>();
        for (Offer o : offers) {
            if (categories.contains(o.getTag())) {
                list.add(o);
            }
        }
        return list;
    }

    @Nullable
    private <T> List<T> getList(@NonNull String path) {
        List<T> list = new ArrayList<>();
        for (Map.Entry<String, Object> entry : database.entrySet()) {
            if (entry.getKey().startsWith(path)) {
                list.add((T) entry.getValue());
            }
        }
        return list;
    }

    /**
     * Set working state of the Database.
     *
     * @param w the working state of the Database, the DataBase will send
     *          error when working is set at false and will work as
     *          expected otherwise.
     */
    public void setWorking(boolean w) {
        working = w;
    }


    /**
     * Allow to disable a specific entry for reading. When reading this entry, there will be an
     * error.
     *
     * @param path the path of the value
     * @param id   the id of the value
     */
    public void setEntryNotWorking(String path, String id) {
        workingOnEntry.add(path + "/" + id);
    }

}
