package csse4011.findmykeys;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.os.Handler;


public class WebViewActivity extends Activity {


    private String TAG = "WebViewActivity";
    private WebView myWebView;
    private String AR_URL2 = "http://cardboard.kissr.com";
    private String AR_URL1 = "file:///android_asset/www/index.html";
    private String AR_URL3 = "http://www.google.com";

    private GoogleMap mMap;
    private LatLng UQ = new LatLng(-27.494908, 153.012023);
    private Location myLocation;

    private SensorManager mSensorManager;
    ShortDistance shortThread;
    WifiManager wifiManager;
    TextView keyDegreeText;
    TextView myAngleText;
    Thread keyDegreeThread;
    private Handler mHandler;

    private boolean offsetisSet = false;
    private int offset = 0;
    private int calibration = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        getActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mHandler = new Handler();
        setupMap();
        setupShortDistance();
        setupWebView();
        createLocationListener();
    }



    private void setupShortDistance(){
        keyDegreeText = (TextView) findViewById(R.id.keyDegree);
        myAngleText = (TextView) findViewById(R.id.myAngle);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        shortThread = new ShortDistance(this, wifiManager, mSensorManager, new ShortDistance.DebugCallback() {
            @Override
            public void printString(String str) {
//                mHandler.post(new updateUIThread1(str));
            }
        });
        keyDegreeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int degree = shortThread.processData();
                    if (Math.abs(degree) > 181) {
                        Log.d("keyDegree", "wrap wrong "+ degree);
                        return;
                    }
                    if (degree != -181) {
                        if (degree < 0) {
                            degree = degree + 360;
                        }
                        mHandler.post(new updateUIThread(degree));
                        Log.d("keyDegree", "Key Degree " + degree);
                        shortThread.resetData();
                    } else {
                        mHandler.post(new updateUIThread(-181));
                        Log.d("keyDegree", "processing");

                    }
//                    if (!offsetisSet) {
//                        offset = Math.round(shortThread.getCompass());
//                        Log.d("offset", offset + "");
//                        offsetisSet = true;
//                    }
                }
            }
        });

        keyDegreeThread.start();

    }

    class updateUIThread implements Runnable {
        private int msg;

        public updateUIThread(int angle) {
            this.msg = angle;
        }

        @Override
        public void run() {
            if(msg == -181 && keyDegreeText.getText().equals("busy")) {
                keyDegreeText.setText("busy");
            } else if (msg != -181){
                int tmpAngle = msg - offset;
                if (tmpAngle < 0) {
                    tmpAngle += 360;
                }
                keyDegreeText.setText(msg + 60 + " " + getDirFromAngle(msg+60));
                myWebView.loadUrl("javascript:setData(\"" + tmpAngle + "\");");
            }


        }
    }

    private String getDirFromAngle(int angle) {
        String dir = "N";

        if (angle > 337  || angle <= 22) {
            dir = "N";
        } else if (angle > 22  && angle <= 67) {
            dir = "NE";
        } else if (angle > 67  && angle <= 112) {
            dir = "E";
        } else if (angle > 112  && angle <= 157) {
            dir = "SE";
        } else if (angle > 157  && angle <= 202) {
            dir = "S";
        } else if (angle > 202  && angle <= 247) {
            dir = "SW";
        } else if (angle > 247  && angle <= 292) {
            dir = "W";
        } else if (angle > 292  && angle <= 337) {
            dir = "NW";
        }

        return dir;
    }

    class updateUIThread1 implements Runnable {
        private String msg;

        public updateUIThread1(String angle) {
            this.msg = angle;
        }

        @Override
        public void run() {
            int myAngle = Integer.parseInt(msg, 10);
            if (myAngle < 0) {
                myAngle = myAngle + 360;
            }
            Log.d("myAngle","myAngle " + myAngle);
            myAngleText.setText(myAngle + " " + getDirFromAngle(myAngle));
        }
    }

    private void setupMap() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

//        float zoom = getIntent().getFloatExtra("zoom", 18.0f);

