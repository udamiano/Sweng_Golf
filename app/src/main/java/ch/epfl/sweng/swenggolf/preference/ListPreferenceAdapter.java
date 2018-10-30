package ch.epfl.sweng.swenggolf.preference;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.swenggolf.R;
import ch.epfl.sweng.swenggolf.User;
import ch.epfl.sweng.swenggolf.database.Database;
import ch.epfl.sweng.swenggolf.database.DbError;
import ch.epfl.sweng.swenggolf.database.ValueListener;
import ch.epfl.sweng.swenggolf.profile.ProfileActivity;
import ch.epfl.sweng.swenggolf.tools.ThreeFieldsViewHolder;

public class ListPreferenceAdapter
        extends RecyclerView.Adapter<ListPreferenceAdapter.PreferenceViewHolder> {

    private static final int DEFAULT_PICTURE = R.drawable.common_google_signin_btn_icon_dark;
    private List<User> mDataset = new ArrayList<>();

    /**
     * Create a new adapter for the list that fetches information about users.
     * If debug is set to true, a default list of users is used.
     */
    public ListPreferenceAdapter() {
        Database d = Database.getInstance();
        ValueListener<List<User>> getUserList = new ValueListener<List<User>>() {
            @Override
            public void onDataChange(List<User> value) {
                mDataset = value;
                ListPreferenceAdapter.this.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DbError error) {
                Log.d("DBError", "Failed to load users");
            }
        };
        d.readList("/users", getUserList, User.class);
    }

    public class PreferenceViewHolder extends ThreeFieldsViewHolder {

        /**
         * Create a container for displaying a cell of the view.
         * Container contains user picture, username and preference.
         */
        public PreferenceViewHolder(View view) {
            super(view, R.id.preference_username,
                    R.id.preference_preference, R.id.preference_userpic);
        }
    }

    @NonNull
    @Override
    public PreferenceViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.preferences_list_quad, parent, false);
        final PreferenceViewHolder preferenceHolder = new PreferenceViewHolder(v);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = preferenceHolder.getAdapterPosition();
                User user = mDataset.get(position);
                Bundle bundle = new Bundle();
                bundle.putParcelable("ch.epfl.sweng.swenggolf.user", user);
                ProfileActivity profile = new ProfileActivity();
                profile.setArguments(bundle);
                ((AppCompatActivity)(parent.getContext())).getSupportFragmentManager().beginTransaction().replace(R.id.centralFragment, profile).commit();
            }
        });
        return preferenceHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder, int position) {
        User current = mDataset.get(position);
        Picasso.with(holder.getMainContent().getContext())
                .load(Uri.parse(current.getPhoto()))
                .placeholder(DEFAULT_PICTURE)
                .fit().into((ImageView) holder.getMainContent());
        ((TextView) holder.getTitle()).setText(current.getUserName());
        ((TextView) holder.getSubTitle()).setText(current.getPreference());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
