package ch.epfl.sweng.swenggolf.offer;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ch.epfl.sweng.swenggolf.Config;
import ch.epfl.sweng.swenggolf.R;
import ch.epfl.sweng.swenggolf.User;
import ch.epfl.sweng.swenggolf.database.Database;
import ch.epfl.sweng.swenggolf.database.DatabaseUser;
import ch.epfl.sweng.swenggolf.database.DbError;
import ch.epfl.sweng.swenggolf.database.ValueListener;
import ch.epfl.sweng.swenggolf.tools.ThreeFieldsViewHolder;

public class ListAnswerAdapter extends RecyclerView.Adapter<ListAnswerAdapter.AnswerViewHolder> {
    private Answers answers;
    private Offer offer;
    private static final int HEART_FULL = R.drawable.ic_favorite;
    private static final int HEART_EMPTY = R.drawable.ic_favorite_border;

    public static class AnswerViewHolder extends ThreeFieldsViewHolder {

        public AnswerViewHolder(View view) {
            super(view, R.id.user_name, R.id.answer_description, R.id.user_pic);
        }
    }

    /**
     * Constructs a ListAnswerAdapter for the RecyclerView.
     *
     * @param answers the objet containing the list of answers to be displayed
     */
    public ListAnswerAdapter(Answers answers, Offer offer) {
        if (answers == null || answers.getAnswerList() == null || offer == null) {
            throw new IllegalArgumentException();
        }
        this.answers = answers;
        this.offer = offer;
    }

    /**
     * Sets the answers field.
     * @param answers the new answers
     */
    public void setAnswers(Answers answers) {
        this.answers = answers;
        notifyDataSetChanged();
    }

    /**
     * Gets the answers.
     * @return the answers
     */
    public Answers getAnswers() {
        return answers;
    }


    @Override
    public AnswerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reactions_others, parent, false);
        return new AnswerViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final AnswerViewHolder holder, int position) {
        final Answer answer = answers.getAnswerList().get(position);

        // get the user data from database
        ValueListener<User> vlUser = new ValueListener<User>() {
            @Override
            public void onDataChange(User value) {
                TextView userName = (TextView) holder.getFieldOne();
                userName.setText(value.getUserName());
                userName.setContentDescription(
                        "username"+Integer.toString(holder.getAdapterPosition()));
                ImageView userPic = (ImageView) holder.getFieldThree();
                Picasso.with(userPic.getContext())
                        .load(Uri.parse(value.getPhoto()))
                        .placeholder(R.drawable.gender_neutral_user1)
                        .fit().into(userPic);
                userPic.setContentDescription("pic"+Integer.toString(holder.getAdapterPosition()));
            }

            @Override
            public void onCancelled(DbError error) {
                Log.d(error.toString(), "Unable to load user from database");
            }
        };
        DatabaseUser.getUser(vlUser, answer.getUserId());

        TextView description = (TextView) holder.getFieldTwo();
        description.setText(answer.getDescription());
        description.setContentDescription("description"+Integer.toString(position));

        setupFavorite(holder, position);

    }

    private void setupFavorite(final AnswerViewHolder holder, int position) {
        ImageButton favButton = holder.getContainer().findViewById(R.id.favorite);
        favButton.setContentDescription("fav"+Integer.toString(position));
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getLayoutPosition();
                int newPos = answers.getFavoritePos() != pos ? pos : -1;
                answers.setFavoritePos(newPos);
                Database.getInstance().write(Database.ANSWERS_PATH, offer.getUuid(), answers);
                notifyDataSetChanged();
            }
        });

        boolean isAuthor = offer.getUserId().equals(Config.getUser().getUserId());
        if (!isAuthor) {
            favButton.setClickable(false);
        }
        if (answers.getFavoritePos() == position) {
            favButton.setImageResource(HEART_FULL);
            favButton.setTag(HEART_FULL);
        } else if (isAuthor) {
            favButton.setImageResource(HEART_EMPTY);
            favButton.setTag(HEART_EMPTY);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return answers.getAnswerList().size();
    }
}