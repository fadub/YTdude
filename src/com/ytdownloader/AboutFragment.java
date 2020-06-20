package com.ytdownloader;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment
{
	private static final String ARG_SECTION_NUMBER = "section_number";
	private TextView tvHelp;
	private String helpText = "<center><h2>About</h2></center> <h4>Version</h4> <p>1.0.1</p><hr><br> <center><h2>FAQ</h2></center> <h4>Which quality is best for music?</h4> <p>M4A 128kbit/s (audio) OR M4A 256kbit/s (audio)</p> <h4>Which format is recommended for video?</h4> <p>MP4</p>";
	
	public static AboutFragment newInstance(int sectionNumber) 
	{
		AboutFragment fragment = new AboutFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_SECTION_NUMBER, sectionNumber);
		fragment.setArguments(args);
		return fragment;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		return inflater.inflate(R.layout.fragment_about, container, false);
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState)
	{	
		super.onActivityCreated(savedInstanceState);
		tvHelp = (TextView) getActivity().findViewById(R.id.tvHelp);
	    tvHelp.setText(Html.fromHtml(helpText));
	}
	
	@Override
	public void onResume()
	{
		tvHelp.setText(Html.fromHtml(helpText));
		super.onResume();
	}
	
	@Override
	public void onAttach(Activity activity) 
	{
		super.onAttach(activity);
		((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
	}
	
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
    {
        inflater.inflate(R.menu.global, menu);
        restoreActionBar();
        super.onCreateOptionsMenu(menu, inflater);
    }
    
	public void restoreActionBar() 
	{
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(getString(R.string.title_about));
	}
}
