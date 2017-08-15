package hp.ihcnu.twitteter;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserStreamAdapter;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class UserStreamActivity extends Activity {

	private Twitter mTwitter;
	private TwitterStream twitterStream;
	private MyUserStreamAdapter mUserStreamAdapter;
	private TweetAdapter mTweetAdapter;
	private AccessToken mAccessToken;
	private static TwitterUtils.EVENT Event;
	private TwitterUtils.TWEET_MODE Mode;

	private Button tweetButton;
	private Button mypageButton;
	private ListView tweetlistView;
	private EditText editText;

	private Handler handler = new Handler();
	
	private String[] muteListString;
	private Set<String> muteList = new HashSet<String>();
	private Uri imageUri = null;
	private final static int GET_IMAGE_RESULT = 0;
	private final static int REPLY_RESULT = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.activity_userstream);
		
		mypageButton = (Button)findViewById(R.id.bio_btn);
		mypageButton.setOnClickListener(mypageButtonClickListener);
		
		tweetButton = (Button)findViewById(R.id.tweet_button);
		tweetButton.setOnClickListener(tweetButtonClickListener);
		tweetButton.setOnLongClickListener(tweetButtonLongClickListener);
		
		editText = (EditText)findViewById(R.id.tweet_text);
		editText.addTextChangedListener(textChangedListener);

		mTweetAdapter = new TweetAdapter(this, 0);
		
		tweetlistView = (ListView)findViewById(R.id.tweet_listview);
		tweetlistView.setAdapter(mTweetAdapter);

		mTwitter = TwitterUtils.getTwitterInstance(this);
		mUserStreamAdapter = new MyUserStreamAdapter(this);
		mAccessToken = TwitterUtils.loadAccessToken(this);
		
		//load mutelist
		SharedPreferences pref = getSharedPreferences("Prefs", MODE_PRIVATE);
		muteList = pref.getStringSet("mute_list", null);
		if(muteList != null){
			Object[] muteListObj = muteList.toArray();
			muteListString = Arrays.asList(muteListObj).toArray(new String[muteListObj.length]);
		}

		ConfigurationBuilder builder = new ConfigurationBuilder();
		{
			builder.setOAuthConsumerKey(BuildConfig.TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(BuildConfig.TWITTER_CONSUMER_SECRET);
			builder.setOAuthAccessToken(mAccessToken.getToken());
			builder.setOAuthAccessTokenSecret(mAccessToken.getTokenSecret());
		}
		
		Configuration conf = builder.build();
		twitterStream = new TwitterStreamFactory(conf).getInstance();

		reloadTimeLine();
		stopUserStreaming();
		startUserStreaming();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		stopUserStreaming();
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		
		AdapterContextMenuInfo adapterInfo = (AdapterContextMenuInfo)menuInfo;
		
		ListView listView = (ListView)v;
		Status status = (Status)listView.getItemAtPosition(adapterInfo.position);
//		Log.v("test", "onCreateContextMenu");
		
//		switch(v.getId()){
//		case R.id.tweet_listview:
//			menu.setHeaderTitle(status.getText());
//			getMenuInflater().inflate(R.menu.context_layout, menu);
//			break;
//		}
	}
	
	//Adapter for getting tweets
	class MyUserStreamAdapter extends UserStreamAdapter{
		public MyUserStreamAdapter(Context c){
			super();
		}
	}
	
	TextWatcher textChangedListener = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			tweetButton.setText("Tweet\n" + (140 - s.length()));
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			
		}
	};

	OnClickListener tweetButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			tweet(editText.getText().toString());
		}
	};

	OnLongClickListener tweetButtonLongClickListener = new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, GET_IMAGE_RESULT);
			return false;
		}
	};
	
	OnClickListener mypageButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			AsyncTask<Long, Void, twitter4j.User> task = new AsyncTask<Long, Void, twitter4j.User>(){

				@Override
				protected twitter4j.User doInBackground(Long... params) {
					return TwitterUtils.getUserInfo(getMyContext(), params[0]);
				}
				
				@Override
				protected void onPostExecute(twitter4j.User myUser){
					if(myUser != null){
						Intent intent = new Intent(getMyContext(), UserPageActivity.class);
						intent.putExtra("USER", myUser);
						startActivity(intent);
					}
				}
			};
			task.execute(TwitterUtils.loadMyId(getMyContext()));
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
					showToast("Set image.");
				}
			}
			break;
		case REPLY_RESULT:
			if(resultCode == Activity.RESULT_OK){
				if(data != null){
				}
			}
			break;
		}
	}

	private Context getMyContext(){
		return this;
	}

	public void startUserStreaming(){

		mUserStreamAdapter = new MyUserStreamAdapter(this){

			//Events while UserStreaming
			@Override
			public void onStatus(Status status) {
				
				final Status st = status;
				
				handler.post(new Runnable(){
					@Override
					public void run(){
						//exclude tweet of user in mutelist
						if(!(muteListString != null && !Arrays.asList(muteListString).contains(st.getUser().getScreenName()))){
							if(st.isRetweet() && st.getRetweetedStatus().getUser().getId() == TwitterUtils.loadMyId(getMyContext())){
									showRetweetToast(st.getUser(), st.getRetweetedStatus());
							} else {
								mTweetAdapter.insert(st, 0);
							}
						}
					}
				});
			}
			
			@Override
			public void onFavorite(User user0, User user1, Status status){
				
				final User usr0 = user0;
				final Status st = status;
				final long mID = TwitterUtils.loadMyId(getApplicationContext());
				
				handler.post(new Runnable(){
					@Override
					public void run(){
						if(usr0.getId() != mID) showFavoriteToast(usr0, st);
					}
				});
			}
			
			@Override
			public void onFollow(User user0, User user1){
				final User usr0 = user0;
				final User usr1 = user1;
				final String mScreenName = TwitterUtils.loadMyScreenName(getApplicationContext());
				
				handler.post(new Runnable(){
					@SuppressWarnings("static-access")
					@Override
					public void run(){
						if(!usr0.getScreenName().equals(mScreenName)) showNotifyToast(usr0, usr1, Event.FOLLOW);
					}
				});
			}

			@Override
			public void onUnfollow(User user0, User user1){
				final User usr0 = user0;
				final User usr1 = user1;
				
				handler.post(new Runnable(){
					@SuppressWarnings("static-access")
					@Override
					public void run(){
						showNotifyToast(usr0, usr1, Event.UNFOLLOW);
					}
				});
			}
		};
		
		showToast("Start stream.");
		twitterStream.addListener(mUserStreamAdapter);
		twitterStream.user();
	}

	public void stopUserStreaming(){
		twitterStream.shutdown();
	}
	
	private void reloadTimeLine(){
		AsyncTask<Void, Void, List<twitter4j.Status>> task =
				new AsyncTask<Void, Void, List<twitter4j.Status>>(){
			@Override
			protected List<twitter4j.Status> doInBackground(Void... params){
				try{
					return mTwitter.getHomeTimeline();
				} catch(TwitterException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<twitter4j.Status> result){
				if(result != null){
					mTweetAdapter.clear();
					for(twitter4j.Status status : result){
						mTweetAdapter.add(status);
					}
				} else {
					showToast("Failed to get Timeline");
				}
			}
		};
		task.execute();
	}
	
	private void tweet(String tweet){
		
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>(){
			
			@Override
			protected Boolean doInBackground(String... params){
				try{
					if(imageUri != null){
						StatusUpdate statusUpdate = new StatusUpdate(params[0]);
						statusUpdate.setMedia(new File(imageUri.toString()));
						mTwitter.updateStatus(statusUpdate);
					} else {
						mTwitter.updateStatus(params[0]);
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
					showToast("Send tweet :)");
					editText.getEditableText().clear();
					imageUri = null;
				} else {
					showToast("Failed to send tweet X(");
					imageUri = null;
				}
			}
		};
		task.execute(tweet);
	}

	private void sendMention(String str, Status status){
		try {
			mTwitter.updateStatus(new StatusUpdate(str).inReplyToStatusId(status.getId()));
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	private void showToast(String str){
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	private void showFavoriteToast(User user, Status status){
		LayoutInflater inflater = getLayoutInflater();
		View toastLayout = inflater.inflate(R.layout.fav_toast_layout, null);

		ImageView image = (ImageView)toastLayout.findViewById(R.id.icon);
		Picasso.with(getMyContext())
				.load(user.getProfileImageURL())
				.into(image);

		TextView name = (TextView)toastLayout.findViewById(R.id.name);
		name.setText(user.getName().toString());
		
		TextView screenName = (TextView)toastLayout.findViewById(R.id.screen_name);
		screenName.setText("@" + user.getScreenName().toString());
		
		TextView text = (TextView)toastLayout.findViewById(R.id.text);
		text.setText(status.getText().toString());
		
		Toast toast = new Toast(this);
		toast.setView(toastLayout);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}

	private void showRetweetToast(User user, Status status){
		LayoutInflater inflater = getLayoutInflater();
		View toastLayout = inflater.inflate(R.layout.retweet_toast_layout, null);

		ImageView image = (ImageView)toastLayout.findViewById(R.id.icon);
        Picasso.with(getMyContext())
				.load(user.getProfileImageURL())
				.into(image);

		TextView name = (TextView)toastLayout.findViewById(R.id.name);
		name.setText(user.getName().toString());
		
		TextView screenName = (TextView)toastLayout.findViewById(R.id.screen_name);
		screenName.setText("@" + user.getScreenName().toString());
		
		TextView text = (TextView)toastLayout.findViewById(R.id.text);
		text.setText(status.getText().toString());
		
		Toast toast = new Toast(this);
		toast.setView(toastLayout);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}
	
	private void showNotifyToast(User user0, User user1, TwitterUtils.EVENT e){
		LayoutInflater inflater = getLayoutInflater();
		View toastLayout = inflater.inflate(R.layout.notify_toast_layout, null);
		TextView type;

		switch(e){
		case FOLLOW:
			type = (TextView)toastLayout.findViewById(R.id.type);
			type.setText("followed by");
			break;
		case UNFOLLOW:
			type = (TextView)toastLayout.findViewById(R.id.type);
			type.setText("unfollowed by");
			break;
		case BLOCK:
			type = (TextView)toastLayout.findViewById(R.id.type);
			type.setText("blocked by");
			break;
		case UNBLOCK:
			type = (TextView)toastLayout.findViewById(R.id.type);
			type.setText("unblocked by");
			break;
		case DIRECT_MESSAGE:
			type = (TextView)toastLayout.findViewById(R.id.type);
			type.setText("DM from");
			break;
		default:
			break;
		}

		ImageView image = (ImageView)toastLayout.findViewById(R.id.icon);
        Picasso.with(getMyContext())
				.load(user0.getProfileImageURL())
				.into(image);
		
		TextView name = (TextView)toastLayout.findViewById(R.id.name);
		name.setText(user0.getName().toString());
		
		TextView screenName = (TextView)toastLayout.findViewById(R.id.screen_name);
		screenName.setText("@" + user0.getScreenName().toString());
		
		Toast toast = new Toast(this);
		toast.setView(toastLayout);
		toast.setGravity(Gravity.TOP, 0, 0);
		toast.show();
	}
}
