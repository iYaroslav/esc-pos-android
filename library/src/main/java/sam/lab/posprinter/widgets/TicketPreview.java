package sam.lab.posprinter.widgets;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import sam.lab.posprinter.Ticket;

/**
 * Copyright 2015 Samardak Yaroslav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class TicketPreview extends android.support.v7.widget.AppCompatTextView {

	private static Typeface typeface;

	public TicketPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TicketPreview(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TicketPreview(Context context) {
		super(context);
		init();
	}

	private void init() {
		setGravity(Gravity.CENTER);
		if (isInEditMode()) {
			setTypeface(Typeface.MONOSPACE);
			setText("┌────────┐\n│ Ticket │\n└────────┘");
			return;
		}

		if (typeface == null) {
			typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/FreePixel.ttf");
		}
		setTypeface(typeface);
	}

	public void setTicket(Ticket ticket) {
//		TODO calculate text size for fit to width
		setText(ticket.getTicketPreview());
	}

}
