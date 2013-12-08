package com.tumblr.railproboston.android;

import jscholl.commuterrail.R;
import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;

import com.tumblr.railproboston.android.engine.ScheduleEngine;
import com.tumblr.railproboston.android.engine.types.Route;
import com.tumblr.railproboston.android.engine.types.Trip;
import com.tumblr.railproboston.android.ui.RoutesFragment;
import com.tumblr.railproboston.android.ui.StopTimesFragment;
import com.tumblr.railproboston.android.ui.TripsFragment;

public class MainActivityA extends FragmentActivity implements
		RoutesFragment.OnRouteSelectedListener,
		TripsFragment.OnTripSelectedListener {
	private final static String CLASSNAME = new Object() {}.getClass().getEnclosingClass()
			.getSimpleName();
	public static final String EXTRA_ROUTE = "jscholl.commuterrail.ROUTE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(CLASSNAME, "Application started");
		setContentView(R.layout.activity_main);
		instance = this;
		ScheduleEngine.getServiceIdToday(this);

		//ActionBar actionBar = getSupportActionBar();

		// Check that the activity is using the layout version with
		// the fragment_container FrameLayout
		if (findViewById(R.id.fragment_container) != null) {

			// However, if we're being restored from a previous state,
			// then we don't need to do anything and should return or else
			// we could end up with overlapping fragments.
			if (savedInstanceState != null) {
				return;
			}

			// Create a new Fragment to be placed in the activity layout
			RoutesFragment firstFragment = new RoutesFragment();

			// In case this activity was started with special instructions from an
			// Intent, pass the Intent's extras to the fragment as arguments
			firstFragment.setArguments(getIntent().getExtras());

			// Add the fragment to the 'fragment_container' FrameLayout
			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, firstFragment).commit();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy(); // Always call the superclass
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onRouteSelected(Route route) {
		ScheduleEngine.setSelectedRoute(route);

		TripsFragment newFragment = new TripsFragment();
		Bundle args = new Bundle();
		args.putString(TripsFragment.EXTRA_ROUTE, route.routeId);
		newFragment.setArguments(args);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack so the user can navigate back
		transaction.replace(R.id.fragment_container, newFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}

	@Override
	public void onTripSelected(Trip trip) {
		ScheduleEngine.setSelectedTrip(trip);
		StopTimesFragment newFragment = new StopTimesFragment();
		Bundle args = new Bundle();
		args.putString(StopTimesFragment.EXTRA_TRIP, trip.tripId);
		newFragment.setArguments(args);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack so the user can navigate back
		transaction.replace(R.id.fragment_container, newFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();
	}

	private static MainActivityA instance;

	public static MainActivityA getInstance() {
		return instance;
	}
}
