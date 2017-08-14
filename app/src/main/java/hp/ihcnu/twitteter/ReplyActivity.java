package hp.ihcnu.twitteter;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.squareup.picasso.Picasso;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ReplyActivity extends Activity{
	
	private Status status;
	private Pattern getViaPattern = Pattern.compile("</?a.*?>");
	private Twitter mTwitter;
	private final static int GET_IMAGE_RESULT = 0;

	private EditText tweetText;
	private Uri imageUri = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
//		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_rep);
		
		mTwitter = TwitterUtils.getTwitterInstance(this);
		
		Animation inAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_anim);
		View menu = (View)findViewById(R.id.reply);
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

		Button sendButton = (Button)findViewById(R.id.sendBtn);
		sendButton.setOnClickListener(sendButtonClickListener);
		sendButton.setOnLongClickListener(sendButtonLongClickListener);

		tweetText = (EditText)findViewById(R.id.replyText);
		tweetText.setText("@" + status.getUser().getScreenName() + " ");
		tweetText.setSelection(tweetText.getText().length());
	}
	
	OnClickListener sendButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			sendReply(tweetText.getText().toString());
			finish();
		}
	};
	
	OnLongClickListener sendButtonLongClickListener = new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, GET_IMAGE_RESULT);
			return false;
		}
	};
	
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
