package com.yaroslav.posprintersample;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Yaroslav on 08.07.15.
 * Copyright 2015 iYaroslav LLC.
 */
public class CustomTV extends TextView {

	private static Typeface typeface;

	public CustomTV(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CustomTV(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CustomTV(Context context) {
		super(context);
		init();
	}

	private void init() {
		if (isInEditMode()) return;

		if (typeface == null) {
			typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/FreePixel.ttf");
//			Preview▓Предпросмотр
		}
		setTypeface(typeface);
	}

}
