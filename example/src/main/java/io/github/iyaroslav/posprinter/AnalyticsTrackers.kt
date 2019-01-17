package io.github.iyaroslav.posprinter

import android.content.Context

import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker

class AnalyticsTrackers constructor(context: Context) {

  private val tracker: Tracker

  init {
    val analytics = GoogleAnalytics.getInstance(context)
    analytics.setLocalDispatchPeriod(1800)

    tracker = analytics.newTracker(R.xml.app_tracker)
    tracker.enableExceptionReporting(true)
    tracker.enableAdvertisingIdCollection(true)
    tracker.enableAutoActivityTracking(true)
  }

  fun send(values: Map<String, String>) = tracker.send(values)
  fun setScreenName(name: String) = tracker.setScreenName(name)

}
