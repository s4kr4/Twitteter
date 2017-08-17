package com.s4kr4.twitteter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

@EActivity(R.layout.activity_rep)
public class ReplyActivity extends Activity{

    private Status status;
    private Pattern viaPattern = Pattern.compile("</?a.*?>");
    private Twitter mTwitter;
    private final static int GET_IMAGE_RESULT = 0;

    private Uri imageUri = null;

    @ViewById(R.id.reply)
    View reply;

    @ViewById(R.id.name)
    TextView name;

    @ViewById(R.id.screen_name)
    TextView screenName;

    @ViewById(R.id.text)
    TextView text;

    @ViewById(R.id.via)
    TextView via;

    @ViewById(R.id.time)
    TextView time;

    @ViewById(R.id.tweet_id)
    TextView tweetId;

    @ViewById(R.id.icon)
    ImageView icon;

    @ViewById(R.id.replyText)
    EditText replyText;

    @ViewById(R.id.sendBtn)
    Button sendButton;

    @AnimationRes(R.anim.fade_in_anim)
    Animation fadeIn;

    @AfterViews
    void initView() {
        mTwitter = TwitterUtils.getTwitterInstance(this);
        status = (Status)getIntent().getSerializableExtra("STATUS");

        name.setText(status.getUser().getName());

        screenName.setText("@" + status.getUser().getScreenName());

        text.setText(status.getText());

        Matcher m = viaPattern.matcher(status.getSource().toString());
        via.setText("via " + m.replaceAll(""));

        time.setText(status.getCreatedAt().toString());

        tweetId.setText(Long.toString(status.getId()));

        Picasso.with(getContext())
                .load(status.getUser().getProfileImageURL())
                .into(icon);

        reply.startAnimation(fadeIn);

        replyText.setText("@" + status.getUser().getScreenName() + " ");
        replyText.setSelection(replyText.getText().length());
    }

    @Click(R.id.sendBtn)
    void sendButtonClicked() {
        sendReply(replyText.getText().toString());
        finish();
    }

    @LongClick(R.id.sendBtn)
    void sendButtonLongClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GET_IMAGE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
        case GET_IMAGE_RESULT:
            if(resultCode == Activity.RESULT_OK){
                if(data != null){
                    //get image path
                    ContentResolver cr = getContentResolver();
                    String[] columns = {
                            MediaStore.Images.Media.DATA
                    };
                    Cursor c = cr.query(data.getData(), columns, null, null, null);
                    int column_index = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    c.moveToFirst();
                    String path = c.getString(column_index);
                    imageUri = Uri.parse(path);
                    Toast.makeText(this, "Set image.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
        }
    }

    private void sendReply(String tweet){
        AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>(){

            @Override
            protected Boolean doInBackground(String... params){
                try{
                    StatusUpdate statusUpdate = new StatusUpdate(params[0]);
                    if(imageUri != null){
                        statusUpdate.setMedia(new File(imageUri.toString()));
                        statusUpdate.setInReplyToStatusId(status.getId());
                        mTwitter.updateStatus(statusUpdate);
                    } else {
                        mTwitter.updateStatus(statusUpdate.inReplyToStatusId(status.getId()));
                    }
                    return true;
                } catch(TwitterException e) {
                    Log.e("test", "tweet", e);
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean res){
                if(res){
                    Toast.makeText(getContext(), "Send reply :)", Toast.LENGTH_SHORT).show();
                    imageUri = null;
                } else {
                    Toast.makeText(getContext(), "Failed to send reply X(", Toast.LENGTH_SHORT).show();
                    imageUri = null;
                }
            }
        };
        task.execute(tweet);
    }

    private Context getContext(){
        return this;
    }
}
