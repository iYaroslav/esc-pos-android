package sam.lab.posprinter.old_bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

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
public class DevicesListAdapter extends BaseAdapter {

	private List<BluetoothDevice> pairedDevices;
	private LayoutInflater inflater;
	private UBluetooth.OnDeviceSelectedListener listener;
//	private ArrayList<BluetoothDevice> visibleDevices;

	public DevicesListAdapter(Activity activity, List<BluetoothDevice> pairedDevices) {
		inflater = LayoutInflater.from(activity);
		this.pairedDevices = pairedDevices;
//		visibleDevices = new ArrayList<BluetoothDevice>();
	}

	@Override
	public int getCount() {
//		int size = pairedDevices.size();
//		int ad = size == 0 ? 0 : 1;
//		return size + visibleDevices.size() + ad;
		return pairedDevices.size();
	}

	@Override
	public BluetoothDevice getItem(int index) {
//		if (index == pairedDevices.size())
//			return null;
//
//		if (index < pairedDevices.size()) {
//
//		}

		return pairedDevices.get(index);
	}

	@Override
	public long getItemId(int index) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup root) {
		Holder holder;

		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_1, root, false);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.fill(position, getItem(position));
		return convertView;
	}


	private class Holder {

		private TextView textView;

		private Holder(View view) {
			textView = (TextView) view.findViewById(android.R.id.text1);
		}

		public void fill(final int position, final BluetoothDevice device) {
			textView.setText(device.getName());
			textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (listener != null) {
						listener.onSelected(device);
					}
				}
			});
		}

	}

	public void setOnDeviceSelectedListener(UBluetooth.OnDeviceSelectedListener listener) {
		this.listener = listener;
	}
}
