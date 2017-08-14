package hp.ihcnu.twitteter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Handler;


public class TwitterUtils {

	private static final String TOKEN = "token";
	private static final String TOKEN_SECRET = "token_secret";
	private static final String PREF_NAME = "twitter_access_token";
	private static final String ID = "id";
	private static final String SCREEN_NAME = "screen_name";
	private static Twitter twitter = null;
	private static Handler handler = new Handler();
	
	public enum EVENT{
		FAVORITE,
		UNFAVORITE,
		RETWEET,
		FOLLOW,
		UNFOLLOW,
		BLOCK,
		UNBLOCK,
		DIRECT_MESSAGE
	}
	
	public enum TWEET_MODE{
		TWEET,
		MENTION
	}
	
	public static Twitter getTwitterInstance(Context context){
		String consumerKey = BuildConfig.TWITTER_CONSUMER_KEY;
		String consumerSecret = BuildConfig.TWITTER_CONSUMER_SECRET;

		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		
		if(hasAccessToken(context)){
			twitter.setOAuthAccessToken(loadAccessToken(context));
		}
		
		return twitter;
	}
	
	public static void storeAccessToken(Context context, AccessToken accessToken){
		SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putString(TOKEN, accessToken.getToken());
		editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());
		editor.commit();
	}
	
	public static AccessToken loadAccessToken(Context context){
		SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		String token = preferences.getString(TOKEN, null);
		String tokenSecret = preferences.getString(TOKEN_SECRET, null);
		
		if(token != null && tokenSecret != null){
			return new AccessToken(token, tokenSecret);
		} else {
			return null;
		}
	}

	public static void storeAccountInfo(Context context, AccessToken accessToken){
//		User user = null;
//		try {
//			user = mTwitter.verifyCredentials();
//		} catch (TwitterException e) {
//			e.printStackTrace();
//		}
		SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Editor editor = preferences.edit();
		editor.putLong(ID, accessToken.getUserId());
		editor.putString(SCREEN_NAME, accessToken.getScreenName());
		editor.commit();
	}
	
	public static Long loadMyId(Context context){
		SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		Long id = preferences.getLong(ID, 0);
		
		if(id != 0){
			return id;
		} else {
			return null;
		}
	}
	
	public static String loadMyScreenName(Context context){
		SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		String screenName = preferences.getString(SCREEN_NAME, null);
		
		if(screenName != null){
			return screenName;
		} else {
			return null;
		}
	}
	
	public static User getUserInfo(Context context, long id){
//		SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		User user = null;
		try {
			user =twitter.showUser(id);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return user;
	}

	public static boolean hasAccessToken(Context context){
		return loadAccessToken(context) != null;
	}
	
	public static String getAccessToken(Context c){
		SharedPreferences preferences = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return preferences.getString(TOKEN, null);
	}

	public static String getAccessTokenSecret(Context c){
		SharedPreferences preferences = c.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return preferences.getString(TOKEN_SECRET, null);
	}
	
	public static void favorite(Long statusId){
		AsyncTask<Long, Void, Boolean> task = new AsyncTask<Long, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Long... params){
				long id = params[0];
				try{
					twitter.createFavorite(id);
					return true;
				} catch(TwitterException e) {
					e.printStackTrace();
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean res){
				super.onPostExecute(res);
			}
		};
		task.execute(statusId);
	}

	public static void retweet(Long statusId){
		AsyncTask<Long, Void, Boolean> task = new AsyncTask<Long, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(Long... params){
				Long statusId = params[0];
				try{
					twitter.retweetStatus(statusId);
					return true;
				} catch(TwitterException e) {
					e.printStackTrace();
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean res){
				super.onPostExecute(res);
			}
		};
		task.execute(statusId);
	}
}
