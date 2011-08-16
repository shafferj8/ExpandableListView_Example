package com.rocketmbsoft.protectme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

public class ProtectMeSumActivity extends Activity {

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.sum);

		WebView wv = ((WebView) findViewById(R.id.helloWebView));
		
		wv.getSettings().setJavaScriptEnabled(true);
		
		wv.loadUrl("http://rocketmbsoft.blogspot.com/2011/08/protectme-settings.html");
		
		((Button) findViewById(R.id.btn_dismiss_id)).setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				finish();
			}
		});
		
		((Button) findViewById(R.id.btn_about_id)).setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				showAboutDialog(ProtectMeSumActivity.this);
			}
		});

	} 
	
	public static void showAboutDialog(Context c) {
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(Config.ABOUT_DIALOG_TITLE);
        builder.setMessage(Config.ABOUT_DIALOG_MESSAGE);
        builder.setNeutralButton("OK", null);
        builder.show();
	}

}
