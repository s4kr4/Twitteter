package hp.ihcnu.twitteter;

import twitter4j.User;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class UserPageFragmentAdapter extends FragmentPagerAdapter {

	private User user;
	private Context c;

	public UserPageFragmentAdapter(FragmentManager fm, Context context, User user) {
		super(fm);

		c = context;
		this.user = user;
	}

	@Override
	public Fragment getItem(int pos) {
		UserPageFragment fragment = new UserPageFragment(c, user, pos);
		return fragment;
	}

	@Override
	public int getCount() {
		return 5;
	}

}
