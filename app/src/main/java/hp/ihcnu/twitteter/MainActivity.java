package hp.ihcnu.twitteter;

import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class MainActivity extends ListActivity {

	private TweetAdapter mAdapter;
	private Twitter mTwitter;
	private EditText mInputText;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		if(!TwitterUtils.hasAccessToken(this)){
			Intent intent = new Intent(this, TwitterOAuthActivity.class);
			startActivity(intent);
			finish();
		} else {
			Intent intent = new Intent(this, UserStreamActivity.class);
			startActivity(intent);
			finish();
//			mAdapter = new TweetAdapter(this, 0);
//			setListAdapter(mAdapter);
//			
//			mTwitter = TwitterUtils.getTwitterInstance(this);
//			reloadTimeLine();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
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
					mAdapter.clear();
					for(twitter4j.Status status : result){
						mAdapter.add(status);
					}
					getListView().setSelection(0);
				} else {
					showToast("Failed to get Timeline");
				}
			}
		};
		task.execute();
	}
	
	private void tweet(){
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>(){
			
			@Override
			protected Boolean doInBackground(String... params){
				try{
					mTwitter.updateStatus(params[0]);
					return true;
				} catch(TwitterException e) {
					e.printStackTrace();
					return false;
				}
			}
			
			@Override
			protected void onPostExecute(Boolean res){
				if(res){
					showToast("Send tweet :)");
					finish();
				} else {
					showToast("Failed to send tweet X(");
				}
			}
		};
		task.execute(mInputText.getText().toString());
	}
	
	private void showToast(String str){
		Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
	}

	private class TweetAdapter extends ArrayAdapter<twitter4j.Status>{
		
		private LayoutInflater mInflater;
		
		public TweetAdapter(Context c, int resource) {
			super(c, android.R.layout.simple_list_item_1);
			mInflater = (LayoutInflater)c.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null){
				convertView = mInflater.inflate(R.layout.tweet_layout, null);
			}
			Status item = getItem(position);
			TextView name = (TextView)convertView.findViewById(R.id.name);
			name.setText(item.getUser().getName());
			TextView screenName = (TextView)convertView.findViewById(R.id.screen_name);
			screenName.setText("@" + item.getUser().getScreenName());
			TextView text = (TextView)convertView.findViewById(R.id.text);
			text.setText(item.getText());
			ImageView icon = (ImageView)convertView.findViewById(R.id.icon);
			Picasso.with(getContext())
					.load(item.getUser()
					.getProfileImageURL());

			return convertView;
		}
	}
}
