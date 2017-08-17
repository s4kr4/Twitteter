package com.s4kr4.twitteter;

import twitter4j.Status;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class TweetAdapter extends ArrayAdapter<twitter4j.Status>{

    private LayoutInflater mInflater;
    private static final float BUTTON_WIDTH_DP = 80f;
    private int margin;
    private String className;

    public TweetAdapter(Context c, int resource) {
        super(c, resource);
        mInflater = LayoutInflater.from(c);
        try {
            PackageInfo pInfo = c.getPackageManager().getPackageInfo(c.getPackageName(), c.getPackageManager().GET_ACTIVITIES);
            className = pInfo.activities[0].name;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }


        WindowManager wm = (WindowManager)c.getSystemService(Context.WINDOW_SERVICE);
        float density = c.getResources().getDisplayMetrics().density;
        int btnWidthPX = (int)(BUTTON_WIDTH_DP * density + 0.5f);
        Display dp = wm.getDefaultDisplay();
        Point p = new Point();
        dp.getSize(p);
        margin = p.x - btnWidthPX;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        if(convertView == null){
            convertView = mInflater.inflate(R.layout.custom_row, null);
        }

        final Status st = getItem(position);
        final TweetViewPager mViewPager = (TweetViewPager)convertView.findViewById(R.id.viewpager);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int pos) {
                switch(pos){
                case 0:
                    TwitterUtils.retweet(st.getId());
                    ImageView retweetIcon = (ImageView)mViewPager.findViewById(R.id.retweet_icon);
                    retweetIcon.setVisibility(ImageView.VISIBLE);
                    break;
                case 1:
                    break;
                case 2:
                    TwitterUtils.favorite(st.getId());
                    ImageView favoriteIcon = (ImageView)mViewPager.findViewById(R.id.favorite_icon);
                    favoriteIcon.setVisibility(ImageView.VISIBLE);
                    break;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch(state){
                case ViewPager.SCROLL_STATE_IDLE:
                    mViewPager.setCurrentItem(1, true);
                    break;
                }
            }
        });

        mViewPager.setPageMargin(-margin);
        mViewPager.setAdapter(new TweetPagerAdapter(getContext(), st));
        mViewPager.setCurrentItem(1, true);

        return convertView;
    }
}
