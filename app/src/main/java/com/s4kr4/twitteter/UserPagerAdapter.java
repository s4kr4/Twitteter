package com.s4kr4.twitteter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class UserPagerAdapter extends PagerAdapter{

    private LayoutInflater mInflater;
    private User user;

    private Twitter mTwitter;
    private TweetAdapter mTweetAdapter;
    private TweetAdapter mFavoriteAdapter;
    private UserListAdapter mUserListAdapter;
    private Context c;

    public UserPagerAdapter(Context context, User user) {
        super();
        c = context;
        mInflater = LayoutInflater.from(context);
        this.user = user;
        mTwitter = TwitterUtils.getTwitterInstance(context);
        mTweetAdapter = new TweetAdapter(context, 0);
        mFavoriteAdapter = new TweetAdapter(context, 0);
        mUserListAdapter = new UserListAdapter(context, 0);

//        View tweetView = (View)mInflater.inflate(R.layout.userpage_tweet, null);
//        ListView tweetlist = (ListView)tweetView.findViewById(R.id.tweetlist);
//        tweetlist.setAdapter(mTweetAdapter);
//
//        View retweetView = (View)mInflater.inflate(R.layout.userpage_favorite, null);
//        ListView favoritelist = (ListView)retweetView.findViewById(R.id.favoritelist);
//        favoritelist.setAdapter(mFavoriteAdapter);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int pos){
        View convertView = null;

        switch(pos){
        case 0:
            convertView = (View)mInflater.inflate(R.layout.userpage_bio, null);

            TextView bio = (TextView)convertView.findViewById(R.id.bio);
            bio.setText(user.getDescription());

            TextView web = (TextView)convertView.findViewById(R.id.website);
            web.setText(user.getURL());

            TextView location = (TextView)convertView.findViewById(R.id.location);
            location.setText(user.getLocation());
            container.addView(convertView);
            break;
        case 1:
            Log.d("test", "instantiateItem");
            if(mTweetAdapter.getCount() == 0) {
                convertView = (View)mInflater.inflate(R.layout.userpage_tweet, null);

                ListView tweetlist = (ListView)convertView.findViewById(R.id.tweetlist);
                tweetlist.setAdapter(mTweetAdapter);
                getTweet(user);
                container.addView(convertView);
            }
            break;
        case 2:
            if(mFavoriteAdapter.getCount() == 0) {
                convertView = (View)mInflater.inflate(R.layout.userpage_favorite, null);

                ListView favoritelist = (ListView)convertView.findViewById(R.id.favoritelist);
                favoritelist.setAdapter(mFavoriteAdapter);
                getFavorite(user);
                container.addView(convertView);
            }
            break;
        case 3:
            convertView = (View)mInflater.inflate(R.layout.userpage_following, null);

            ListView followinglist = (ListView)convertView.findViewById(R.id.followinglist);
            followinglist.setAdapter(mUserListAdapter);
            getFriend(user);
            container.addView(convertView);
            break;
        default:
            convertView = (View)mInflater.inflate(R.layout.userpage_follower, null);
            container.addView(convertView);
            break;
        }


        return convertView;
    }

    @Override
    public void destroyItem(ViewGroup container, int pos, Object obj){
        ((ViewPager)container).removeView((View)obj);
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public boolean isViewFromObject(View view, Object obj) {
        return view.equals(obj);
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

    private void getFriend(User user){

    }
}
