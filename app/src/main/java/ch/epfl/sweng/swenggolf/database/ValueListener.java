package ch.epfl.sweng.swenggolf.database;

/**
 * Listener of generic type.
 * Used to read any kind of Data in the Database.
 *
 * @param <T> the type of the value
 */
public interface ValueListener<T> {

    /**
     * Return the value found in the database.
     *
     * @param value the value. The value should be null if it is not found
     */
    void onDataChange(T value);

    /**
     * If there was an error, this method will be called by the database.
     *
     * @param error the error
     */
    void onCancelled(DbError error);
}
