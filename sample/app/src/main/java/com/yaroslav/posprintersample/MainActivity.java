package com.yaroslav.posprintersample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import sam.lab.posprinter.PosPrinter;

/**
 * Created by Yaroslav on 04.07.15.
 * Copyright 2015 iYaroslav LLC.
 */
public class MainActivity extends AppCompatActivity {

	private PosPrinter printer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Tracker tracker = App.getTracker();
		tracker.setScreenName("MainActivity");
		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
