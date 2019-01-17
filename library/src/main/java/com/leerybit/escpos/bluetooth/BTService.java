package com.leerybit.escpos.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import com.leerybit.escpos.DeviceCallbacks;
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
public class BTService {

  ConnectThread connectThread;
  ListenThread listenThread;
  MainThread mainThread;

  private static final String TAG = "BTService";
  private static final String BT_NAME = "BTService";
  @SuppressWarnings("SpellCheckingInspection")
  private static final UUID BT_UUID = UUID.fromString("B3EDED23-82FB-4054-B24F-043966975FCA");
  private static final int REQUEST_ENABLE_BLUETOOTH = 0x0BBA;
  public static final int STATE_NONE = 0x00;
  public static final int STATE_LISTENING = 0x01;
  public static final int STATE_CONNECTING = 0x02;
  public static final int STATE_CONNECTED = 0x03;

  public static final int MESSAGE_STATE_CHANGE = 0x05;
  public static final int MESSAGE_READ = 0x06;
  public static final int MESSAGE_WRITE = 0x07;
  public static final int MESSAGE_CONNECTION_LOST = 0x08;
  public static final int MESSAGE_UNABLE_TO_CONNECT = 0x09;

  private final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
  private final Handler handler;
  private final Handler mainHandler;
  private final Context context;

  private int state;
  private BTCallback btCallback;
  private DeviceCallbacks deviceCallbacks;

  public BTService(Context context, Handler handler) {
    this.context = context;
    if (handler == null) {
      this.handler = new Handler();
    } else {
      this.handler = handler;
    }

    mainHandler = new Handler(Looper.getMainLooper());
    setState(STATE_NONE);
  }

  public synchronized boolean isAvailable() {
    return adapter != null;
  }

  public synchronized boolean isEnabled() {
    return isAvailable() && this.adapter.isEnabled();
  }

  public synchronized int getState() {
    return state;
  }

  void setState(int state) {
    setState(MESSAGE_STATE_CHANGE, state);
  }

  void setState(int message, int state) {
    this.state = state;
    handler.obtainMessage(-1, state, message).sendToTarget();
  }

  private void sendMessage(int message, int count, byte[] buffer) {
    handler.obtainMessage(count, getState(), message, buffer).sendToTarget();
  }

  public void setDeviceCallbacks(DeviceCallbacks callbacks) {
    this.deviceCallbacks = callbacks;
  }

  public synchronized boolean startDiscovery() {
    setState(STATE_LISTENING);
    return adapter.startDiscovery();
  }

  public synchronized boolean isDiscovering() {
    return adapter.isDiscovering();
  }

  public synchronized boolean cancelDiscovery() {
    return adapter.cancelDiscovery();
  }

  public synchronized Set<BluetoothDevice> getBondedDevices() {
    return adapter.getBondedDevices();
  }

  public synchronized void sendMessage(String message, String charset) {
    if (message.length() > 0) {
      byte[] send;
      try {
        send = message.getBytes(charset);
      } catch (UnsupportedEncodingException var5) {
        send = message.getBytes();
      }

      this.send(send);
      byte[] tail = new byte[]{(byte) 10, (byte) 13, (byte) 0};
      this.send(tail);
    }

  }

  public void send(byte[] out) {
    BTService.ListenThread r;
    synchronized (this) {
      if (this.state != 3) {
        return;
      }

      r = this.listenThread;
    }

    r.send(out);
  }

