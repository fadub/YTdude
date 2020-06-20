package com.ytdownloader;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class HomeFragment extends Fragment
{
	private static final String ARG_SECTION_NUMBER = "section_number";
	private WebView webView;
	private LinkedHashMap<Integer, String> itagQualityFullMap;
	private LinkedHashMap<String, String> qualityContainerMap;
	
	private final String playerScriptUrlTemplate = "http://s.ytimg.com/yts/jsbin/player-%s/base.js";
    private final String decodeFunctionPatternTemplate = "#NAME#.?\\=.?function.?\\(.*?\\).?\\{.*?\\}";
    private final String helperObjectPatternTemplate = "var.?#NAME#.?\\=.?\\{.*?\\};";
    
    private Pattern signatureRegex = Pattern.compile("s=([A-F0-9]+\\.[A-F0-9]+)");
    private Pattern playerVersionRegex = Pattern.compile("player-([\\w\\d\\-]+)\\\\\\/base\\.js");
    private Pattern decodeFunctionNameRegex = Pattern.compile("\\.sig\\|\\|([a-zA-Z0-9$]+)\\(");
    private Pattern helperObjectNameRegex = Pattern.compile(";([A-Za-z0-9]+)\\.");
    private Matcher matcher;
    private String decodedSignatures[] = null;
    private String videoTitle = "Title";
    protected boolean bGettingVideoInfo = false;
	
	public HomeFragment()
	{		
		itagQualityFullMap = new LinkedHashMap<Integer, String>();
		itagQualityFullMap.put(5, "FLV 240p (video)");
		itagQualityFullMap.put(17, "3GP 144p (video)");
		itagQualityFullMap.put(18, "MP4 360p (video)");
		itagQualityFullMap.put(22, "MP4 720p (video)");
		itagQualityFullMap.put(36, "3GP (video)");
		itagQualityFullMap.put(43, "WebM (video)");
		itagQualityFullMap.put(82, "MP4 360p (video)");
		itagQualityFullMap.put(83, "MP4 240p (video)");
		itagQualityFullMap.put(84, "MP4 720p (video)");
		itagQualityFullMap.put(85, "MP4 1080p (video)");
		itagQualityFullMap.put(100, "WebM 360p (video)");
		itagQualityFullMap.put(133, "MP4 240p (video - no sound)");
		itagQualityFullMap.put(134, "MP4 360p (video - no sound)");
		itagQualityFullMap.put(135, "MP4 480p (video - no sound)");
		itagQualityFullMap.put(136, "MP4 720p (video - no sound)");
		itagQualityFullMap.put(137, "MP4 1080p (video - no sound)");
		itagQualityFullMap.put(138, "MP4 2160p-4320p (video - no sound)");
		itagQualityFullMap.put(160, "MP4 144p (video - no sound)");
		itagQualityFullMap.put(242, "WebM 240p (video - no sound)");
		itagQualityFullMap.put(243, "WebM 360p (video - no sound)");
		itagQualityFullMap.put(244, "WebM 480p (video - no sound)");
		itagQualityFullMap.put(247, "WebM 720p (video - no sound)");
		itagQualityFullMap.put(248, "WebM 1080p (video - no sound)");
		itagQualityFullMap.put(264, "MP4 1440p (video - no sound)");
		itagQualityFullMap.put(266, "MP4 2160p-2304p (video - no sound)");
		itagQualityFullMap.put(271, "WebM 1440p (video - no sound)");
		itagQualityFullMap.put(278, "WebM 144p (video - no sound)");
		itagQualityFullMap.put(298, "MP4 360p/720p HFR (video - no sound)");
		itagQualityFullMap.put(299, "MP4 480p/1080p HFR (video - no sound)");
		itagQualityFullMap.put(302, "WebM 360p/720p HFR (video - no sound)");
		itagQualityFullMap.put(303, "WebM 480p/1080p HFR (video - no sound)");
		itagQualityFullMap.put(308, "WebM 1440p HFR (video - no sound)");
		itagQualityFullMap.put(313, "WebM 2160p (video - no sound)");
		itagQualityFullMap.put(315, "WebM 2160p HFR (video - no sound)");
		itagQualityFullMap.put(140, "M4A 128kbit/s (audio)");
		itagQualityFullMap.put(141, "M4A 256kbit/s (audio)");
		itagQualityFullMap.put(171, "WebM 128kbit/s (audio)");
		itagQualityFullMap.put(249, "WebM 48kbit/s (audio)");
		itagQualityFullMap.put(250, "WebM 64kbit/s (audio)");
		itagQualityFullMap.put(251, "WebM 160kbit/s (audio)");
		
		qualityContainerMap = new LinkedHashMap<String, String>();
		qualityContainerMap.put("FLV 240p (video)", ".flv");
		qualityContainerMap.put("3GP 144p (video)", ".3gp");
		qualityContainerMap.put("MP4 360p (video)", ".mp4");
		qualityContainerMap.put("MP4 720p (video)", ".mp4");
		qualityContainerMap.put("3GP (video)", ".3gp");
		qualityContainerMap.put("WebM (video)", ".webm");
		qualityContainerMap.put("MP4 360p (video)", ".mp4");
		qualityContainerMap.put("MP4 240p (video)", ".mp4");
		qualityContainerMap.put("MP4 720p (video)", ".mp4");
		qualityContainerMap.put("MP4 1080p (video)", ".mp4");
		qualityContainerMap.put("WebM 360p (video)", ".webm");
		qualityContainerMap.put("MP4 240p (video - no sound)", ".mp4");
		qualityContainerMap.put("MP4 360p (video - no sound)", ".mp4");
		qualityContainerMap.put("MP4 480p (video - no sound)", ".mp4");
		qualityContainerMap.put("MP4 720p (video - no sound)", ".mp4");
		qualityContainerMap.put("MP4 1080p (video - no sound)", ".mp4");
		qualityContainerMap.put("MP4 2160p-4320p (video - no sound)", ".mp4");
		qualityContainerMap.put("MP4 144p (video - no sound)", ".mp4");
		qualityContainerMap.put("WebM 240p (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 360p (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 480p (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 720p (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 1080p (video - no sound)", ".webm");
		qualityContainerMap.put("MP4 1440p (video - no sound)", ".mp4");
		qualityContainerMap.put("MP4 2160p-2304p (video - no sound)", ".mp4");
		qualityContainerMap.put("WebM 1440p (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 144p (video - no sound)", ".webm");
		qualityContainerMap.put("MP4 360p/720p HFR (video - no sound)", ".mp4");
		qualityContainerMap.put("MP4 480p/1080p HFR (video - no sound)", ".mp4");
		qualityContainerMap.put("WebM 360p/720p HFR (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 480p/1080p HFR (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 1440p HFR (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 2160p (video - no sound)", ".webm");
		qualityContainerMap.put("WebM 2160p HFR (video - no sound)", ".webm");
		qualityContainerMap.put("M4A 128kbit/s (audio)", ".m4a");
		qualityContainerMap.put("M4A 256kbit/s (audio)", ".m4a");
		qualityContainerMap.put("WebM 128kbit/s (audio)", ".m4a");
		qualityContainerMap.put("WebM 48kbit/s (audio)", ".webm");
		qualityContainerMap.put("WebM 64kbit/s (audio)", ".webm");
		qualityContainerMap.put("WebM 160kbit/s (audio)", ".webm");
	}
	
	public static HomeFragment newInstance(int sectionNumber) 
	{
		HomeFragment fragment = new HomeFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		return inflater.inflate(R.layout.fragment_home, container, false);
	}
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState)
	{
		webView = (WebView) getActivity().findViewById(R.id.webview);
		webView.setWebViewClient(new MyWebViewClient());
		webView.setWebChromeClient(new WebChromeClient());
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setAllowUniversalAccessFromFileURLs(true);
		webSettings.setBuiltInZoomControls(true);
		
		super.onActivityCreated(savedInstanceState);
	}
	
	private class MyWebViewClient extends WebViewClient 
	{
		
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) 
	    {
	        if (Uri.parse(url).getHost().startsWith("www.youtube") || Uri.parse(url).getHost().startsWith("youtube") || Uri.parse(url).getHost().startsWith("m.youtube")) 
	        {
	            view.loadUrl(url);
	            return false;
	        }
	        
	        return true;
	    }
	}
	
	protected void onBackKeyPressed()
	{
		if (webView.canGoBack())
		{
			webView.goBack();
		}
	}
	
	protected boolean canGoBack()
	{
		return webView.canGoBack();
	}
	
	protected void refreshPage()
	{
		webView.reload();
	}
	
	protected void downloadCurrentVideo()
	{
		try
		{
			if (webView.getUrl().contains("watch?v") & bGettingVideoInfo != true)
			{
				ExtractVideoUrl extractVideoUrl = new ExtractVideoUrl("http://www.youtube.com/watch?" + Uri.parse(webView.getUrl()).getQuery(), this);
				extractVideoUrl.execute();
				Toast.makeText(getActivity(), "Getting video information... This might take a few moments.", Toast.LENGTH_LONG).show();
				bGettingVideoInfo = true;
			}
			else if (bGettingVideoInfo == true)
			{
				
			}
			else
			{
				throw new Exception("no video to download");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(getActivity(), "no video to download", Toast.LENGTH_SHORT).show();
		}
	}
	
	private LinkedHashMap<String, String> extractPlaybackUrls(String url)
	{
		try
		{
			// get youtube video desktop html source
		    String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0";
		    HttpClient client = new DefaultHttpClient();
		    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, userAgent);
		    HttpGet request = new HttpGet(url);
		    HttpResponse response = client.execute(request);
		    InputStream in = response.getEntity().getContent();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		    StringBuilder sb = new StringBuilder();
		    String line = null;
		    while ((line = reader.readLine()) != null) 
		    {
		        sb.append(line.replace("\\u0026", "&"));
		    }
		    in.close();
		    String html = sb.toString();
		    
		    try
		    {
		    	videoTitle = StringEscapeUtils.unescapeHtml4(html.split("\\<title\\>")[1].split("\\<\\/title\\>")[0].replace(" - YouTube", ""));
		    }
		    catch(Exception e)
		    {
		    	e.printStackTrace();
		    }
		  
		    // search for signatures
		    try
		    {
		    	matcher = signatureRegex.matcher(html);
		    	int signatureMatchCount = 0;
		    	while (matcher.find())
		    	{
		    		signatureMatchCount++;
		    	}
		    	if (signatureMatchCount == 0)
		    	{
		    		throw new IllegalStateException();
		    	}
		    	matcher = signatureRegex.matcher(html);
		    	String[] encodedSignatures = new String[signatureMatchCount];
		    	decodedSignatures = new String[signatureMatchCount];
		    	for (int i = 0; i < signatureMatchCount; i++)
		    	{
		    		if (matcher.find())
		    		{
		    			encodedSignatures[i] = matcher.group(1);
		    		}
		    	}
		    	
		    	matcher = playerVersionRegex.matcher(html);
		    	matcher.find();
		    	String playerVersion = matcher.group(1);
		    	
		    	String playerScriptUrl = String.format(playerScriptUrlTemplate, playerVersion);
		    	
			    request = new HttpGet(playerScriptUrl);
			    response = client.execute(request);
			    in = response.getEntity().getContent();
			    reader = new BufferedReader(new InputStreamReader(in));
			    sb = new StringBuilder();
			    line = null;
			    while ((line = reader.readLine()) != null) 
			    {
			        sb.append(line);
			    }
			    in.close();
		    	String playerScript = sb.toString();
		    	playerScript = playerScript.replace("\n", "").replace("\r", "");
		    	
		    	matcher = decodeFunctionNameRegex.matcher(playerScript);
		    	matcher.find();
		    	String decodeFunctionName = matcher.group(1);
		    	
		    	Pattern decodeFunctionRegex = Pattern.compile(decodeFunctionPatternTemplate.replace("#NAME#", decodeFunctionName));
		    	matcher = decodeFunctionRegex.matcher(playerScript);
		    	matcher.find();
		    	String decodeFunction = matcher.group();
		    	
		    	matcher = helperObjectNameRegex.matcher(decodeFunction);
		    	matcher.find();
		    	String helperObjectName = matcher.group(1);
		    	
		    	Pattern helperObjectRegex = Pattern.compile(helperObjectPatternTemplate.replace("#NAME#", helperObjectName));
		    	matcher = helperObjectRegex.matcher(playerScript);
		    	matcher.find();
		    	String helperObject = matcher.group();
		    	
		    	for (int i = 0; i < encodedSignatures.length; i++)
		    	{
			    	Object[] params = new Object[] { encodedSignatures[i] };
			    	Context rhino = Context.enter();
			    	rhino.setOptimizationLevel(-1);
			    	try 
			    	{
			    	    Scriptable scope = rhino.initStandardObjects();
			    	    rhino.evaluateString(scope, helperObject + decodeFunction, "JavaScript", 1, null);
			    	    Object obj = scope.get(decodeFunctionName, scope);
	
			    	    if (obj instanceof Function) 
			    	    {
			    	        Function jsFunction = (Function) obj;
			    	        Object jsResult = jsFunction.call(rhino, scope, scope, params);
			    	        decodedSignatures[i] = Context.toString(jsResult);
			    	    }
			    	} 
			    	finally 
			    	{
			    	    Context.exit();
			    	}
			    	
			    	decodedSignatures[i] = "&signature=" + decodedSignatures[i];
		    	}
		    }
		    catch (Exception e)
		    {
		    	decodedSignatures = null;
		    	e.printStackTrace();
		    }
		    
		    // extract video urls and decode them
		    LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<String, String>();
		    for (String str : html.split("&url="))
		    {
		    	String encoded_url = "";
		    	
		    	for (int itag : itagQualityFullMap.keySet())
		    	{
		    		if (str.contains("itag%3D" + String.valueOf(itag).trim()))
		    		{
		    			encoded_url = str.split("&|\\,")[0];
		    			String decoded_url = java.net.URLDecoder.decode(encoded_url, "UTF-8");
		    			for (int i = 0; i < 4; i++)
		    			{
		    				decoded_url = java.net.URLDecoder.decode(decoded_url, "UTF-8");
		    			}
		    			if (decoded_url.startsWith("http"))
		    			{
		    				linkedHashMap.put(itagQualityFullMap.get(itag), decoded_url);
		    			}
		    		}
		    	}
		    }
		    
		    return linkedHashMap;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	protected void promptQualityAndName(final LinkedHashMap<String, String> QualityUrlMap)
	{
        AlertDialog.Builder qualityDialog = new AlertDialog.Builder(getActivity());
        final CharSequence[] items = new CharSequence[QualityUrlMap.size()];
        int i = 0;
        for (String quality : QualityUrlMap.keySet())
        {
        	items[i] = quality;
        	i++;
        }
        qualityDialog.setItems(items, new DialogInterface.OnClickListener() 
        {
            @Override
            public void onClick(DialogInterface dialog, int which) 
            {
            	final String quality = (String) items[which];
	           	AlertDialog.Builder filenameDialog = new AlertDialog.Builder(getActivity());
	           	final EditText edittext = new EditText(getActivity());
	           	filenameDialog.setTitle("Enter file title");
	           	edittext.setText(videoTitle);
	           	edittext.setSelectAllOnFocus(true);
	           	filenameDialog.setView(edittext);
	           	filenameDialog.setPositiveButton("Set", new DialogInterface.OnClickListener() 
	           	{	
	           	    public void onClick(DialogInterface dialog, int whichButton) 
	           	    {
	           	    	String filename = edittext.getText().toString();
	           	    	filename = filename + qualityContainerMap.get(quality);
	           	    	Intent videoDownloadService = new Intent(getActivity(), VideoDownloadService.class);
	           	    	String decodedSignaturesParam = "";
	           	    	if (decodedSignatures != null)
	           	    	{
	           	    		for (int i = 0; i < decodedSignatures.length; i++)
	           	    		{
	           	    			decodedSignaturesParam = decodedSignaturesParam + "|||" + decodedSignatures[i];
	           	    		}
	           	    	}
	           	    	else
	           	    	{
	           	    		decodedSignaturesParam = "|||_";
	           	    	}
	           	    	videoDownloadService.setData(Uri.parse(QualityUrlMap.get(quality) + "|||" + filename + decodedSignaturesParam));
	           	    	getActivity().startService(videoDownloadService);
	           	    	Toast.makeText(getActivity(), filename + " added to download queue", Toast.LENGTH_LONG).show();
	           	    }
	           	});
	           	filenameDialog.show();
            }
        });
        qualityDialog.setTitle("Select a quality/format");
        qualityDialog.setCancelable(true);
        qualityDialog.show();
	}
	
	protected void onSearchSubmit(String query)
	{
		webView.loadUrl("http://m.youtube.com/results?q=" + query);
	}
	
	private class ExtractVideoUrl extends AsyncTask<Void, Void, LinkedHashMap<String, String>>
	{
		private String url;
		HomeFragment homeFragment;
		
		public ExtractVideoUrl(String url, HomeFragment homeFragment)
		{
			this.url = url;
			this.homeFragment = homeFragment;
		}
		
		@Override
		protected LinkedHashMap<String, String> doInBackground(Void...arg0) 
		{
			if (url != null)
			{
				return extractPlaybackUrls(url);
			}
			else
			{
				return null;
			}
		}
		
	    @Override
	    protected void onPostExecute(LinkedHashMap<String, String> result) 
	    {
	        this.homeFragment.promptQualityAndName(result);
	        bGettingVideoInfo = false;
	    }
 
	}
}
