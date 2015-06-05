package com.mikaljrue.tcptesting;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * Created by mikaljrue on 3/06/2015.
 */
public class ShortDistance implements SensorEventListener {
    private Thread thread;
    private ServerSocketChannel serverSocketChannel;
    SocketChannel socketChannel;
    WifiManager wifiManager;

    private FileIO fileIO;

    float[] mGravity;
    float[] mGeomagnetic;

    public float getAzimut() {
        return azimut;
    }

    public float getRssi() {
        return rssi;
    }
    public interface DebugCallback {
        void printString(String str);
    }
    float azimut;
    float rssi;


    TCPClient shortDistClient = null;

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    protected void onResume() {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        mSensorManager.unregisterListener(this);
    }


    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
                    mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = (int) Math.round(Math.toDegrees(orientation[0]));
            }
        }
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    public ShortDistance(WifiManager wifiManagerPar, SensorManager sensorManagerPar, final DebugCallback call)
    {

        wifiManager = wifiManagerPar;
        mSensorManager = sensorManagerPar;
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        shortDistClient = new TCPClient();


        String dateStamp = (DateFormat.format("dd-MM-yyyy-hh-mm-ss", new java.util.Date()).toString());
        fileIO = new FileIO("/Android/data/com.CSSE4011.Lab7/files/",String.format("audio-%s.txt",dateStamp));


        thread = new Thread(new Runnable() {
            public void run ()
            {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    connectToAP();
                    fileIO.writeToFile(String.format("%d, %d\n", (int) azimut, (int)rssi));
                    call.printString(String.format("%d, %d\n", (int) azimut, (int)rssi));
                }

            }
        });
        thread.start();
    }


    private int connectToWifiConfig(WifiConfiguration wifiConfig) {
        int initialNetworkConfigId = wifiManager.getConnectionInfo().getNetworkId();
        int netId = wifiManager.addNetwork(wifiConfig);
        if (netId != initialNetworkConfigId) {
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();
        }
        return netId;
    }

    private void connectToAP() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo info = wifiManager.getConnectionInfo();
        while (!wifiManager.isWifiEnabled() && (
                ((info = wifiManager.getConnectionInfo()) == null) ||
                        info.getSSID() == null)
                )
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        WifiConfiguration config = new WifiConfiguration();
        config.SSID = "\"4011TEAMA\"";
        config.status = WifiConfiguration.Status.ENABLED;
        int newId = connectToWifiConfig(config);
        Log.v("Wifi: ", "trying to connect to wifi");
        while(wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED
                || wifiManager.getConnectionInfo().getNetworkId() != newId)
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        Log.v("Wifi: ", "Connected supposedly");

        final DhcpInfo dhcp = wifiManager.getDhcpInfo();
        final String address = Formatter.formatIpAddress(dhcp.gateway);
        Log.v("wifi connection", address);

        shortDistClient.sendHeartbeat(address);
        Log.v("Wifi Rssi ", wifiManager.getConnectionInfo().getRssi() + "");
        rssi = wifiManager.getConnectionInfo().getRssi();
        Log.v("Azimuth ", azimut + "");
    }

}
