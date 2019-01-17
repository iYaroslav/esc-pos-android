package com.leerybit.escpos.bluetooth;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leerybit.escpos.R;

/**
 * Copyright 2015 LeeryBit
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DeviceItemView extends LinearLayout {

  private ImageView icon;
  private TextView title;

  public DeviceItemView(Context context) {
    super(context);
    init(null);
  }

  public DeviceItemView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public DeviceItemView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  @SuppressWarnings("unused")
  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  public DeviceItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    setOrientation(HORIZONTAL);

    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    icon = new ImageView(getContext());
    params.gravity = Gravity.CENTER;
    icon.setLayoutParams(params);
    icon.setScaleType(ImageView.ScaleType.FIT_CENTER);

    params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
    params.gravity = Gravity.CENTER_VERTICAL;
    title = new TextView(getContext());
    title.setLayoutParams(params);
    title.setTextColor(getResources().getColor(R.color.pos_text_primary));
    title.setSingleLine();
    title.setEllipsize(TextUtils.TruncateAt.END);

    if (attrs != null) {
      TypedArray attributes = getContext().obtainStyledAttributes(attrs, R.styleable.DeviceItemView, 0, 0);
      setInsets(
          attributes.getDimensionPixelSize(R.styleable.DeviceItemView_insets_vertical, 0),
          attributes.getDimensionPixelSize(R.styleable.DeviceItemView_insets_horizontal, 0)
      );
      title.setText(attributes.getText(R.styleable.DeviceItemView_item_title));
      setIconDrawable(attributes.getDrawable(R.styleable.DeviceItemView_item_icon));
      setSelected(attributes.getBoolean(R.styleable.DeviceItemView_selected, false));
      attributes.recycle();
    } else {
      setSelected(false);
    }

    addView(icon);
    addView(title);
  }

  public void setSelected(boolean selected) {
    int color;
    if (selected) {
      setBackgroundColor(getResources().getColor(R.color.pos_highlight));
      color = getResources().getColor(R.color.pos_accent);
      title.setTextColor(color);
    } else {
      setBackgroundResource(R.drawable.menu_item_bg);
      title.setTextColor(getResources().getColor(R.color.pos_text_primary));
      color = getResources().getColor(R.color.pos_text_secondary);
    }

    setClickable(!selected);
    if (!isInEditMode()) {
      Drawable drawable = icon.getDrawable();
      if (drawable != null) {
        drawable.mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        icon.setImageDrawable(drawable);
      }
    }
  }

  public void setIconResourceId(int resId) {
    setIconDrawable(ContextCompat.getDrawable(getContext(), resId));
  }

  public void setIconDrawable(Drawable drawable) {
    if (drawable == null) {
      icon.setVisibility(GONE);
      return;
    }

    icon.setVisibility(VISIBLE);
    icon.setImageDrawable(drawable);
  }

  public void setTitle(int resId) {
    setTitle(getResources().getString(resId));
  }

  public void setTitle(String title) {
    this.title.setText(title);
  }

  public void setInsets(int vertical, int horizontal) {
    icon.setPadding(horizontal, 0, 0, 0);
    title.setPadding(vertical, vertical, horizontal, vertical);
  }
}
