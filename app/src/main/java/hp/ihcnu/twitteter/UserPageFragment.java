package hp.ihcnu.twitteter;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class UserPageFragment extends Fragment{

	int pNum;
	User user;
	private Context c;
	private Twitter mTwitter;
	private TweetAdapter mTweetAdapter;
	private TweetAdapter mFavoriteAdapter;
	
	public UserPageFragment(Context context, User user, int pos){
		c = context;
		this.user = user;
		pNum = pos;
//		pNum = getArguments() != null ? getArguments().getInt("pageIndex") : 1;
		mTwitter = TwitterUtils.getTwitterInstance(c);
		mTweetAdapter = new TweetAdapter(c, 0);
		mFavoriteAdapter = new TweetAdapter(c, 0);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
//		Log.d("test", "onCreate");
		switch(pNum){
		case 0:
			break;
		case 1:
			getTweet(user);
			break;
		case 2:
			getFavorite(user);
			break;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View convertView = null;
		Log.d("test", "onCreateView");

		switch(pNum){
		case 0:
//			Log.d("test", "onCreateView " + pNum);
			convertView = inflater.inflate(R.layout.userpage_bio, null);
			
			TextView bio = (TextView)convertView.findViewById(R.id.bio);
			bio.setText(user.getDescription());
			
			TextView web = (TextView)convertView.findViewById(R.id.website);
			web.setText(user.getURL());
			
			TextView location = (TextView)convertView.findViewById(R.id.location);
			location.setText(user.getLocation());
			break;
		case 1:
//			Log.d("test", "onCreateView " + pNum);
			convertView = inflater.inflate(R.layout.userpage_tweet, null);
		
			ListView tweetList = (ListView)convertView.findViewById(R.id.tweetlist);
			tweetList.setAdapter(mTweetAdapter);
			break;
		case 2:
//			Log.d("test", "onCreateView " + pNum);
			convertView = inflater.inflate(R.layout.userpage_favorite, null);
			
			ListView favoriteList = (ListView)convertView.findViewById(R.id.favoritelist);
			favoriteList.setAdapter(mFavoriteAdapter);
			break;
		case 3:
//			Log.d("test", "onCreateView " + pNum);
			convertView = inflater.inflate(R.layout.userpage_following, null);

//			ListView followinglist = (ListView)convertView.findViewById(R.id.followinglist);
//			followinglist.setAdapter(mUserListAdapter);
//			getFriend(user);
			break;
		default:
//			Log.d("test", "onCreateView " + pNum);
			convertView = inflater.inflate(R.layout.userpage_follower, null);
			break;
		}

		return convertView;
	}
	
	private void getTweet(User user){
		
		AsyncTask<twitter4j.User, Void, Boolean> task = new AsyncTask<twitter4j.User, Void, Boolean>(){
			ResponseList<twitter4j.Status> tweetList;

			@Override
			protected Boolean doInBackground(twitter4j.User... params) {
				try {
					tweetList = mTwitter.getUserTimeline(params[0].getId());
					if(tweetList != null){
						return true;
					} else {
						return false;
					}
				} catch (TwitterException e) {
					Log.e("test", "getTweet", e);
					return false;
				}
			}
			
			@Override
			protected void onPostExecute(Boolean res){
				if(res == true){
					for(twitter4j.Status status : tweetList){
						mTweetAdapter.add(status);
					}
					Toast.makeText(c, "Get tweet.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(c, "Failed to get tweet.", Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute(user);
	}

	private void getFavorite(User user){
		
		AsyncTask<twitter4j.User, Void, Boolean> task = new AsyncTask<twitter4j.User, Void, Boolean>(){
			ResponseList<twitter4j.Status> tweetList;

			@Override
			protected Boolean doInBackground(twitter4j.User... params) {
				try {
					tweetList = mTwitter.getFavorites(params[0].getId());
					if(tweetList != null){
						return true;
					} else {
						return false;
					}
				} catch (TwitterException e) {
					Log.e("test", "getFavorite", e);
					return false;
				}
			}
			
			@Override
			protected void onPostExecute(Boolean res){
				if(res == true){
					for(twitter4j.Status status : tweetList){
						mFavoriteAdapter.add(status);
					}
					Toast.makeText(c, "Get Favorite.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(c, "Failed to get favorite.", Toast.LENGTH_SHORT).show();
				}
			}
		};
		task.execute(user);
	}
}
