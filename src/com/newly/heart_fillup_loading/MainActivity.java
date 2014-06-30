package com.newly.heart_fillup_loading;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.newly.heart_fillup_loading.customview.HeartLoadingView;
import com.newly.heart_fillup_loading.customview.HeartLoadingView.onLoadingSucessListener;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final HeartLoadingView view = (HeartLoadingView) findViewById(R.id.heart_loading);
		
		view.moveToProgress(80);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				view.moveToProgress(100);
				view.moveToFull();
			}
		});
		
		view.setOnLoadingSucessListener(new onLoadingSucessListener() {

			@Override
			public void onLoadingSucess(HeartLoadingView loadingView) {
				loadingView.setVisibility(View.GONE);
				Toast.makeText(MainActivity.this, "loading sucess！", Toast.LENGTH_SHORT).show();
			}
		});
		
	}
	
	
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
