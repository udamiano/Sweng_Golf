package ch.epfl.sweng.swenggolf.messaging;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import ch.epfl.sweng.swenggolf.Config;
import ch.epfl.sweng.swenggolf.R;
import ch.epfl.sweng.swenggolf.database.Database;
import ch.epfl.sweng.swenggolf.database.DbError;
import ch.epfl.sweng.swenggolf.database.ValueListener;
import ch.epfl.sweng.swenggolf.offer.Offer;
import ch.epfl.sweng.swenggolf.offer.answer.Answer;
import ch.epfl.sweng.swenggolf.offer.answer.Answers;
import ch.epfl.sweng.swenggolf.profile.User;
import ch.epfl.sweng.swenggolf.statistics.UserStats;
import ch.epfl.sweng.swenggolf.tools.FragmentConverter;

public class MessagingFragment extends FragmentConverter {
    private View inflated;
    private String offerId;
    private User otherUser;
    private MessagesAdapter messagesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        otherUser = bundle.getParcelable(User.USER);
        offerId = bundle.getString(Offer.OFFER);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstance) {
        setToolbar(R.drawable.ic_baseline_arrow_back_24px, otherUser.getUserName());
        inflated = inflater.inflate(R.layout.activity_messaging, container, false);
        setRecyclerView();
        inflated.findViewById(R.id.send_message_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendMessage();
                    }
                });
        return inflated;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Database.getInstance().deafen(Database.ANSWERS_PATH, offerId,
                messagesAdapter.getUpdateListener());
        messagesAdapter.setUpdateListener(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setRecyclerView() {
        RecyclerView mRecyclerView = inflated.findViewById(R.id.messages);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this.getContext());

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        messagesAdapter = new MessagesAdapter(otherUser);
        mRecyclerView.setAdapter(messagesAdapter);
        fetchMessages();

        // Add dividing line
        mRecyclerView.addItemDecoration(
                new DividerItemDecoration(this.getContext(), LinearLayoutManager.VERTICAL));
    }

    private void sendMessage() {
        EditText editText = findViewById(R.id.message_content);
        Answers messages = messagesAdapter.getAnswers();
        String message = editText.getText().toString();
        if (!message.isEmpty()) {
            // we write the whole lists of messages every time because of the listeners
            messages.getAnswerList()
                    .add(new Answer(Config.getUser().getUserId(), message));
            Database.getInstance().write(Database.MESSAGES_PATH, offerId, messages);
            messagesAdapter.setAnswers(messages);
            editText.getText().clear();
            messagesAdapter.notifyDataSetChanged();
            UserStats.updateStat(UserStats.Stats.MESSAGES_SENT, Config.getUser().getUserId(), 1);
            UserStats.updateStat(UserStats.Stats.MESSAGES_RECEIVED, otherUser.getUserId(), 1);
        }
    }

    private void fetchMessages() {
        ValueListener<Answers> messageListener = new ValueListener<Answers>() {
            @Override
            public void onDataChange(Answers value) {
                if (value != null) {
                    messagesAdapter.setAnswers(value);
                }
            }

            @Override
            public void onCancelled(DbError error) {
                if (error != DbError.DATA_DOES_NOT_EXIST) {
                    Toast.makeText(MessagingFragment.this.getContext(),
                            R.string.error_load_messages, Toast.LENGTH_LONG).show();
                }
            }
        };
        Database.getInstance().listen(Database.MESSAGES_PATH, offerId,
                messageListener, Answers.class);
        messagesAdapter.setUpdateListener(messageListener);
    }


}
