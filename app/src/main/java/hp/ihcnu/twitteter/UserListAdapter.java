package hp.ihcnu.twitteter;

import twitter4j.User;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.loopj.android.image.SmartImageView;

public class UserListAdapter extends ArrayAdapter<twitter4j.User> {

	private LayoutInflater mInflater;
	
	public UserListAdapter(Context c, int resourse){
		super(c, resourse);
		mInflater = LayoutInflater.from(c);
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent){
		if(convertView == null){
			convertView = mInflater.inflate(R.layout.user_row, null);
		}
		
		User user = getItem(pos);
		
		SmartImageView icon = (SmartImageView)convertView.findViewById(R.id.icon);
		icon.setImageUrl(user.getProfileImageURL());
		
		TextView name = (TextView)convertView.findViewById(R.id.name);
		name.setText(user.getName());

		TextView screenName = (TextView)convertView.findViewById(R.id.screen_name);
		screenName.setText("@" + user.getScreenName());
		
		TextView description = (TextView)convertView.findViewById(R.id.description);
		description.setText(user.getDescription());
		
		return convertView;
	}
}
