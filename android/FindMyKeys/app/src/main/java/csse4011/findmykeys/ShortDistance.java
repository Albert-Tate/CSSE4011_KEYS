package csse4011.findmykeys;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
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
import java.util.List;

/**
 * Created by mikaljrue on 3/06/2015.
 */
public class ShortDistance implements SensorEventListener {
    private Thread thread;
    Context app;
    private ServerSocketChannel serverSocketChannel;
    SocketChannel socketChannel;
    WifiManager wifiManager;
    final DebugCallback callb;
    private FileIO fileIO;
    private RssiDsp datastore;
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
    private float azimut;
    float rssi;


    UDPClient shortDistClient = null;

    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;

    public float getCompass() {
        return this.azimut;
    }

    protected void onResume(Context app) {
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

        app.registerReceiver(this.myWifiReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        app.registerReceiver(this.myRssiChangeReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    protected void onPause() {
        mSensorManager.unregisterListener(this);
        app.unregisterReceiver(this.myWifiReceiver);
        app.unregisterReceiver(this.myRssiChangeReceiver);
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

                azimut = 180-compassHeading(orientation[0], orientation[1], orientation[2]);
//                Log.d("azimut", (orientation[0] / Math.PI ) * 180 + "");
//                Log.d("azimut1", azimut + "");
            }
        }
    }



    private int compassHeading(float alpha, float beta, float gamma ) {
        double degtorad = Math.PI / 180; // Degree-to-Radian conversion

        double _x = beta; // beta value
        double _y = gamma; // gamma value
        double _z = alpha; // alpha value

        double cX = Math.cos( _x );
        double cY = Math.cos( _y );
        double cZ = Math.cos( _z );
        double sX = Math.sin( _x );
        double sY = Math.sin( _y );
        double sZ = Math.sin( _z );

        // Calculate Vx and Vy components
        double Vx = - cZ * sY - sZ * sX * cY;
        double Vy = - sZ * sY + cZ * sX * cY;

        // Calculate compass heading
        double compassHeading = Math.atan( Vx / Vy );

        // Convert compass heading to use whole unit circle
        if( Vy < 0 ) {
            compassHeading += Math.PI;
        } else if( Vx < 0 ) {
            compassHeading += 2 * Math.PI;
        }

        return (int) Math.round((compassHeading * ( 180 / Math.PI )));

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    public ShortDistance(Context appv, WifiManager wifiManagerPar, SensorManager sensorManagerPar, final DebugCallback call)
    {
        app = appv;
        callb = call;
        wifiManager = wifiManagerPar;
        mSensorManager = sensorManagerPar;
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        shortDistClient = new UDPClient();

        app.registerReceiver(this.myWifiReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        app.registerReceiver(this.myRssiChangeReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        String dateStamp = (DateFormat.format("dd-MM-yyyy-hh-mm-ss", new java.util.Date()).toString());
//        fileIO = new FileIO("/Android/data/com.CSSE4011.Lab7/files/",String.format("audio-%s.txt",dateStamp));
        datastore = new RssiDsp();


        wifiManager.startScan();

    }

    public int processData() {
        return datastore.process();
    }

    public void resetData() {
        datastore = new RssiDsp();
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

        //shortDistClient.sendHeartbeat(address);
    }


    private BroadcastReceiver myRssiChangeReceiver
            = new BroadcastReceiver(){

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            int newRssi = arg1.getIntExtra(WifiManager.EXTRA_NEW_RSSI, 0);
            //textRssi.setText(String.valueOf(newRssi));

            Log.v("Wifi Rssi ", rssi + "");
            Log.v("Azimuth ", azimut + "");


            List<ScanResult> wifiList = wifiManager.getScanResults();
            for (ScanResult info: wifiList)
                if (info.SSID.contentEquals("4011TEAMA"))
                {
                    Log.v("SSID ", info.SSID);
                    rssi = info.level;
                    datastore.add((int) azimut, (int) rssi);
//                    fileIO.writeToFile(String.format("%d, %d\n", (int) azimut, (int) rssi));
                    callb.printString(String.format("%d", (int) azimut));

                }

            wifiManager.startScan();
        }};

    private BroadcastReceiver myWifiReceiver
            = new BroadcastReceiver(){

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            NetworkInfo networkInfo = (NetworkInfo) arg1.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI){
                //DisplayWifiState();
            }
        }};


}
