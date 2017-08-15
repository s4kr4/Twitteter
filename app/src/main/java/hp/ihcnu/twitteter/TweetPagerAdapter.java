package hp.ihcnu.twitteter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.squareup.picasso.Picasso;

import twitter4j.Status;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class TweetPagerAdapter extends PagerAdapter {
	private Status status;
	private LayoutInflater mInflater;
	private Pattern getViaPattern = Pattern.compile("</?a.*?>");
	private Context c;
	private static final int REQUEST_ACTIVITY = 1;
	
	public TweetPagerAdapter(Context c, final Status status){
			super();
			this.status = status;
			this.c = c;
			mInflater = LayoutInflater.from(c);
//			Log.d("test", "TweetPagerAdapter constructer");
	}

	@Override
	public Object instantiateItem(ViewGroup container, int pos){
		final TweetViewPager cntnr = (TweetViewPager)container;
		final ViewGroup convertView;

		switch(pos){
		case 0:
			convertView = (ViewGroup)mInflater.inflate(R.layout.left_layout, null);
			break;
		case 1:
//			Log.d("test", "instantiateItem");
			convertView = (ViewGroup)mInflater.inflate(R.layout.tweet_layout, null);
			if(status.getInReplyToUserId() != -1) convertView.setBackgroundColor(Color.rgb(0, 0, 80));

			if(status.isRetweet()){
				if(status.getUser().getId() == TwitterUtils.loadMyId(c)){
					ImageView retweetIcon = (ImageView)convertView.findViewById(R.id.retweet_icon);
					retweetIcon.setVisibility(ImageView.VISIBLE);
				}
				
				ImageView srcIcon = (ImageView)convertView.findViewById(R.id.src_icon);
				srcIcon.setVisibility(ImageView.VISIBLE);
				Picasso.with(c)
					.load(status.getUser().getProfileImageURL().replaceAll("_normal", ""))
					.into(srcIcon);
				
				TextView srcName = (TextView)convertView.findViewById(R.id.src_name);
				srcName.setVisibility(TextView.VISIBLE);
				srcName.setText("Retweeted by " + status.getUser().getName());
				
				TextView srcScreenName = (TextView)convertView.findViewById(R.id.src_screen_name);
				srcScreenName.setVisibility(TextView.VISIBLE);
				srcScreenName.setText("@" + status.getUser().getScreenName());

				status = status.getRetweetedStatus();
				convertView.setBackgroundColor(Color.rgb(80, 0, 0));
			}
			convertView.setOnClickListener(tweetClickListenter);
			convertView.setOnLongClickListener(tweetLongClickListener);

			TextView name = (TextView)convertView.findViewById(R.id.name);
			name.setText(status.getUser().getName());

			TextView screenName = (TextView)convertView.findViewById(R.id.screen_name);
			screenName.setText("@" + status.getUser().getScreenName());
			
			TextView text = (TextView)convertView.findViewById(R.id.text);
			text.setText(status.getText());
			
			TextView via = (TextView)convertView.findViewById(R.id.via);
			Matcher m = getViaPattern.matcher(status.getSource().toString());
			via.setText("via " + m.replaceAll(""));
			
			TextView time = (TextView)convertView.findViewById(R.id.time);
			time.setText(status.getCreatedAt().toString());
			
			TextView id = (TextView)convertView.findViewById(R.id.tweetId);
			id.setText(Long.toString(status.getId()));
			
			ImageView icon = (ImageView)convertView.findViewById(R.id.icon);
			Picasso.with(c)
				.load(status.getUser().getProfileImageURL().replaceAll("_normal", ""))
				.into(icon);
			icon.setOnClickListener(iconClickListener);
			
//			if(status.isFavorited()) {
//				ImageView favoriteIcon = (ImageView)convertView.findViewById(R.id.favorite_icon);
//				favoriteIcon.setVisibility(ImageView.VISIBLE);
//			}
			
			ImageView[] img = new ImageView[4];
			img[0] = (ImageView)convertView.findViewById(R.id.img1);
			img[1] = (ImageView)convertView.findViewById(R.id.img2);
			img[2] = (ImageView)convertView.findViewById(R.id.img3);
			img[3] = (ImageView)convertView.findViewById(R.id.img4);

			int i = 0;
			for(twitter4j.MediaEntity item : status.getMediaEntities()){
				img[i].setVisibility(ImageView.VISIBLE);
				Picasso.with(c)
					.load(item.getMediaURL())
					.into(img[i]);
				img[i].setOnClickListener(imageClickListener);
				img[i].setTag(item.getMediaURL());
				i++;
			}
			
			if (i >= 3){
				RelativeLayout.LayoutParams layoutParams = new LayoutParams(img[0].getLayoutParams().width, img[0].getLayoutParams().height);
				layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.icon);
				layoutParams.addRule(RelativeLayout.BELOW, R.id.icon);
				img[0].setLayoutParams(layoutParams);
			}
			
			if(status.getUser().isProtected()){
				TextView lock = (TextView)convertView.findViewById(R.id.lock);
				lock.setVisibility(TextView.VISIBLE);
			}
			
			break;
		default:
			convertView = (ViewGroup)mInflater.inflate(R.layout.right_layout, null);
			break;
		}

		cntnr.addView(convertView);
		
		return convertView;
	}
	
	private void showToast(String str){
		Toast.makeText(c, str, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void destroyItem(ViewGroup container, int pos, Object obj){
		((ViewPager)container).removeView((View)obj);
	}
	
	@Override
	public int getCount() {
		return 3;
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		return view.equals(obj);
	}

	OnClickListener tweetClickListenter = new OnClickListener() {
		
		@Override
		public void onClick(View view){
		}
	};
	
	OnLongClickListener tweetLongClickListener = new OnLongClickListener() {
		
		@Override
		public boolean onLongClick(View v) {
			Intent intent = new Intent(c, ContextMenuActivity.class);
			intent.putExtra("STATUS", status);
			c.startActivity(intent);
			return false;
		}
	};
	
	OnClickListener iconClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(c, UserPageActivity.class);
			intent.putExtra("USER", status.getUser());
			c.startActivity(intent);
		}
	};
	
	OnClickListener imageClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Uri uri = Uri.parse(v.getTag().toString());
			Intent intent = new Intent(c, ImageViewerActivity.class);
			intent.setType("image/*");
			intent.setData(uri);
			c.startActivity(intent);
		}
	};
}
