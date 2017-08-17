package com.s4kr4.twitteter;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twitter4j.Status;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;

/**
 * Context menu that showed when tapping tweet
 */
@EActivity(R.layout.activity_contextmenu)
public class ContextMenuActivity extends Activity {

    private Status status;
    private Pattern viaPattern = Pattern.compile("</?a.*?>");
    private Set<String> muteList;

    @ViewById(R.id.menu)
    View menu;

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

    @AnimationRes(R.anim.fade_in_anim)
    Animation fadeIn;

    @AfterViews
    void initViews() {
        status = (Status)getIntent().getSerializableExtra("STATUS");

        name.setText(status.getUser().getName());

        screenName.setText("@" + status.getUser().getScreenName());

        text.setText(status.getText());

        Matcher m = viaPattern.matcher(status.getSource().toString());
        via.setText("via " + m.replaceAll(""));

        time.setText(status.getCreatedAt().toString());

        tweetId.setText(Long.toString(status.getId()));

        menu.startAnimation(fadeIn);

        Picasso.with(getContext())
                .load(status.getUser().getProfileImageURL())
                .into(icon);
    }

    @Click({R.id.reply_btn})
    void replyButtonClicked() {
        ReplyActivity_.intent(getContext()).extra("STATUS", status).start();
        // setResult(RESULT_OK, intent);
        finish();
    }

    @Click({R.id.DM_btn})
    void dmButtonClicked() {
        finish();
    }

    @Click({R.id.retweet_btn})
    void retweetButtonClicked() {
        TwitterUtils.retweet(status.getId());
        finish();
    }

    @Click({R.id.favorite_btn})
    void favoriteButtonClicked() {
        TwitterUtils.favorite(status.getId());
        finish();
    }

    @Click({R.id.userpage_btn})
    void userpageButtonClicked() {
        Intent intent = new Intent(getContext(), UserPageActivity.class);
        intent.putExtra("USER", status.getUser());
        startActivity(intent);
        finish();
    }

    @Click({R.id.mute_btn})
    void muteButtonClicked() {
        SharedPreferences pref = getContext().getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        Editor editor = pref.edit();
        muteList = new HashSet<String>();
        muteList.add(status.getUser().getScreenName());
        editor.putStringSet("mute_list", muteList);
        if(editor.commit()){
            Toast.makeText(getContext(), "save mutelist", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private Context getContext(){
        return this;
    }
}
