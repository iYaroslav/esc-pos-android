package sam.lab.posprinter.old_bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
public class UBluetooth {

	private static final String TAG = "UBluetooth";

	private static final int ACTION_CHOOSE_DEVICE_DIALOG = 0;
	public static final int REQUEST_ENABLE_BLUETOOTH = 1715;

	private static OnDeviceSelectedListener savedDeviceSelectedListener;
	private static int lastAction;

	public static BluetoothAdapter getAdapter() {
		return BluetoothAdapter.getDefaultAdapter();
	}

	public static void requestBluetoothActivation(Activity activity) {
		Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		activity.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BLUETOOTH);
	}

	public static List<BluetoothDevice> getListOfPairedDevices() {
		Set<BluetoothDevice> pairedDevices = getAdapter().getBondedDevices();
		ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
		for(BluetoothDevice device : pairedDevices) {
			devices.add(device);
		}

		return devices;
	}

	public static void connectToDevice(Activity activity, final BluetoothDevice device, final OnDeviceConnected callback) {
		final Handler mainThread = new Handler(Looper.getMainLooper());
		final ProgressDialog dialog = new ProgressDialog(activity);
		dialog.setMessage("Pairing with " + device.getName());
		dialog.setCancelable(false);
		dialog.show();

		new Thread() {

			@Override
			public void run() {
				super.run();

				try {
					try {
						Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
						try {
							final BluetoothSocket socket = (BluetoothSocket) m.invoke(device, Integer.valueOf(1));
							socket.connect();
							Log.d(TAG, device.getName() + " - connected");
							mainThread.post(new Runnable() {
								@Override
								public void run() {
									callback.success(device, socket);
									dialog.hide();
								}
							});
							return;
						} catch (IOException e) {
							Log.e(TAG, "IOException: " + e.getLocalizedMessage());
							Log.d(TAG, device.getName() + " - not connected");
						}
					} catch (IllegalArgumentException e) {
						Log.e(TAG, "IllegalArgumentException: " + e.getLocalizedMessage());
					} catch (IllegalAccessException e) {
						Log.e(TAG, "IllegalAccessException: " + e.getLocalizedMessage());
					} catch (InvocationTargetException e) {
						Log.e(TAG, "InvocationTargetException: " + e.getLocalizedMessage());
					}
				} catch (SecurityException e) {
					Log.e(TAG, "SecurityException: " + e.getLocalizedMessage());
				} catch (NoSuchMethodException e) {
					Log.e(TAG, "NoSuchMethodException: " + e.getLocalizedMessage());
				}

				mainThread.post(new Runnable() {
					@Override
					public void run() {
						callback.failure();
						dialog.hide();
					}
				});
			}
		}.start();
	}


	public static void chooseDeviceDialog(Activity activity, final OnDeviceSelectedListener listener) {
		if (!getAdapter().isEnabled()) {
			savedDeviceSelectedListener = listener;
			lastAction = ACTION_CHOOSE_DEVICE_DIALOG;
			requestBluetoothActivation(activity);
			return;
		}

//		TODO register receiver!

		DevicesListDialog dialog = new DevicesListDialog(activity, getListOfPairedDevices()) {

			@Override
			public void onDismiss() {
				if (listener != null)
					listener.onCancel();
			}

			@Override
			public void onDeviceSelected(BluetoothDevice device) {
				if (listener != null)
					listener.onSelected(device);
			}
		};
//		dialog.addDevice();

//		TODO! savedDeviceSelectedListener = null;
	}

	public static void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BLUETOOTH:
				if(!getAdapter().isEnabled())
					return;

				switch (lastAction) {
					case ACTION_CHOOSE_DEVICE_DIALOG:
						if (savedDeviceSelectedListener != null)
							chooseDeviceDialog(activity, savedDeviceSelectedListener);
						break;
				}

				break;
		}
	}

	public interface OnDeviceSelectedListener {
		void onSelected(BluetoothDevice device);
		void onCancel();
	}
	public interface OnDeviceConnected {
		void success(BluetoothDevice device, BluetoothSocket socket);
		void failure();
	}
}
