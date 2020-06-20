package com.ytdownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;

public class VideoDownloadService extends Service 
{
	  private Looper mServiceLooper;
	  private ServiceHandler mServiceHandler;
	  NotificationManager notifyManager;
	  private final IBinder mBinder = new LocalBinder();
	  int currentJobId;

	  private final class ServiceHandler extends Handler
	  {
		  private boolean bStopTask = false;
		  
	      public ServiceHandler(Looper looper) 
	      {
	          super(looper);
	      }
	      
	      public void stopCurrentTask()
	      {
	    	  bStopTask = true;
	      }
	      
	      @Override
	      public void handleMessage(Message msg) 
	      {
	    	  bStopTask = false;
	    	  currentJobId = msg.what;
	    	  File folder = new File(Environment.getExternalStorageDirectory() + "/YTdude");
	    	  File file = null;
	    	  String dataStringArray[] = null;
	    	  String filename = "";
	    	  
	    	  try
	    	  {
	    		  dataStringArray = msg.getData().getString("data").split("\\|\\|\\|");
	    		  filename = dataStringArray[1];
	    		  
	    		  /*
	    		  if(new File(Environment.getExternalStorageDirectory() + "/Music").exists())
	    		  {
	    			  file = new File(new File(Environment.getExternalStorageDirectory() + "/Music"), filename);
	    		  }
	    		  else if (new File(Environment.getExternalStorageDirectory() + "/Musik").exists())
	    		  {
	    			  file = new File(new File(Environment.getExternalStorageDirectory() + "/Musik"), filename);
	    		  }
	    		  else if (new File(Environment.getExternalStorageDirectory() + "/mp3").exists())
	    		  {
	    			  file = new File(new File(Environment.getExternalStorageDirectory() + "/mp3"), filename);
	    		  }
	    		  else
	    		  {
	    			  file = new File(new File(Environment.getExternalStorageDirectory() + "/Youtube_Downloads"), filename);
	    		  }
	    		  */
	    		  
	    		  if (!folder.exists())
	    		  {
	    			  folder.mkdir();
	    		  }
	    		  
	    		  file = new File(folder, filename);
	    	  }
	    	  catch(Exception e)
	    	  {
	    		  e.printStackTrace();
	    	  }
	    	  
	    	  NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
	    	  Intent notificationActivityIntent = new Intent(VideoDownloadService.this, NotificationFloatingActivity.class);
	    	  try
	    	  {
	    		  notificationActivityIntent.setData(Uri.parse(msg.what + "|||" + file.getAbsolutePath()));
	    	  }
	    	  catch(Exception e)
	    	  {
	    		  e.printStackTrace();
	    	  }
		      PendingIntent pendingIntent = PendingIntent.getActivity(VideoDownloadService.this, 0, notificationActivityIntent, 0);
	    	  
		        try
		  		{		          	
		          	boolean downloadDone = false;
		  			
		  			for (int i = 2; i < dataStringArray.length; i++)
		  			{
		  				String signature;
		  				
		  				if (dataStringArray[i].trim().equals("_"))
		  				{
		  					signature = "";
		  				}
		  				else
		  				{
		  					signature = dataStringArray[i];
		  				}
		  				
		  				URL videoUrl = new URL(dataStringArray[0] + signature);
		  				HttpsURLConnection httpConn = (HttpsURLConnection) videoUrl.openConnection();
		  			    if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK)
		  			    {
		  			    	InputStream inputStream = httpConn.getInputStream();
		  			        FileOutputStream outputStream = new FileOutputStream(file.getAbsolutePath());
		  			        int bytesRead = -1;
		  			        int bytesReadTotal = 0;
		  			        byte[] buffer = new byte[httpConn.getContentLength()];
		  			        while ((bytesRead = inputStream.read(buffer)) != -1) 
		  			        {
		  			        	if(bStopTask == true)
		  			        	{
		  			        		outputStream.close();
		  			        		inputStream.close();
		  			        		file.delete();
		  			        		
		  			        		builder.setContentTitle(filename + " - Download")
		  			        		.setContentText("Download canceled")
		  			        		.setSmallIcon(R.drawable.ic_clear)
		  		                    .setProgress(0,0,false)
		  		                    .setContentIntent(null);
		  			        		notifyManager.notify(msg.what, builder.build());
		  			        		
		  			        		downloadDone = true;
		  			        		break;
		  			        	}
		  			        	
		  			        	bytesReadTotal += bytesRead;
		  			        	
		  			        	builder.setContentTitle(filename + " - Download")
				          		.setContentText("Download in progress")
				          		.setSmallIcon(R.drawable.ic_download_white)
		  			        	.setProgress(100, Math.round((float)(bytesReadTotal * 100)/httpConn.getContentLength()), false)
		  			        	.setContentIntent(pendingIntent);
		  			        	notifyManager.notify(msg.what, builder.build());
		  			        	
		  			        	outputStream.write(buffer, 0, bytesRead);
		  			        }
		  			        
		  			        if (bStopTask == false)
		  			        {
			  			        outputStream.close();
			  			        inputStream.close();
			  			        
			  			        MediaScannerWrapper mediaScanner = new MediaScannerWrapper(getApplicationContext(), file.getParentFile().getAbsolutePath() ,"*/*");
			  			        mediaScanner.scan();
			  			        
			  			        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
			  			        
			  			        downloadDone = true;
			  			        
			  			        notificationActivityIntent = new Intent(VideoDownloadService.this, NotificationFloatingActivity.class);
			  			        notificationActivityIntent.setData(Uri.parse(msg.what + "|||" + file.getAbsolutePath() + "|||" + "downloadComplete"));
			  			        pendingIntent = PendingIntent.getActivity(VideoDownloadService.this, 0, notificationActivityIntent, 0);
			  			        builder.setContentTitle(filename + " - Download")
			  			        		.setContentText("Download complete")
			  			        		.setSmallIcon(R.drawable.ic_download_black)
			  		                    .setProgress(0,0,false)
			  		                    .setContentIntent(pendingIntent);
			  		            notifyManager.notify(msg.what, builder.build());
		  			        }
		  			       
		  			        bStopTask = false;
		  			        break;
		  			    }   
		  			}
		  			
		  			if (downloadDone != true)
		  			{
		  				throw new Exception();
		  			}
		  		}
		  		catch (Exception e)
		  		{
		  			if (bStopTask == true)
		  			{
		  				builder.setContentTitle(msg.getData().getString("data").split("\\|\\|\\|")[1] + " - Download")
		  					.setContentText("Download canceled")
		  					.setSmallIcon(R.drawable.ic_clear)
		  					.setProgress(0,0,false)
		  					.setContentIntent(null);
		  				notifyManager.notify(msg.what, builder.build());
		  			}
		  			else
		  			{
		  				file.delete();
		  				
			  			builder.setContentTitle(msg.getData().getString("data").split("\\|\\|\\|")[1] + " - Download")
			  				.setContentText("Download failed")
			  				.setSmallIcon(R.drawable.ic_error_outline)
			  				.setProgress(0,0,false)
			  				.setContentIntent(null);
			  			notifyManager.notify(msg.what, builder.build());
		  			}
		  		}
		        