//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UQ, zoom));
//        mMap.addMarker(new MarkerOptions().position(UQ));

    }

    private void createLocationListener() {

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (location == myLocation) {
                    return;
                }
                myLocation = location;

                if (mMap != null) {
                    mMap.clear();
                    if (myLocation != null) {
//                        mMap.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).title("phone").snippet("Phone GPS"));
//                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 16));
//                        mMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);

                        MarkerOptions marker = new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()));
                        marker.title("Current location");
                        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                        mMap.addMarker(marker);

                        float compassBearing = shortThread.getCompass();
                        if(compassBearing < 0) {
                            compassBearing = compassBearing + 360;
                        }
                        Log.d("myAngle", "myAngle " + compassBearing);
                        int tmpAngle = Math.round(compassBearing) - offset;
                        if (tmpAngle < 0) {
                            tmpAngle += 360;
                        }

                        myAngleText.setText(compassBearing + " " + getDirFromAngle(Math.round(compassBearing)));
                        if (calibration < 3) {
                            offset = Math.round(shortThread.getCompass());
                            myWebView.loadUrl("javascript:setData(\"" + tmpAngle + "\");");
                            myAngleText.setText("Calibrating offset- " + offset);
                            calibration += 1;
                            shortThread.resetData();
                        }



//                        mMap.addMarker(new MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
//                                .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
//                                .flat(true)
//                                .rotation((float)0-90-compassBearing));

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to Mountain View
                                        .zoom((float)15.5)                   // Sets the zoom
                                        .bearing(compassBearing)                // Sets the orientation of the camera to east
                                        .tilt((float) 67.5)                   // Sets the tilt of the camera to 30 degrees
                                        .build();                   // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(), 16));
                    }
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void setupWebView() {
        myWebView = (WebView) findViewById(R.id.webview);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        myWebView.setFocusable(false);
        myWebView.setFocusableInTouchMode(false);
        myWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        myWebView.addJavascriptInterface(new WebViewActivity.WebAppInterface(this), "Android");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        myWebView.setWebViewClient(new MyWebViewClient());
        Log.i("klWebViewActivity", "UA: " + myWebView.getSettings().getUserAgentString());

        myWebView.setWebChromeClient(new WebChromeClient() {
            // Need to accept permissions to use the camera and audio
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                Log.d(TAG, "onPermissionRequest");
                WebViewActivity.this.runOnUiThread(new Runnable() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        // Make sure the request is coming from our file
                        // Warning: This check may fail for local files
//                        if (request.getOrigin().toString().equals(AR_URL1)) {
                        Log.d("requestl", "kl" + request.getOrigin().toString());
                        request.grant(request.getResources());
//                        } else {
//                            request.deny();
//                        }
                    }
                });
            }
        });
        myWebView.loadUrl(AR_URL1);
//        offset = Math.round(shortThread.getCompass());

    }

//    @Override
//    public void onBackPressed() {
//        if(myWebView.canGoBack()) {
//            myWebView.goBack();
//        } else {
//            myWebView.getSettings().setJavaScriptEnabled(false);
//            super.onBackPressed();
//        }
//    }

    @Override
    public void onPause() {
        myWebView.getSettings().setJavaScriptEnabled(false);
        super.onPause();
        shortThread.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        myWebView.getSettings().setJavaScriptEnabled(true);
        shortThread.onResume(this);
    }

    @Override
    public void onDestroy() {

        myWebView.getSettings().setJavaScriptEnabled(false);
        super.onDestroy();
    }


    public class WebAppInterface {
        Context mContext;

        /** Instantiate the interface and set the context */
        WebAppInterface(Context c) {
            mContext = c;
        }

        /** Show a toast from the web page */
        @JavascriptInterface
        public void showToast(String toast) {
            Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
        }
    }
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            if (Uri.parse(url).getHost().equals("www.example.com")) {
//                // This is my web site, so do not override; let my WebView load the page
                return false;
//            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
//            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//            startActivity(intent);
//            return true;
        }
    }



}
