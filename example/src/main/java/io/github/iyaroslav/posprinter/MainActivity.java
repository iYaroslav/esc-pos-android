package io.github.iyaroslav.posprinter;

import android.content.Intent;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.IOException;
import java.util.Date;

import sam.lab.posprinter.DeviceCallbacks;
import sam.lab.posprinter.PosPrinter;
import sam.lab.posprinter.Ticket;
import sam.lab.posprinter.TicketBuilder;
import sam.lab.posprinter.widgets.TicketPreview;
import sam.lab.posprinter.bluetooth.BTService;

/**
 * Created by Yaroslav on 04.07.15.
 * Copyright 2015 iYaroslav LLC.
 */
public class MainActivity extends AppCompatActivity {

	private PosPrinter printer;
	private TicketPreview ticketPreview;
	private int ticketNumber = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		TODO register in http://android-arsenal.com

		Tracker tracker = App.getTracker();
		tracker.setScreenName("MainActivity");
		tracker.send(new HitBuilders.ScreenViewBuilder().build());

		final Button btn = (Button) findViewById(R.id.button);
		final Button btnSend = (Button) findViewById(R.id.btn_send);
		btnSend.setEnabled(true);

		final TextView tvState = (TextView) findViewById(R.id.tv_state);
		final TextView tvMessage = (TextView) findViewById(R.id.tv_message);
		ticketPreview = (TicketPreview) findViewById(R.id.ticket);

		printer = PosPrinter.getPrinter(this);
		printer.setCharsetName("UTF-8");
		printer.setDeviceCallbacks(new DeviceCallbacks() {
			@Override
			public void onConnected() {
				btn.setText(R.string.action_disconnect);
				btnSend.setEnabled(true);
			}

			@Override
			public void onFailure() {
				Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
//				btnSend.setEnabled(false);
			}

			@Override
			public void onDisconnected() {
				btn.setText(R.string.action_connect);
//				btnSend.setEnabled(false);
			}
		});
		printer.setStateChangedListener(new PosPrinter.OnStateChangedListener() {
			@Override
			public void onChanged(int state, Message msg) {
				switch (state) {
					case BTService.STATE_NONE:
						tvState.setText("State: NONE");
						tvState.setTextColor(getResources().getColor(R.color.text));
						break;
					case BTService.STATE_CONNECTED:
						tvState.setText("State: CONNECTED");
						tvState.setTextColor(getResources().getColor(R.color.green));
						break;
					case BTService.STATE_CONNECTING:
						tvState.setText("State: CONNECTING");
						tvState.setTextColor(getResources().getColor(R.color.blue));
						break;
					case BTService.STATE_LISTENING:
						tvState.setText("State: LISTENING");
						tvState.setTextColor(getResources().getColor(R.color.amber));
						break;
				}

				switch (msg.arg2) {
					case BTService.MESSAGE_STATE_CHANGE:
						tvMessage.setText("Message: STATE CHANGED");
						tvMessage.setTextColor(getResources().getColor(R.color.text));
						break;
					case BTService.MESSAGE_READ:
						tvMessage.setText("Message: READ (" + msg.what + ")");
						tvMessage.setTextColor(getResources().getColor(R.color.green));
						break;
					case BTService.MESSAGE_WRITE:
						tvMessage.setText("Message: WRITE (" + msg.what + ")");
						tvMessage.setTextColor(getResources().getColor(R.color.green));
						break;
					case BTService.MESSAGE_CONNECTION_LOST:
						tvMessage.setText("Message: CONNECTION LOST");
						tvMessage.setTextColor(getResources().getColor(R.color.red));
						break;
					case BTService.MESSAGE_UNABLE_TO_CONNECT:
						tvMessage.setText("Message: UNABLE TO CONNECT");
						tvMessage.setTextColor(getResources().getColor(R.color.red));
						break;
				}
			}
		});

		if (!printer.isAvailable()) {
			btn.setText("NOT AVAILABLE");
			btn.setEnabled(false);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (printer != null) {
			printer.onActivityResult(requestCode, resultCode, data);
		}
	}

	public void onSearchClick(View view) {
		if (!printer.isConnected()) {
			printer.connect();
		} else {
			printer.disconnect();
		}
	}

	@SuppressWarnings("SpellCheckingInspection")
	public void onPrintClick(View view) {
		try {
			Date date = new Date();
			Ticket ticket;

			ticket = new TicketBuilder()
					.isCyrillic(true)
					.raw(this, R.raw.ticket,
							DateFormat.format("dd.MM.yyyy", date).toString(),
							DateFormat.format("HH:mm", date).toString(),
							(++ticketNumber) + ""
					)
					.fiscalInt("ticket_no", ticketNumber)
					.fiscalDouble("gift", 3, 2)
					.fiscalDouble("price", 131.30, 2)
					.fiscalDouble("out_price", 128.30, 2)

//					.header("PosPrinter")
//					.divider()
//						.center("Кирилица должна поддерживаться самим принтером, инече будут кракозябры или пустота")
//					.divider()
//						.text("Date: " + DateFormat.format("dd.MM.yyyy", date).toString())
//						.text("Time: " + DateFormat.format("HH:mm", date).toString())
//						.text("Ticket No: " + (++ticketNumber))
//						.fiscalInt("ticket_no", ticketNumber)
//					.divider()
//					.subHeader("Hot dishes")
//						.menuLine("— 3 Kazan kabob", "60,00")
//						.menuLine("— 2 Full-Rack Ribs", "32,00")
//						.right("Total: 92,00")
//					.feedLine()
//					.subHeader("Salads")
//						.menuLine("— 1 Turkey & Swiss", "4,50")
//						.menuLine("— 1 Classic Cheese", "3,30")
//						.menuLine("— 1 Chicken Caesar Salad", "7,00")
//						.right("Total: 14,80")
//					.feedLine()
//					.subHeader("Desserts")
//						.menuLine("— 1 Blondie", "5,00")
//						.menuLine("— 2 Chocolate Cake", "7,00")
//						.right("Total: 12,00")
//					.feedLine()
//					.subHeader("Drinkables")
//						.center("50% sale for Coke on mondays!")
//						.menuLine("— 3 Coca-Cola", "6,00")
//						.menuLine("— 7 Tea", "3,50")
//						.menuLine("— 2 Coffee", "3,00")
//						.right("Total: 12,50")
//					.dividerDouble()
//						.menuLine("Total gift", "3,00")
//						.menuLine("Total", "128,30")
//						.fiscalDouble("gift", 3, 2)
//						.fiscalDouble("price", 131.30, 2)
//						.fiscalDouble("out_price", 128.30, 2)
//					.dividerDouble()
//						.stared("THANK YOU")
					.build();

			ticketPreview.setTicket(ticket);
			printer.send(ticket);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		Log.e("TICKET", ticket.toString());
	}
}
