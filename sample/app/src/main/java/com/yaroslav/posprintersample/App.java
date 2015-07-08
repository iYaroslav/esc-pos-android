package com.yaroslav.posprintersample;

import android.app.Application;
import android.content.SharedPreferences;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Yaroslav on 04.07.15.
 * Copyright 2015 iYaroslav LLC.
 */
public class App extends Application {

	public static final String APP_REFERENCES_KEY = "APP_PREFERENCES";
	private static final String REFERENCES_LAUNCHES = "count_of_launches";

	@Override
	public void onCreate() {
		super.onCreate();

		AnalyticsTrackers.initialize(this);
		Tracker tracker = getTracker();

		SharedPreferences preferences = getSharedPreferences(APP_REFERENCES_KEY, MODE_PRIVATE);
		int launchesCount = preferences.getInt(REFERENCES_LAUNCHES, 0);

		String action = launchesCount==0?"first_launch":"launch_app";
		tracker.send(new HitBuilders.EventBuilder()
				.setCategory("app")
				.setAction(action)
				.setValue(launchesCount)
				.build());

		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(REFERENCES_LAUNCHES, ++launchesCount);
		editor.apply();
	}

	public static Tracker getTracker() {
		return AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
	}

}