  @SuppressWarnings("UnusedParameters")
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_ENABLE_BLUETOOTH:
        if (btCallback != null)
          btCallback.onEnableBTResult(isEnabled());
        break;
    }
  }

  public void requestBTActivation(Activity activity, BTCallback callback) {
    btCallback = callback;

    Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    activity.startActivityForResult(turnOnIntent, REQUEST_ENABLE_BLUETOOTH);
  }

  private <T extends BaseThread> T cleanThread(T thread) {
    if (thread != null) {
      thread.cancel();
    }

    return null;
  }

  private void cancelAllThreads() {
    connectThread = cleanThread(connectThread);
    listenThread = cleanThread(listenThread);
    mainThread = cleanThread(mainThread);
  }

  public void start() {
    if (!isEnabled()) return;
    connectThread = cleanThread(connectThread);
    listenThread = cleanThread(listenThread);

    if (mainThread == null) {
      mainThread = new MainThread();
      mainThread.start();
    }
  }

  public void stop() {
    setState(STATE_NONE);
    cancelAllThreads();
  }

  public synchronized void connect(BluetoothDevice device) {
    listenThread = cleanThread(listenThread);
    if (state == STATE_CONNECTING && connectThread != null) {
      connectThread = cleanThread(connectThread);
    }

    connectThread = new ConnectThread(device);
    connectThread.start();
    setState(STATE_CONNECTING);
  }

  private synchronized void onDeviceConnected(BluetoothSocket socket, BluetoothDevice device) {
    cancelAllThreads();

    listenThread = new ListenThread(socket);
    listenThread.start();

    listenThread = new ListenThread(socket);
    listenThread.start();

    if (deviceCallbacks != null) {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          deviceCallbacks.onConnected();
        }
      });
    }
    Log.e(TAG, "Device " + device.getName() + " [" + device.getAddress() + "] — connected");
    this.setState(STATE_CONNECTED);
  }

  private void onConnectingFailed() {
    this.setState(STATE_NONE);
    setState(MESSAGE_UNABLE_TO_CONNECT, getState());

  }

  private void onConnectionLost() {
    setState(MESSAGE_CONNECTION_LOST, getState());
  }

  private abstract class BaseThread extends Thread {
    public abstract void cancel();
  }

  private class MainThread extends BaseThread {
    private final BluetoothServerSocket serverSocket;

    public MainThread() {
      Log.d(TAG, "Main thread created");
      BluetoothServerSocket tmp = null;

      try {
        tmp = adapter.listenUsingRfcommWithServiceRecord(BT_NAME, BT_UUID);
      } catch (IOException ignored) {
      }

      serverSocket = tmp;
    }

    public void run() {
      Log.d(TAG, "Main thread started");
      this.setName("BTMainThread");
      BluetoothSocket socket;

      while (BTService.this.getState() != STATE_CONNECTED) {
        try {
          socket = serverSocket.accept();
        } catch (IOException e) {
          break;
        }

        if (socket != null) {
          synchronized (BTService.this) {
            switch (BTService.this.getState()) {
              case STATE_NONE:
              case STATE_CONNECTED:
                try {
                  socket.close();
                } catch (IOException ignored) {
                }
                break;
              case STATE_LISTENING:
              case STATE_CONNECTING:
                onDeviceConnected(socket, socket.getRemoteDevice());
            }
          }
        }
      }
    }

    @Override
    public void cancel() {
      Log.d(TAG, "Main thread cancelled");
      try {
        this.serverSocket.close();
      } catch (IOException ignored) {
      }
    }
  }

  private class ConnectThread extends BaseThread {
    private final BluetoothSocket socket;
    private final BluetoothDevice device;
    private final ProgressDialog dialog;

    @SuppressWarnings("TryWithIdenticalCatches")
    public ConnectThread(@NonNull BluetoothDevice device) {
      Log.d(TAG, "Connect thread created");
      this.device = device;

      dialog = new ProgressDialog(context);
      showDialog();

      BluetoothSocket tmp = null;
      try {
        //noinspection RedundantArrayCreation, SpellCheckingInspection
        Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
        try {
          tmp = (BluetoothSocket) m.invoke(device, 1);
        } catch (InvocationTargetException ignored) {
        }
      } catch (IllegalAccessException ignored) {
      } catch (NoSuchMethodException ignored) {
      }

      socket = tmp;
    }

    @Override
    public void run() {
      Log.d(TAG, "Connect thread started");
      this.setName("BTConnectThread");
      adapter.cancelDiscovery();

      try {
        this.socket.connect();
      } catch (IOException e) {
        Log.e("BTService", "FAILED");
        e.printStackTrace();
        onConnectingFailed();
        sendFail();

        try {
          this.socket.close();
        } catch (IOException ignored) {
        }

        BTService.this.start();
        hideDialog();
        return;
      }

      synchronized (BTService.this) {
        connectThread = null;
      }

      onDeviceConnected(this.socket, this.device);
      hideDialog();
    }

    private void sendFail() {
      if (deviceCallbacks != null) {
        mainHandler.post(new Runnable() {
          @Override
          public void run() {
            deviceCallbacks.onFailure();
          }
        });
      }
    }

    private void showDialog() {
      String deviceName = device.getName();
      if (deviceName == null || deviceName.isEmpty()) deviceName = device.getAddress();
      String title = context.getString(R.string.pos_title_connecting_to) + " " + deviceName;
      dialog.setMessage(title);
      dialog.setCancelable(false);

      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          dialog.show();
        }
      });
    }

    private void hideDialog() {
      mainHandler.post(new Runnable() {
        @Override
        public void run() {
          dialog.hide();
        }
      });
    }

    @Override
    public void cancel() {
      Log.d(TAG, "Connect thread cancelled");
      try {
        socket.close();
      } catch (IOException ignored) {
      }
    }
  }

  private class ListenThread extends BaseThread {
    private final BluetoothSocket socket;
    private final InputStream inStream;
    private final OutputStream outStream;

    public ListenThread(@NonNull BluetoothSocket socket) {
      Log.d(TAG, "Listen thread created");
      this.socket = socket;

      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      try {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException ignored) {
      }

      this.inStream = tmpIn;
      this.outStream = tmpOut;
    }

    @Override
    public void run() {
      Log.d(TAG, "Listen thread started");
      this.setName("BTConnectedThread");

      try {
        while (true) {
          byte[] e = new byte[256];
          int bytes = inStream.read(e);
          if (bytes <= 0) {
            fail();
            break;
          }

          sendMessage(MESSAGE_READ, bytes, e);
        }
      } catch (IOException e) {
        fail();
      }
    }

    private void fail() {
      onConnectionLost();
      if (BTService.this.getState() != STATE_NONE) {
        BTService.this.start();
      }
    }

    public void send(byte[] buffer) {
      try {
        outStream.write(buffer);
        sendMessage(MESSAGE_WRITE, buffer.length, buffer);
      } catch (IOException ignored) {
      }
    }

    @Override
    public void cancel() {
      Log.d(TAG, "Listen thread cancelled");
      try {
        socket.close();
      } catch (IOException ignored) {
      }
    }
  }

  public interface BTCallback {
    void onEnableBTResult(boolean isEnabled);
  }
}
