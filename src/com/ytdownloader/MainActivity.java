package com.ytdownloader;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks
{

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	
	private String currentSection;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
		
		currentSection = getString(R.string.title_home);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position)
	{	
		Fragment fragment;
		
		switch(position)
		{
			case 0:
				fragment = HomeFragment.newInstance(position + 1);
				currentSection = getString(R.string.title_home);
				break;
			case 1:
				fragment = AboutFragment.newInstance(position + 1);
				currentSection = getString(R.string.title_about);
				break;
			case 2:
				// Exit
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			default:
				fragment = HomeFragment.newInstance(position + 1);
				currentSection = getString(R.string.title_home);
		}
		
		// update the main content by replacing fragments
			getFragmentManager()
				.beginTransaction()
				.replace(R.id.container, fragment, currentSection).commit();	
	}

	public void onSectionAttached(int number)
	{
		switch (number) 
		{
		case 1:
			mTitle = getString(R.string.title_home);
			break;
		case 2:
			mTitle = getString(R.string.title_about);
			break;
		}
	}

	public void restoreActionBar()
	{
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		final Menu mMenu = menu;
		
		if (!mNavigationDrawerFragment.isDrawerOpen() && currentSection.equals(getString(R.string.title_home)))
		{
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			
			SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	        SearchView search = (SearchView) menu.findItem(R.id.search).getActionView();
	        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
	        search.setOnQueryTextListener(new OnQueryTextListener() 
	        { 
	            @Override 
	            public boolean onQueryTextSubmit(String query) 
	            {
	            	if (currentSection.equals(getString(R.string.title_home)))
	            	{
		            	HomeFragment homeFragment = (HomeFragment) getFragmentManager().findFragmentByTag(getString(R.string.title_home));
		    	        homeFragment.onSearchSubmit(query);
	            	}
	            	
	            	if (mMenu != null)
	            	{
	            		mMenu.findItem(R.id.search).collapseActionView();
	            	}
	            	
	                return true; 
	            } 
	            
	            @Override 
	            public boolean onQueryTextChange(String query) 
	            {
	                return true; 
	            }
	        });
			
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (item.getItemId() == R.id.action_download)
		{
			if (currentSection.equals(getString(R.string.title_home)))
			{
				HomeFragment homeFragment = (HomeFragment) getFragmentManager().findFragmentByTag(getString(R.string.title_home));
		        homeFragment.downloadCurrentVideo();
			}
		}
		else if (item.getItemId() == R.id.action_refreshPage)
		{
			if (currentSection.equals(getString(R.string.title_home)))
			{
				HomeFragment homeFragment = (HomeFragment) getFragmentManager().findFragmentByTag(getString(R.string.title_home));
		        homeFragment.refreshPage();
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
	    // Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) & currentSection.equals(getString(R.string.title_home))) 
	    {
	        HomeFragment homeFragment = (HomeFragment) getFragmentManager().findFragmentByTag(getString(R.string.title_home));
	        if (homeFragment.canGoBack())
	        {
		        homeFragment.onBackKeyPressed();
		        return true;
	        }
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
}