		        stopSelfResult(msg.what);
	      }
	  }
	  
	  public class MediaScannerWrapper implements MediaScannerConnectionClient
	  {
	      private MediaScannerConnection mConnection;
	      private String mPath;
	      private String mMimeType;

	      public MediaScannerWrapper(Context ctx, String filePath, String mime)
	      {
	          mPath = filePath;
	          mMimeType = mime;
	          mConnection = new MediaScannerConnection(ctx, this);
	      }

	      public void scan() 
	      {
	          mConnection.connect();
	      }

	      public void onMediaScannerConnected() 
	      {
	          mConnection.scanFile(mPath, mMimeType);
	      }

	      public void onScanCompleted(String path, Uri uri) 
	      {
	    	  
	      }
	  }
	  
	  public class LocalBinder extends Binder 
	  {
	      VideoDownloadService getService() 
	      {
	          return VideoDownloadService.this;
	      }
	  }
	    

	  @Override
	  public void onCreate() 
	  {		
		  	notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		  
		    HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
		    thread.start();
	
		    mServiceLooper = thread.getLooper();
		    mServiceHandler = new ServiceHandler(mServiceLooper);
	  }

	  @Override
	  public int onStartCommand(Intent intent, int flags, int startId) 
	  {
		  String filename;
		  try
		  {
			  filename = intent.getDataString().split("\\|\\|\\|")[1];
		  }
		  catch(NullPointerException e)
		  {
			  filename = "filename";
			  e.printStackTrace();
		  }
		  
		  try
		  {
		      Message msg = mServiceHandler.obtainMessage();
		      msg.what = startId;
		      Bundle data = new Bundle();
		      data.putString("data", intent.getDataString());
		      msg.setData(data);
		      mServiceHandler.sendMessage(msg);
		      
		      Intent notificationActivityIntent = new Intent(this, NotificationFloatingActivity.class);
		      notificationActivityIntent.setData(Uri.parse(startId + "|||" + filename));
		      PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationActivityIntent, 0);
		      
			  NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
		      
		      builder.setContentTitle(filename + " - Download")
	    	    .setContentText("Download pending")
	    		.setSmallIcon(R.drawable.ic_download_white)
		      	.setProgress(0,0,false)
		      	.setContentIntent(pendingIntent);
		      notifyManager.notify(startId, builder.build());
		  }
		  catch (Exception e)
		  {
			  e.printStackTrace();
		  }

	      return START_STICKY;
	  }

	  @Override
	  public IBinder onBind(Intent intent) 
	  {
	      return mBinder;
	  }
	  
	  public void removeDownloadJob(int startId)
	  {
		  if(currentJobId == startId)
		  {
			  mServiceHandler.stopCurrentTask();
		  }
		  else
		  {
			  mServiceHandler.removeMessages(startId);
		      notifyManager.cancel(startId);
		  }
	  }
}