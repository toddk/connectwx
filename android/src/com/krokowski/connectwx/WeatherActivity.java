package com.krokowski.connectwx;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;
import com.garmin.android.connectiq.exception.ServiceUnavailableException;

import java.util.List;

/**
 * Created by todd on 1/14/15.
 */
public class WeatherActivity extends Activity {

    private LocationManager locationManager;

    private ConnectIQ connectIq;
    private IQDevice device;
    private IQApp app;

    private TextView connectStatus;

    private static final String TAG = WeatherActivity.class.getSimpleName();

    ConnectIQ.ConnectIQListener listener = new ConnectIQ.ConnectIQListener() {
        @Override
        public void onSdkReady() {
            List<IQDevice> deviceList = connectIq.getAvailableDevices();

            try {
                for (IQDevice device : deviceList) {
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

        @Override
        public void onInitializeError(ConnectIQ.IQSdkErrorStatus iqSdkErrorStatus) {

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

        connectStatus = (TextView)findViewById(R.id.connectStatus);

        // Get an instance of ConnectIQ that does BLE simulation over ADB to the simulator.
        connectIq = ConnectIQ.getInstance(ConnectIQ.IQCommProtocol.SIMULATED_BLE);
        connectIq.setAdbPort(7381);

        app = new IQApp("", "ConnectWx App", 1);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                sendToWxUpdateToGarmin(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        Button connect = (Button)findViewById(R.id.connectButton);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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

    private void sendToWxUpdateToGarmin(Location location) {
        GetWeatherTask asyncTask = new GetWeatherTask() {
            @Override
            protected void onPostExecute(String jsonString) {
                super.onPostExecute(jsonString);

                // TODO: strip out details in JSON that are irrelevant before sending
                // Send JSON string to Garmin device.
            }
        };
    }
}
