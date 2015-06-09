package csse4011.findmykeys;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;


public class WebViewActivity extends Activity {


    private String TAG = "WebViewActivity";
    private WebView myWebView;
    private String AR_URL2 = "http://cardboard.kissr.com";
    private String AR_URL1 = "file:///android_asset/www/index.html";
    private String AR_URL3 = "http://www.google.com";

    private GoogleMap mMap;
    private GoogleMap mMap1;
    private LatLng UQ = new LatLng(-27.494908, 153.012023);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        getActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        setupMap();
        setupWebView();
        myWebView.loadUrl("javascript:hello();");

    }

    private void setupMap() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap1 = ((MapFragment) getFragmentManager().findFragmentById(R.id.map1)).getMap();

        float zoom = getIntent().getFloatExtra("zoom", 18.0f);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UQ, zoom));
        mMap.addMarker(new MarkerOptions().position(UQ));

        mMap1.moveCamera(CameraUpdateFactory.newLatLngZoom(UQ, zoom));
        mMap1.addMarker(new MarkerOptions().position(UQ));
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
                        Log.d("requestl","kl"+ request.getOrigin().toString());
                        request.grant(request.getResources());
//                        } else {
//                            request.deny();
//                        }
                    }
                });
            }
        });
        myWebView.loadUrl(AR_URL1);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        myWebView.getSettings().setJavaScriptEnabled(true);
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
