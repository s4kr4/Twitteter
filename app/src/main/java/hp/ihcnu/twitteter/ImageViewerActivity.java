package hp.ihcnu.twitteter;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.loopj.android.image.SmartImageView;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends Activity{

	private SmartImageView imageView;
	private Animation inAnimation;
	private Animation outAnimation;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_imageviewer);
		
		inAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_anim);
		outAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out_anim);
		
		imageView = (SmartImageView)findViewById(R.id.imageview);
		imageView.setAnimation(inAnimation);
		
		String url = getIntent().getDataString();
		
		Picasso.with(getApplicationContext())
			.load(url)
			.into(imageView);
	}
}
