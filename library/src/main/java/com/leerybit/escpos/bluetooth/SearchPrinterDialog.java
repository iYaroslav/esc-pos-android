package com.leerybit.escpos.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.Set;

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
public abstract class SearchPrinterDialog {

  private final Activity activity;
  // protected BluetoothService bluetoothService;
  private final BTService service;
  private int insets;

  private LinearLayout availableDevicesContainer;
  private View progress;
  private Dialog dialog;

  private final HashMap<String, DeviceItemView> deviceItems;

  public SearchPrinterDialog(Activity activity, BTService service) {
    this.activity = activity;
    this.service = service;
    deviceItems = new HashMap<>();

    @SuppressLint("InflateParams")
    View view = LayoutInflater.from(activity).inflate(R.layout.serach_dialog_layout, null, false);

    insets = dpToPx(16);
    activity.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    activity.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_CLASS_CHANGED));
    activity.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_NAME_CHANGED));
    activity.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

    Set<BluetoothDevice> pairedDevices = service.getBondedDevices();

    availableDevicesContainer = (LinearLayout) view.findViewById(R.id.available_devices_container);
    progress = view.findViewById(R.id.search_devices_progress);
    if (pairedDevices.size() > 0) {
      fillPairedDevices(pairedDevices, (LinearLayout) view.findViewById(R.id.paired_devices_container));
    } else {
      view.findViewById(R.id.paired_devices).setVisibility(View.GONE);
    }

    service.startDiscovery();

    dialog = new Dialog(activity);
    dialog.setCancelable(true);
    dialog.setContentView(view);
    dialog.setTitle(R.string.pos_title_choose_printer);
    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        SearchPrinterDialog.this.onCancel();
      }
    });
    dialog.show();
  }

  private BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      if (BluetoothDevice.ACTION_FOUND.equals(action) || BluetoothDevice.ACTION_CLASS_CHANGED.equals(action) || BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
          View view = makeItem(device);
          if (view != null) availableDevicesContainer.addView(view);
        }
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        service.setState(BTService.STATE_NONE);
        progress.setVisibility(View.GONE);
      }
    }
  };

  private void fillPairedDevices(Set<BluetoothDevice> pairedDevices, LinearLayout view) {
    for (BluetoothDevice device : pairedDevices) {
      view.addView(makeItem(device));
    }
  }

  public int dpToPx(int dp) {
    DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
    return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
  }

  private DeviceItemView makeItem(final BluetoothDevice device) {
    boolean append;
    DeviceItemView view = deviceItems.get(device.getAddress());
    if (view != null) {
      append = false;
    } else {
      append = true;
      view = new DeviceItemView(activity);
    }

    int resId = R.drawable.ic_bluetooth_24dp;
    BluetoothClass cls = device.getBluetoothClass();
    cls.getMajorDeviceClass();
    Log.e("BTDeviceClass", cls.getMajorDeviceClass() + ", " + cls.getDeviceClass() + ", " + device.getName());
    switch (cls.getMajorDeviceClass()) {
      case BluetoothClass.Device.Major.PHONE:
        resId = R.drawable.ic_phone_24dp;
        break;
      case BluetoothClass.Device.Major.COMPUTER:
        resId = R.drawable.ic_laptop_24dp;
        break;
      case BluetoothClass.Device.Major.WEARABLE:
        if (cls.getDeviceClass() == BluetoothClass.Device.WEARABLE_WRIST_WATCH)
          resId = R.drawable.ic_watch_24dp;
        break;
      case BluetoothClass.Device.Major.TOY:
        if (cls.getDeviceClass() == BluetoothClass.Device.TOY_CONTROLLER)
          resId = R.drawable.ic_gamepad_24dp;
        break;
      case BluetoothClass.Device.Major.AUDIO_VIDEO:
        if (cls.getDeviceClass() == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES)
          resId = R.drawable.ic_headset_24dp;
      case BluetoothClass.Device.Major.IMAGING:
        if (cls.getDeviceClass() == 1664) resId = R.drawable.ic_print_24dp;
        break;
    }

    String name = device.getName();
    if (name == null || name.isEmpty()) {
      name = device.getAddress();
    }

    view.setInsets(insets, insets);
    view.setTitle(name);
    view.setIconResourceId(resId);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        service.connect(device);
        onFinish();
      }
    });

    deviceItems.put(device.getAddress(), view);
    return append ? view : null;
  }

  private void onCancel() {
    onFinish();
    onCancelled();
  }

  private void onFinish() {
    if (dialog != null) dialog.dismiss();
    service.cancelDiscovery();
    activity.unregisterReceiver(receiver);
  }

  public abstract void onCancelled();
//	public abstract void registerReceivers(IntentFilter actionFound, IntentFilter classChanged, IntentFilter nameChanged, IntentFilter actionDiscoveryFinished, BroadcastReceiver receiver);
}
