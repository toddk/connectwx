package com.krokowski.connectwx;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.garmin.android.connectiq.ConnectIQ;
import com.garmin.android.connectiq.IQApp;
import com.garmin.android.connectiq.IQDevice;

import java.util.List;

/**
 * Created by todd on 1/14/15.
 */
public class WeatherActivity extends Activity {

    private LocationManager locationManager;

    private ConnectIQ connectIq;
    private IQDevice device;
    private IQApp app;

    ConnectIQ.ConnectIQListener listener = new ConnectIQ.ConnectIQListener() {
        @Override
        public void onSdkReady() {
            List<IQDevice> deviceList = connectIq.getAvailableDevices();

            for (IQDevice device : deviceList) {
                connectIq.registerForEvents(device, eventListener, app, appEventListener);
            }
        }

        @Override
        public void onInitializeError(ConnectIQ.IQSdkErrorStatus iqSdkErrorStatus) {

        }

        @Override
        public void onSdkShutDown() {

        }
    };

    ConnectIQ.IQDeviceEventListener eventListener = new ConnectIQ.IQDeviceEventListener() {
        @Override
        public void onDeviceStatusChanged(IQDevice iqDevice, IQDevice.IQDeviceStatus iqDeviceStatus) {

        }
    };

    ConnectIQ.IQApplicationEventListener appEventListener = new ConnectIQ.IQApplicationEventListener() {
        @Override
        public void onMessageReceived(IQDevice iqDevice, IQApp iqApp, List<Object> list, ConnectIQ.IQMessageStatus iqMessageStatus) {

        }
    };

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        setContentView(R.layout.main);

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
