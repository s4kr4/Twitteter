package hp.ihcnu.twitteter;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class UserPageActivity extends FragmentActivity {

	private Twitter mTwitter;
	private User user;
	private Animation inAnimation;
	private View userpageView;
	private ViewPager bioPager;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userpage);
		
		inAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_anim);
		userpageView = (View)findViewById(R.id.userpage);
		userpageView.setAnimation(inAnimation);
		
		bioPager = (ViewPager)findViewById(R.id.bio_pager);
		
		mTwitter = TwitterUtils.getTwitterInstance(this);
		
		this.user = (User)getIntent().getSerializableExtra("USER");
		
		ImageView icon = (ImageView)findViewById(R.id.icon);
		Picasso.with(getApplicationContext())
				.load(user.getProfileImageURL().replaceAll("_normal", ""))
				.into(icon);

		ImageView header = (ImageView)findViewById(R.id.header);
        Picasso.with(getBaseContext())
				.load(user.getProfileImageURL())
				.into(header);

		TextView name = (TextView)findViewById(R.id.username);
		name.setText(user.getName() + " @" + user.getScreenName());
		
		TextView bio_btn = (TextView)findViewById(R.id.bio_btn);
		bio_btn.setOnClickListener(bioButtonClickListener);
		
		TextView tweet_btn = (TextView)findViewById(R.id.tweet_btn);
		tweet_btn.setText("Tweet\n" + user.getStatusesCount());
		tweet_btn.setOnClickListener(tweetButtonClickListener);

		TextView fav_btn = (TextView)findViewById(R.id.fav_btn);
		fav_btn.setText("Favorite\n" + user.getFavouritesCount());
		fav_btn.setOnClickListener(favoriteButtonClickListener);
		
		TextView following_btn = (TextView)findViewById(R.id.following_btn);
		following_btn.setText("Follow\n" + user.getFriendsCount());
		following_btn.setOnClickListener(followingButtonClickListener);
		
		TextView follower_btn = (TextView)findViewById(R.id.follower_btn);
		follower_btn.setText("Follower\n" + user.getFollowersCount());
		follower_btn.setOnClickListener(followerButtonClickListener);
		
		ViewPager bioPager = (ViewPager)findViewById(R.id.bio_pager);
		bioPager.setAdapter(new UserPageFragmentAdapter(getSupportFragmentManager(), this, user));

	}
	
	public boolean follow(User user){
		User returnUser = null;
		Relationship r = null;

		try {
			returnUser = mTwitter.createFriendship(user.getId());
			r = mTwitter.showFriendship(TwitterUtils.loadMyId(this), returnUser.getId());
		} catch (TwitterException e) {
			e.printStackTrace();
		}

		return r.isSourceFollowingTarget();
	}
	
	OnClickListener bioButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			bioPager.setCurrentItem(0, true);
		}
	};
	
	OnClickListener tweetButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			bioPager.setCurrentItem(1, true);
		}
	};
	
	OnClickListener favoriteButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			bioPager.setCurrentItem(2, true);
		}
	};
	
	OnClickListener followingButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			bioPager.setCurrentItem(3, true);
		}
	};
	
	OnClickListener followerButtonClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			bioPager.setCurrentItem(4, true);
		}
	};
}
