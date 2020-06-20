package com.ytdownloader;

import com.ytdownloader.VideoDownloadService.LocalBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NotificationFloatingActivity extends Activity 
{
	private VideoDownloadService videoDownloadService;
    private boolean mBound = false;
    private String dataString = "";
    private int downloadJobId;
    private String downloadTitle = "";
    private boolean downloadInProgress = true;
    private TextView tvDownloadTitle;
    private Button btnCancelDownload;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_floating);
		
		downloadInProgress = true;
		
		tvDownloadTitle = (TextView) findViewById(R.id.tvDownloadTitle);
		btnCancelDownload = (Button) findViewById(R.id.btnCancelDownload);
		btnCancelDownload.setEnabled(downloadInProgress);
		btnCancelDownload.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				videoDownloadService.removeDownloadJob(downloadJobId);
				btnCancelDownload.setEnabled(false);
				NotificationFloatingActivity.this.finish();
			}
		});
	}
	
    @Override
    protected void onStart() 
    {
        super.onStart();
        
        Intent intent = new Intent(this, VideoDownloadService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        dataString = getIntent().getDataString();
        downloadJobId = Integer.parseInt(dataString.split("\\|\\|\\|")[0]);
		downloadTitle = dataString.split("\\|\\|\\|")[1];
		tvDownloadTitle.setText(downloadTitle);
		
		try
		{
			if (dataString.split("\\|\\|\\|")[2].equals("downloadComplete"))
			{
				downloadInProgress = false;
				btnCancelDownload.setEnabled(downloadInProgress);
			}
		}
		catch (Exception e)
		{
			downloadInProgress = true;
			btnCancelDownload.setEnabled(downloadInProgress);
		}
    }

    @Override
    protected void onStop() 
    {
        super.onStop();
        
        if (mBound) 
        {
            unbindService(mConnection);
            mBound = false;
        }
    }

	
	@Override
	protected void onResume()
	{
		dataString = getIntent().getDataString();
		downloadJobId = Integer.parseInt(dataString.split("\\|\\|\\|")[0]);
		downloadTitle = dataString.split("\\|\\|\\|")[1];
		tvDownloadTitle.setText(downloadTitle);
		
		try
		{
			if (dataString.split("\\|\\|\\|")[2].equals("downloadComplete"))
			{
				downloadInProgress = false;
				btnCancelDownload.setEnabled(downloadInProgress);
			}
		}
		catch (Exception e)
		{
			downloadInProgress = true;
			btnCancelDownload.setEnabled(downloadInProgress);
		}
		
		super.onResume();
	}
	
	@Override
	protected void onNewIntent(Intent intent)
	{
		setIntent(intent);
	}
	
    private ServiceConnection mConnection = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) 
        {
            LocalBinder binder = (LocalBinder) service;
            videoDownloadService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) 
        {
            mBound = false;
        }
    };

}
