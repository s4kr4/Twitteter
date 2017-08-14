package hp.ihcnu.twitteter;

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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class ContextMenuActivity extends Activity {

	private Status status;
	private Pattern getViaPattern = Pattern.compile("</?a.*?>");
	private Set<String> muteList;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contextmenu);
		
		Animation inAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_anim);
		View menu = (View)findViewById(R.id.menu);
		menu.setAnimation(inAnimation);

		status = (Status)getIntent().getSerializableExtra("STATUS");

		TextView name = (TextView)findViewById(R.id.name);
		name.setText(status.getUser().getName());

		TextView screenName = (TextView)findViewById(R.id.screen_name);
		screenName.setText("@" + status.getUser().getScreenName());
		
		TextView text = (TextView)findViewById(R.id.text);
		text.setText(status.getText());
		
		TextView via = (TextView)findViewById(R.id.via);
		Matcher m = getViaPattern.matcher(status.getSource().toString());
		via.setText("via " + m.replaceAll(""));
		
		TextView time = (TextView)findViewById(R.id.time);
		time.setText(status.getCreatedAt().toString());
		
		TextView id = (TextView)findViewById(R.id.tweetId);
		id.setText(Long.toString(status.getId()));
		
		ImageView icon = (ImageView)findViewById(R.id.icon);
		Picasso.with(getContext())
		.load(status.getUser().getProfileImageURL())
		.into(icon);
		
//		if(status.getMediaEntities().length > 0){
//			ImageView img1 = (ImageView)tweetView.findViewById(R.id.img1);
//			img1.setVisibility(ImageView.VISIBLE);
//			img1.setTag(status.getMediaEntities()[0].getMediaURL());
//			GetImageTask imgtask = new GetImageTask(img1);
//			imgtask.execute(status.getMediaEntities()[0].getMediaURL());
//		}

		TextView replyButton = (TextView)findViewById(R.id.reply_btn);
		replyButton.setOnClickListener(replyClickListener);
		TextView dmButton = (TextView)findViewById(R.id.DM_btn);
		dmButton.setOnClickListener(dmClickListener);
		TextView retweetButton = (TextView)findViewById(R.id.retweet_btn);
		retweetButton.setOnClickListener(retweetClickListener);
		TextView favoriteButton = (TextView)findViewById(R.id.favorite_btn);
		favoriteButton.setOnClickListener(favoriteClickListener);
		TextView userpageButton = (TextView)findViewById(R.id.userpage_btn);
		userpageButton.setOnClickListener(userpageClickListener);
		TextView muteButton = (TextView)findViewById(R.id.mute_btn);
		muteButton.setOnClickListener(muteClickListener);
	}
	
	OnClickListener replyClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getContext(), ReplyActivity.class);
			intent.putExtra("STATUS", status);
			getContext().startActivity(intent);
//			setResult(RESULT_OK, intent);
			finish();
		}
	};

	OnClickListener dmClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			finish();
		}
	};

	OnClickListener retweetClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TwitterUtils.retweet(status.getId());
			finish();
		}
	};

	OnClickListener favoriteClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TwitterUtils.favorite(status.getId());
			finish();
		}
	};

	OnClickListener userpageClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getContext(), UserPageActivity.class);
			intent.putExtra("USER", status.getUser());
			startActivity(intent);
			finish();
		}
	};

	OnClickListener muteClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
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
	};

	private Context getContext(){
		return this;
	}
}
