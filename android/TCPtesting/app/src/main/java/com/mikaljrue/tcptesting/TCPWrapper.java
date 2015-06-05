package com.mikaljrue.tcptesting;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class TCPWrapper extends Activity  {
    private TextView text;
    Handler updateConversationHandler;
    TCPClient longDistClient = null;
    WifiManager wifiManager;

    private SensorManager mSensorManager;
    ShortDistance shortThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpwrapper);

        text = (TextView) findViewById(R.id.text2);
        updateConversationHandler = new Handler();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        scanNodes();


        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
         shortThread = new ShortDistance(wifiManager, mSensorManager,
                 new ShortDistance.DebugCallback() {
                     @Override
                     public void printString(String str) {

                         updateConversationHandler.post(new updateUIThread(str));
                     }
                 });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tcpwrapper, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    protected void onPause() {
        super.onPause();
        shortThread.onPause();
    }

    protected void onResume() {
        super.onPause();
        shortThread.onResume();
    }



    public void requestKeyPos(View view)
    {
        if (longDistClient == null)
            longDistClient = new TCPClient();
        Location loc = longDistClient.getKeyLocationEstimate();
        String report = "\nRead:" + loc.getProvider() + "\n\tlat: " + loc.getLatitude()
                + "\n\tlon: " + loc.getLongitude() + "\n\tAcc: " + loc.getAccuracy() + "m\n\ttime: " + loc.getTime();
        if (loc != null)
            updateConversationHandler.post(new updateUIThread(report));
        else
            updateConversationHandler.post(new updateUIThread("Retunreed null"));
    }

    public void requestMyPos(View view)
    {
        if (longDistClient == null)
            longDistClient = new TCPClient();
        Location loc = longDistClient.getMyLocationEstimate(wifiManager.getScanResults());
        if (loc != null) {
            String report = "\nRead:" + loc.getProvider() + "\n\tlat: " + loc.getLatitude()
                    + "\n\tlon: " + loc.getLongitude() + "\n\tAcc: " + loc.getAccuracy() + "m;";
            updateConversationHandler.post(new updateUIThread(report));
        } else
            updateConversationHandler.post(new updateUIThread("Retunreed null"));
    }

    public void scanNodesBtn(View view)
    {
        scanNodes();
    }

    private void scanNodes()
    {
        updateConversationHandler.post(new updateUIThread("Scanned wifi"));
        wifiManager.startScan();
    }
    class updateUIThread implements Runnable {
        private String msg;

        public updateUIThread(String str) {
            this.msg = str;
        }
        @Override
        public void run() {
            String str = msg + "\n" + text.getText().toString();
            text.setText(str);
        }
    }


    public void openWifiAP(View v) {
        //connectToAP();
    }


    public void turnOnWifi(View v) {
        connectToWifi();
    }

    private void createWifiAccessPoint() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
        boolean methodFound = false;
        for (Method method: wmMethods) {
            if (method.getName().equals("setWifiApEnabled")) {
                methodFound = true;
                WifiConfiguration netConfig = new WifiConfiguration();
                netConfig.SSID = "4011TEAMA";
                netConfig.allowedAuthAlgorithms.set(
                        WifiConfiguration.AuthAlgorithm.OPEN);
                try {
                    boolean apstatus = (Boolean) method.invoke(
                            wifiManager, netConfig, true);
                    for (Method isWifiApEnabledmethod: wmMethods) {
                        if (isWifiApEnabledmethod.getName().equals(
                                "isWifiApEnabled")) {
                            while (!(Boolean) isWifiApEnabledmethod.invoke(
                                    wifiManager)) {};
                            for (Method method1: wmMethods) {
                                if (method1.getName().equals(
                                        "getWifiApState")) {
                                    int apstate;
                                    apstate = (Integer) method1.invoke(
                                            wifiManager);
                                }
                            }
                        }
                    }
                    if (apstatus) {
                        Log.d("Wifi Switching",
                                "Access Point created");
                    } else {
                        Log.d("Wifi Switching",
                                "Access Point creation failed");
                    }

                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!methodFound) {
            Log.d("Splash Activity",
                    "cannot configure an access point");
        }
    }



    private void connectToWifi() {
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
        if (info != null)
            Log.v("Wifi_switching", "Finished turning on wifi, wifi SSID: " + info.getSSID());
    }


}
