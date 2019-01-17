package io.github.iyaroslav.posprinter

import android.app.Application
import android.content.Context

import com.google.android.gms.analytics.HitBuilders

/**
 * Created by Yaroslav on 04.07.15.
 * Copyright 2015 LeeryBit LLC.
 */
class App : Application() {

  val tracker: AnalyticsTrackers by lazy {
    AnalyticsTrackers(this)
  }

  override fun onCreate() {
    super.onCreate()

    val preferences = getSharedPreferences(APP_REFERENCES_KEY, Context.MODE_PRIVATE)
    var launchesCount = preferences.getInt(REFERENCES_LAUNCHES, 0)

    val action = if (launchesCount == 0) "first_launch" else "launch_app"
    tracker.send(HitBuilders.EventBuilder()
        .setCategory("app")
        .setAction(action)
        .setValue(launchesCount.toLong())
        .build())

    val editor = preferences.edit()
    editor.putInt(REFERENCES_LAUNCHES, ++launchesCount)
    editor.apply()
  }

  companion object {
    const val APP_REFERENCES_KEY = "APP_PREFERENCES"
    private const val REFERENCES_LAUNCHES = "count_of_launches"
  }
}
