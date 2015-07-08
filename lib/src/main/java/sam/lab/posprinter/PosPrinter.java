package sam.lab.posprinter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import sam.lab.posprinter.bluetooth.BTService;
import sam.lab.posprinter.bluetooth.SearchPrinterDialog;

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
public class PosPrinter implements Printer {

	private static final String TAG = "PosPrinter";

	private final Activity activity;
	private static PosPrinter instance;

	private final BTService btService;
	private OnStateChangedListener onStateChangedListener;

	boolean debug = false;
	int smallCharsOnLine =  35;
	int mediumCharsOnLine = 35;
	int largeCharsOnLine =  35;
	int hugeCharsOnLine =   35;
	String charsetName = "UTF-8";

	private PosPrinter(Activity activity) {
		this.activity = activity;

		btService = new BTService(activity, new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (onStateChangedListener != null) {
					onStateChangedListener.onChanged(msg.arg1, msg);
				}
				Log.e("MESSAGE", msg.toString());
			}
		});
		btService.start();
	}
	public static PosPrinter getPrinter(Activity activity) {
		if (instance == null) {
			instance = new PosPrinter(activity);
		}

		return instance;
	}

	@Override
	public void send(Ticket ticket) {
		ticket.saveFiscalData();
		if (debug) Log.d(TAG, "Send to print:\n" + ticket.toString());
//		TODO print data!
	}

	@Override
	public boolean isAvailable() {
		return btService.isAvailable();
	}

	@Override
	public boolean isEnabled() {
		return btService.isEnabled();
	}

	@Override
	public boolean isConnected() {
		return btService.getState() == BTService.STATE_CONNECTED;
	}

	@Override
	public void setDeviceCallbacks(DeviceCallbacks callbacks) {
		btService.setDeviceCallbacks(callbacks);
	}
	public void setStateChangedListener(OnStateChangedListener listener) {
		onStateChangedListener = listener;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		btService.onActivityResult(requestCode, resultCode, data);
	}

//	private void connectToDevice(final BluetoothDevice device) {
//		final Handler mainThread = new Handler(Looper.getMainLooper());
//		final ProgressDialog dialog = new ProgressDialog(activity);
//		String deviceName = device.getName();
//		if (deviceName == null || deviceName.isEmpty()) deviceName = device.getAddress();
//
//		String title = activity.getString(R.string.pos_title_pairing_with) + " " + deviceName;
//		dialog.setMessage(title);
//		dialog.setCancelable(false);
//		dialog.show();
//
//		new Thread() {
//
//			@Override
//			public void run() {
//				super.run();
//
//				try {
//					try {
//						//noinspection RedundantArrayCreation, SpellCheckingInspection
//						Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//						try {
//							final BluetoothSocket socket = (BluetoothSocket) m.invoke(device, 1);
//							socket.connect();
//							Log.d(TAG, device.getName() + " - connected");
//							mainThread.post(new Runnable() {
//								@Override
//								public void run() {
//									PosPrinter.this.device = device;
//									PosPrinter.this.socket = socket;
//									isConnected = true;
//									dialog.hide();
//									if (printerEvents != null)
//										printerEvents.onConnected();
//								}
//							});
//							return;
//						} catch (IOException e) {
//							Log.e(TAG, "IOException: " + e.getLocalizedMessage());
//							Log.d(TAG, device.getName() + " - not connected");
//						}
//					} catch (IllegalArgumentException e) {
//						Log.e(TAG, "IllegalArgumentException: " + e.getLocalizedMessage());
//					} catch (IllegalAccessException e) {
//						Log.e(TAG, "IllegalAccessException: " + e.getLocalizedMessage());
//					} catch (InvocationTargetException e) {
//						Log.e(TAG, "InvocationTargetException: " + e.getLocalizedMessage());
//					}
//				} catch (SecurityException e) {
//					Log.e(TAG, "SecurityException: " + e.getLocalizedMessage());
//				} catch (NoSuchMethodException e) {
//					Log.e(TAG, "NoSuchMethodException: " + e.getLocalizedMessage());
//				}
//
//				mainThread.post(new Runnable() {
//					@Override
//					public void run() {
//						Toast.makeText(getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
//						isConnected = false;
//						PosPrinter.this.device = null;
//						PosPrinter.this.socket = null;
//						dialog.hide();
//					}
//				});
//			}
//		}.start();
//	}
	public void connect() {
		if (!isEnabled()) {
			btService.requestBTActivation(activity, new BTService.BTCallback() {
				@Override
				public void onEnableBTResult(boolean isEnabled) {
					if (isEnabled) connect();
				}
			});

			return;
		}

		new SearchPrinterDialog(activity, btService) {

			@Override public void onCancelled() {}
		};
	}

	public void disconnect() {
//		TODO create thread for disconnect!
//		TODO create thread for check device state!
		btService.stop();
//		if (socket != null) {
//			try {
//				socket.close();
//				socket = null;
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		device = null;
//		if (printerEvents != null) printerEvents.onDisconnected();
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	public interface OnStateChangedListener {
		void onChanged(int state, Message msg);
	}
}
