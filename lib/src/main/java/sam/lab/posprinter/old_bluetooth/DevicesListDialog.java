package sam.lab.posprinter.old_bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.view.View;
import android.widget.ListView;

import java.util.List;

import sam.lab.posprinter.R;

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
public abstract class DevicesListDialog {

	private boolean callOnCancel = true;

	public DevicesListDialog(Activity activity, List<BluetoothDevice> pairedDevices) {
		final Dialog dialog = new Dialog(activity);
		dialog.setCancelable(true);

		@SuppressLint("InflateParams")
		View view = activity.getLayoutInflater().inflate(R.layout.simple_list, null, false);
		ListView listView = (ListView) view.findViewById(R.id.list);
		DevicesListAdapter adapter = new DevicesListAdapter(activity, pairedDevices);
		adapter.setOnDeviceSelectedListener(new UBluetooth.OnDeviceSelectedListener() {
			@Override
			public void onSelected(BluetoothDevice device) {
				onDeviceSelected(device);
				callOnCancel = false;
				dialog.dismiss();
			}

			@Override
			public void onCancel() {
			}
		});
		listView.setAdapter(adapter);

		dialog.setContentView(view);
		dialog.setTitle("Choose device");

		dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialogInterface) {
				if (callOnCancel)
					DevicesListDialog.this.onDismiss();
			}
		});

		dialog.show();
	}

	public void addDevice(BluetoothDevice device) {

	}

	public abstract void onDismiss();
	public abstract void onDeviceSelected(BluetoothDevice device);

}
