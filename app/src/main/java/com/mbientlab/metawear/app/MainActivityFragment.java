/*
 * Copyright 2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user who
 * downloaded the software, his/her employer (which must be your employer) and
 * MbientLab Inc, (the "License").  You may not use this Software unless you
 * agree to abide by the terms of the License which can be found at
 * www.mbientlab.com/terms . The License limits your use, and you acknowledge,
 * that the  Software may not be modified, copied or distributed and can be used
 * solely and exclusively in conjunction with a MbientLab Inc, product.  Other
 * than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this
 * Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * MBIENTLAB OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT, NEGLIGENCE,
 * STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED
 * TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST
 * PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY
 * DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact MbientLab Inc, at www.mbientlab.com.
 */

package com.mbientlab.metawear.app;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.github.mikephil.charting.data.Entry;
import com.mbientlab.metawear.AsyncDataProducer;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.android.BtleService;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.builder.RouteComponent;
import com.mbientlab.metawear.data.SensorOrientation;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.AccelerometerBosch;
import com.mbientlab.metawear.module.AccelerometerMma8452q;
import com.mbientlab.metawear.module.Debug;
import com.mbientlab.metawear.module.Switch;

import java.util.ArrayList;
import java.util.HashMap;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements ServiceConnection {
    public static final int REQUEST_START_APP= 1;
    private final HashMap<DeviceState, MetaWearBoard> stateToBoards;
    private ArrayList<BluetoothDevice> btDevices;
    private BtleService.LocalBinder binder;

    private ConnectedDevicesAdapter connectedDevices= null;

    private boolean patientfrag = true;

    public MainActivityFragment() {
        stateToBoards = new HashMap<>();
        btDevices = new ArrayList<>();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity owner= getActivity();
        owner.getApplicationContext().bindService(new Intent(owner, BtleService.class), this, Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().getApplicationContext().unbindService(this);
    }


    public void addNewDevice(BluetoothDevice btDevice) {
        final DeviceState newDeviceState= new DeviceState(btDevice);
        final MetaWearBoard newBoard= binder.getMetaWearBoard(btDevice);

        newDeviceState.connecting= true;
        connectedDevices.add(newDeviceState);
        stateToBoards.put(newDeviceState, newBoard);
        btDevices.add(btDevice);

        if(patientfrag) {
            // ...

        }
        else {
//        final Capture<AsyncDataProducer> orientCapture = new Capture<>();
//        final Capture<Accelerometer> accelCapture = new Capture<>();
//
//        newBoard.onUnexpectedDisconnect(status -> getActivity().runOnUiThread(() -> connectedDevices.remove(newDeviceState)));
//        newBoard.connectAsync().onSuccessTask(task -> {
//            getActivity().runOnUiThread(() -> {
//                newDeviceState.connecting= false;
//                connectedDevices.notifyDataSetChanged();
//            });
//
//            final Accelerometer accelerometer = newBoard.getModule(Accelerometer.class);
//            accelCapture.set(accelerometer);
//
//            final AsyncDataProducer orientation;
//            if (accelerometer instanceof AccelerometerBosch) {
//                orientation = ((AccelerometerBosch) accelerometer).orientation();
//            } else {
//                orientation = ((AccelerometerMma8452q) accelerometer).orientation();
//            }
//            orientCapture.set(orientation);
//
//            return orientation.addRouteAsync(source -> source.stream((data, env) -> {
//                getActivity().runOnUiThread(() -> {
//                    newDeviceState.deviceOrientation = data.value(SensorOrientation.class).toString();
//                    connectedDevices.notifyDataSetChanged();
//                });
//            }));
//        }).onSuccessTask(task -> newBoard.getModule(Switch.class).state().addRouteAsync(source -> source.stream((Subscriber) (data, env) -> {
//            getActivity().runOnUiThread(() -> {
//                newDeviceState.pressed = data.value(Boolean.class);
//                connectedDevices.notifyDataSetChanged();
//            });
//        }))).continueWith((Continuation<Route, Void>) task -> {
//            if (task.isFaulted()) {
//                if (!newBoard.isConnected()) {
//                    getActivity().runOnUiThread(() -> connectedDevices.remove(newDeviceState));
//                } else {
//                    Snackbar.make(getActivity().findViewById(R.id.activity_main_layout), task.getError().getLocalizedMessage(), Snackbar.LENGTH_SHORT).show();
//                    newBoard.tearDown();
//                    newBoard.disconnectAsync().continueWith((Continuation<Void, Void>) task1 -> {
//                        connectedDevices.remove(newDeviceState);
//                        return null;
//                    });
//                }
//            } else {
//                orientCapture.get().start();
//                accelCapture.get().start();
//            }
//            return null;
//        });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        connectedDevices= new ConnectedDevicesAdapter(getActivity(), R.id.metawear_status_layout);
        connectedDevices.setNotifyOnChange(true);
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ListView connectedDevicesView= (ListView) view.findViewById(R.id.connected_devices);
        connectedDevicesView.setAdapter(connectedDevices);

        if(patientfrag) {
            connectedDevicesView.setOnItemLongClickListener((parent, view1, position, id) -> {
                Intent intent = new Intent(getActivity(), NavigationActivity.class);
                intent.putExtra(NavigationActivity.EXTRA_BT_DEVICE, btDevices.get(position));
                startActivityForResult(intent, REQUEST_START_APP);
                return false;
            });
        }
        else {
            connectedDevicesView.setOnItemLongClickListener((parent, view1, position, id) -> {
                DeviceState current = connectedDevices.getItem(position);
                final MetaWearBoard selectedBoard = stateToBoards.get(current);

                Accelerometer accelerometer = selectedBoard.getModule(Accelerometer.class);
                accelerometer.stop();
                if (accelerometer instanceof AccelerometerBosch) {
                    ((AccelerometerBosch) accelerometer).orientation().stop();
                } else {
                    ((AccelerometerMma8452q) accelerometer).orientation().stop();
                }

                selectedBoard.tearDown();
                selectedBoard.getModule(Debug.class).disconnectAsync();

                connectedDevices.remove(current);
                return false;
            });
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        binder= (BtleService.LocalBinder) service;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
