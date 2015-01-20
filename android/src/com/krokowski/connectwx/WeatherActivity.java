package com.krokowski.connectwx;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.ServiceUnavailableException;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by todd on 1/14/15.
 */
public class WeatherActivity extends Activity {

    private LocationManager locationManager;

    private ConnectIQ connectIq;
    private IQDevice device;
    private IQApp app;

    private TextView description;
    private TextView location;
    private TextView lastUpdate;
    private TextView connectStatus;

    private static final String TAG = WeatherActivity.class.getSimpleName();

    private List<IQDevice> deviceList;



    ConnectIQ.ConnectIQListener listener = new ConnectIQ.ConnectIQListener() {
        @Override
        public void onSdkReady() {
            Log.d(TAG, "ConnectIQ SDK is ready");
            initDevices();
        }

        @Override
        public void onInitializeError(ConnectIQ.IQSdkErrorStatus iqSdkErrorStatus) {
            Log.d(TAG, "Error initializing SDK " + iqSdkErrorStatus.toString());
        }

        @Override
        public void onSdkShutDown() {
            connectStatus.setText("Shutting down");
            connectStatus.setTextColor(Color.RED);
        }
    };

    ConnectIQ.IQDeviceEventListener eventListener = new ConnectIQ.IQDeviceEventListener() {
        @Override
        public void onDeviceStatusChanged(IQDevice iqDevice, IQDevice.IQDeviceStatus iqDeviceStatus) {
            updateStatus(iqDeviceStatus);
        }
    };

    ConnectIQ.IQApplicationEventListener appEventListener = new ConnectIQ.IQApplicationEventListener() {
        @Override
        public void onMessageReceived(IQDevice iqDevice, IQApp iqApp, List<Object> list, ConnectIQ.IQMessageStatus iqMessageStatus) {
            // data coming from garmin device
        }
    };

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.main);

        description = (TextView) findViewById(R.id.description);
        location = (TextView) findViewById(R.id.location);
        lastUpdate = (TextView) findViewById(R.id.lastUpdate);
        connectStatus = (TextView)findViewById(R.id.connectStatus);

        // Get an instance of ConnectIQ that does BLE simulation over ADB to the simulator.
        connectIq = ConnectIQ.getInstance(ConnectIQ.IQCommProtocol.SIMULATED_BLE);

        app = new IQApp("", "ConnectWx App", 1);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                sendToWxUpdateToGarmin(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d(TAG, "Location status changed " + status + " provider " + provider);
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.w(TAG, "Location provider is disabled");
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        connectIq.initialize(this, true, listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /**
         * Shutdown the SDK so resources and listeners can be released.
         */
        if (isFinishing()) {
            connectIq.shutdown();
        } else {
            /**
             * Unregister for all events.  This is good practice to clean up to
             * allow the SDK to free resources and not listen for events that
             * no one is interested in.
             *
             * We do not call this if we are shutting down because the shutdown
             * method will call this for us during the clean up process.
             */
            connectIq.unregisterAllForEvents();
        }
    }

    private void updateStatus(IQDevice.IQDeviceStatus status) {
        Log.d(TAG, "Updating TextView status");
        switch(status) {
            case CONNECTED:
                connectStatus.setText("Connected");
                connectStatus.setTextColor(Color.GREEN);
                break;
            case NOT_CONNECTED:
                connectStatus.setText("Not Connected");
                connectStatus.setTextColor(Color.RED);
                break;
            case NOT_PAIRED:
                connectStatus.setText("Not Connected");
                connectStatus.setTextColor(Color.RED);
                break;
            case UNKNOWN:
                connectStatus.setText("Unknown");
                connectStatus.setTextColor(Color.RED);
                break;
        }
    }

    private void sendToWxUpdateToGarmin(final Location location) {

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            GetWeatherTask asyncTask = new GetWeatherTask() {
                                @Override
                                protected void onPostExecute(JSONObject json) {
                                    super.onPostExecute(json);
                                    if (json == null)
                                        return;

                                    // Send JSON string to Garmin device.
                                    try {
                                        for (IQDevice device : deviceList) {
                                            Log.d(TAG, "Sending to device " + device.getFriendlyName());
                                            String descriptionStr = ((JSONObject)json.getJSONArray("weather").get(0)).getString("description");
                                            String locationStr = json.getString("name");

                                            String dateTimeStr = DateFormat.getDateTimeInstance().format(new Date());

                                            WeatherActivity.this.location.setText(locationStr);
                                            WeatherActivity.this.description.setText(descriptionStr);
                                            WeatherActivity.this.lastUpdate.setText(dateTimeStr);

                                            connectIq.sendMessage(device, app, descriptionStr + "," + locationStr);
                                        }
                                    } catch(JSONException je) {
                                        Log.e(TAG, "Could not parse JSON " + json.toString(), je);
                                    }
                                }
                            };

                            if(deviceList == null || deviceList.size() == 0)
                                initDevices();

                            asyncTask.execute(location);
                        } catch (Exception e) {
                            Log.e(TAG, "Error scheduling JSON fetch for wx data", e);
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 50000); //execute in every 50000 ms
    }

    private void initDevices() {
        deviceList = connectIq.getAvailableDevices();

        try {
            for (IQDevice device : deviceList) {
                Log.d(TAG, "Found device " + device.getFriendlyName() + " and registering for events");
                connectIq.registerForEvents(device, eventListener, app, appEventListener);

                IQDevice.IQDeviceStatus status = connectIq.getStatus(device);
                updateStatus(status);
            }
        } catch(IllegalStateException ise) {
            Log.e(TAG, "Device connection error", ise);
        } catch(ServiceUnavailableException sue) {
            Log.e(TAG, "Device unavailable", sue);
        }
    }
}
