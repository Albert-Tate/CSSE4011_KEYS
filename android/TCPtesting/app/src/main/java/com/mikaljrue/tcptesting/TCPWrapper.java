package com.mikaljrue.tcptesting;

import android.content.Context;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class TCPWrapper extends ActionBarActivity {

    private TextView text;
    Handler updateConversationHandler;
    TCPClient client = null;
    WifiManager wifi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpwrapper);

        text = (TextView) findViewById(R.id.text2);
        updateConversationHandler = new Handler();
        scanNodes();
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

    public void requestKeyPos(View view)
    {
        if (client == null)
            client = new TCPClient();
        Location loc = client.getKeyLocationEstimate();
        String report = "\nRead:" + loc.getProvider() + "\n\tlat: " + loc.getLatitude()
                + "\n\tlon: " + loc.getLongitude() + "\n\tAcc: " + loc.getAccuracy() + "m\n\ttime: " + loc.getTime();
        if (loc != null)
            updateConversationHandler.post(new updateUIThread(report));
        else
            updateConversationHandler.post(new updateUIThread("Retunreed null"));
    }

    public void requestMyPos(View view)
    {
        if (client == null)
            client = new TCPClient();
        Location loc = client.getMyLocationEstimate(wifi.getScanResults());
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
        if (wifi == null)
            wifi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        updateConversationHandler.post(new updateUIThread("Scanned wifi"));
        wifi.startScan();
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
}
