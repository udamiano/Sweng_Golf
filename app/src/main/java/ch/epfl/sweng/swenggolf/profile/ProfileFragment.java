package ch.epfl.sweng.swenggolf.profile;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import ch.epfl.sweng.swenggolf.Config;
import ch.epfl.sweng.swenggolf.R;
import ch.epfl.sweng.swenggolf.database.CompletionListener;
import ch.epfl.sweng.swenggolf.database.Database;
import ch.epfl.sweng.swenggolf.database.DatabaseUser;
import ch.epfl.sweng.swenggolf.database.DbError;
import ch.epfl.sweng.swenggolf.database.ValueListener;
import ch.epfl.sweng.swenggolf.notification.Notification;
import ch.epfl.sweng.swenggolf.notification.NotificationManager;
import ch.epfl.sweng.swenggolf.notification.NotificationType;
import ch.epfl.sweng.swenggolf.offer.list.own.ListOwnOfferFragment;
import ch.epfl.sweng.swenggolf.tools.FragmentConverter;

import static ch.epfl.sweng.swenggolf.database.DbError.NONE;
import static ch.epfl.sweng.swenggolf.profile.PointType.FOLLOW;
import static ch.epfl.sweng.swenggolf.profile.User.USER;

/**
 * Fragment which shows a profile of an User.
 */
public class ProfileFragment extends FragmentConverter {
    private static final int STAR_OFF = android.R.drawable.btn_star_big_off;
    private static final int STAR_ON = android.R.drawable.btn_star_big_on;
    private User user;
    private boolean isFollowing = false;
    private View inflated;
    private MenuItem button;
    private int fragmentsToSkip;

    protected static void displayPicture(ImageView imageView, User user, Context context) {
        if (!user.getPhoto().isEmpty()) {
            Uri photoUri = Uri.parse(user.getPhoto());
            Picasso.with(context).load(photoUri).into(imageView);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setToolbar(R.drawable.ic_menu_black_24dp, R.string.profile_activity_name);
        inflated = inflater.inflate(R.layout.activity_profile, container, false);
        displayUserData();

        return inflated;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle bundle = getArguments();
        user = bundle.getParcelable("ch.epfl.sweng.swenggolf.user");
        if (user == null) {
            throw new NullPointerException("The user given to ProfileFragment can not be null");
        }
        if (bundle.containsKey(FRAGMENTS_TO_SKIP)) {
            fragmentsToSkip = bundle.getInt(FRAGMENTS_TO_SKIP);
        } else {
            fragmentsToSkip = 0;
        }
    }

    private void displayUserData() {
        TextView name = inflated.findViewById(R.id.name);
        name.setText(user.getUserName());
        ImageView imageView = inflated.findViewById(R.id.ivProfile);
        displayPicture(imageView, user, this.getContext());
        TextView preference = inflated.findViewById(R.id.preference1);
        preference.setText(user.getPreference());
        TextView description = inflated.findViewById(R.id.description);
        description.setText(user.getDescription());
        ImageView badge = inflated.findViewById(R.id.ivBadge);
        badge.setImageResource(Badge.getDrawable(user.getPoints()));
        TextView points = inflated.findViewById(R.id.points);
        points.setText(Integer.toString(user.getPoints()));

        inflated.findViewById(R.id.ind_offers)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable(USER, user);

                        Fragment fragment = new ListOwnOfferFragment();
                        fragment.setArguments(bundle);

                        replaceCentralFragment(fragment);
                    }
                });
    }

    private void showFollowButton() {
        User currentUser = Config.getUser();
        String uid = user.getUserId();
        ValueListener<String> listener = new ValueListener<String>() {
            @Override
            public void onDataChange(String value) {
                if (value != null) {
                    setStar(true);

                } else {
                    setStar(false);
                }
            }

            @Override
            public void onCancelled(DbError error) {
                Log.d("DbError", "Could not load the user follow list");
            }
        };

        Database.getInstance().read(Database.FOLLOWERS_PATH + "/" + currentUser.getUserId(),
                uid, listener, String.class);
    }

    private void setStar(boolean follow) {
        int star = follow ? STAR_ON : STAR_OFF;
        button.setIcon(star);
        isFollowing = follow;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        boolean isCurrent = user.getUserId().equals(Config.getUser().getUserId());
        int id = isCurrent ? R.menu.menu_profile : R.menu.menu_other_user;
        menuInflater.inflate(id, menu);
        if (id == R.menu.menu_other_user) {
            button = menu.findItem(R.id.follow);
            showFollowButton();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                openDrawer();
                return true;
            }
            case R.id.edit_profile: {
                replaceCentralFragment(createEditProfileActivity(fragmentsToSkip));
                return true;
            }
            case R.id.follow: {
                follow();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Follow the user showed in the profile.
     */
    public void follow() {
        User currentUser = Config.getUser();
        if (!isFollowing) {
            addFollow(currentUser);
        } else {
            deleteFollow(currentUser);
        }
    }

    private void deleteFollow(User currentUser) {
        CompletionListener listener = new CompletionListener() {
            @Override
            public void onComplete(DbError error) {
                if (error == NONE) {
                    DatabaseUser.addPointsToUser(-FOLLOW.getValue(), user);
                    setStar(false);
                }
            }
        };
        Database.getInstance().remove("/followers/" + currentUser.getUserId(),
                user.getUserId(), listener);
    }

    private void addFollow(User currentUser) {
        CompletionListener listener = new CompletionListener() {
            @Override
            public void onComplete(DbError error) {
                if (error == NONE) {
                    DatabaseUser.addPointsToUser(FOLLOW.getValue(), user);
                    button.setIcon(STAR_ON);
                    isFollowing = true;
                    NotificationManager.addPendingNotification(user.getUserId(),
                            new Notification(NotificationType.FOLLOW,
                                    Config.getUser(), null));
                    Toast.makeText(ProfileFragment.this.getContext(), getResources()
                                    .getString(R.string.now_following) + " " + user.getUserName(),
                            Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(ProfileFragment.this.getContext(), getResources()
                                    .getString(R.string.error_following) + " " + user.getUserName(),
                            Toast.LENGTH_SHORT)
                            .show();
                }
            }
        };
        Database.getInstance().write("/followers/" + currentUser.getUserId(), user.getUserId(),
                user.getUserId(), listener);
    }

    /**
     * Returns <code>true</code> if the profile is followed.
     *
     * @return <code>true</code> if the profile is followed;
     * <code>false</code> otherwise
     */
    public boolean isFollowing() {
        return isFollowing;
    }
}
