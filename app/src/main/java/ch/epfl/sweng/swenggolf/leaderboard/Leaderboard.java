package ch.epfl.sweng.swenggolf.leaderboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.swenggolf.R;
import ch.epfl.sweng.swenggolf.database.AttributeOrdering;
import ch.epfl.sweng.swenggolf.database.Database;
import ch.epfl.sweng.swenggolf.database.DatabaseUser;
import ch.epfl.sweng.swenggolf.database.DbError;
import ch.epfl.sweng.swenggolf.database.ValueListener;
import ch.epfl.sweng.swenggolf.profile.User;
import ch.epfl.sweng.swenggolf.tools.FragmentConverter;

/**
 * Fragment which shows the LeaderBoard.
 */
public class Leaderboard extends FragmentConverter {

    private static final int LEADERBOARD_SIZE = 10;
    private final List<User> userList = new ArrayList<>();
    protected RecyclerView.LayoutManager mLayoutManager;
    private LeaderboardAdapter mAdapter;
    private TextView errorMessage;
    private TextView noUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstance) {
        userList.clear();

        setToolbar(R.drawable.ic_menu_black_24dp, R.string.leaderboard);
        View inflated = inflater.inflate(R.layout.activity_leaderboard, container, false);
        errorMessage = inflated.findViewById(R.id.error_message);
        noUser = inflated.findViewById(R.id.no_user_to_show);
        setRecyclerView(inflated);
        return inflated;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        openDrawer();
        return true;
    }


    private void setRecyclerView(View inflated) {
        RecyclerView mRecyclerView = inflated.findViewById(R.id.users_recycler_view);
        mLayoutManager = new LinearLayoutManager(this.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdapter = new LeaderboardAdapter(userList, this);
        // Add dividing line
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this.getContext(), LinearLayoutManager.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);


        Database db = Database.getInstance();
        prepareUserData(inflated, db);
    }

    /**
     * Get the users from the database.
     */
    protected void prepareUserData(final View inflated,
                                   Database db) {
        inflated.findViewById(R.id.user_list_loading).setVisibility(View.VISIBLE);
        AttributeOrdering ordering =
                AttributeOrdering.descendingOrdering(DatabaseUser.POINTS, LEADERBOARD_SIZE);

        ValueListener listener = new ValueListener<List<User>>() {

            @Override
            public void onDataChange(List<User> users) {
                if (users.isEmpty()) {
                    noUser.setVisibility(View.VISIBLE);
                } else {
                    noUser.setVisibility(View.GONE);
                    mAdapter.add(users);
                }
                inflated.findViewById(R.id.user_list_loading).setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DbError error) {
                errorMessage.setVisibility(View.VISIBLE);
                Log.d(error.toString(), "Unable to load best users from database");
                inflated.findViewById(R.id.user_list_loading).setVisibility(View.GONE);
            }

        };
        db.readList("users", listener, User.class, ordering);
    }

}
