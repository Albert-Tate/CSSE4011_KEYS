/**
 * Copyright 2014 Google
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package csse4011.findmykeys;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewOutlineProvider;
import android.view.WindowInsets;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import csse4011.findmykeys.ui.AnimatedPathView;
import csse4011.findmykeys.ui.TransitionAdapter;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private CharSequence mTitle;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private LatLng UQ = new LatLng(-27.494908, 153.012023);
    private Location mLastLocation;
    private Location myLocation;
    private GoogleApiClient mGoogleApiClient;


    Location lockey;
    Location locPhone;


    // TCP stuff
    TCPClient client = null;
    WifiManager wifiManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bitmap photo = setupPhoto(getIntent().getIntExtra("photo", R.drawable.photo1));

        colorize(photo);

        setupMap();
        setupText();

        setOutlines(R.id.star, R.id.info);
        applySystemWindowsBottomInset(R.id.container);

        getWindow().getEnterTransition().addListener(new TransitionAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                ImageView hero = (ImageView) findViewById(R.id.photo);
                ObjectAnimator color = ObjectAnimator.ofArgb(hero.getDrawable(), "tint",
                        getResources().getColor(R.color.photo_tint), 0);
                color.start();

                findViewById(R.id.info).animate().alpha(1.0f);
                findViewById(R.id.star).animate().alpha(1.0f);

                getWindow().getEnterTransition().removeListener(this);
            }
        });

        TCPClientStart();
        createLocationListener();
    }



    private void createLocationListener() {

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                myLocation = location;

                if (mMap != null) {
                    mMap.clear();
                    if (myLocation != null) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude())).title("phone").snippet("Phone GPS"));
                        // Instantiates a new CircleOptions object and defines the center and radius
                        CircleOptions circleOptions1 = new CircleOptions()
                                .center(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                                .radius(50)
                                .strokeColor(Color.argb(180, 84, 160, 168))
                                .fillColor(Color.argb(180, 84, 160, 168)); // In meters


                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 12));


// Get back the mutable Circle
                        Circle circle1 = mMap.addCircle(circleOptions1);
                    }

//                    locPhone = client.getMyLocationEstimate(wifi.getScanResults());
                    if (locPhone != null) {
                        mMap.addMarker(new MarkerOptions().position(new LatLng(locPhone.getLatitude(), locPhone.getLongitude())).title("phone").snippet("Phone Wifi"));
                        // Instantiates a new CircleOptions object and defines the center and radius
                        CircleOptions circleOptions2 = new CircleOptions()
                                .center(new LatLng(locPhone.getLatitude(), locPhone.getLongitude()))
                                .radius(100)
                                .strokeColor(Color.argb(180, 84, 160, 168))
                                .fillColor(Color.argb(180, 84, 160, 168)); // In meters


                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locPhone.getLatitude(), locPhone.getLongitude()), 12));

//                        double angle = angleFromCoordinate(locPhone.getLatitude(), locPhone.getLongitude(),UQ.latitude, UQ.longitude );
//                        mMap.addMarker(new MarkerOptions()
//                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
//                                .position(new LatLng(locPhone.getLatitude(),locPhone.getLongitude()))
//                                .flat(true)
//                                .rotation((float)angle - 90));

// Get back the mutable Circle
                        Circle circle1 = mMap.addCircle(circleOptions2);
                    }
//                    lockey = client.getKeyLocationEstimate();
                    if (lockey != null) {
                        Log.v("keyPos", "found key");
                        mMap.addMarker(new MarkerOptions().position(new LatLng(lockey.getLatitude(), lockey.getLongitude())).title("phone").snippet("Keys"));
                        // Instantiates a new CircleOptions object and defines the center and radius
                        CircleOptions circleOptions3 = new CircleOptions()
                                .center(new LatLng(lockey.getLatitude(), lockey.getLongitude()))
                                .radius(500)
                                .strokeColor(Color.argb(180, 84, 160, 168))
                                .fillColor(Color.argb(180, 84, 160, 168)); // In meters

                        double angle = angleFromCoordinate(myLocation.getLatitude(), myLocation.getLongitude(), lockey.getLatitude(), lockey.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                                .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                                .flat(true)
                                .rotation((float) angle - 90));


// Get back the mutable Circle
                        Circle circle1 = mMap.addCircle(circleOptions3);
                    } else {
                        double angle = angleFromCoordinate(myLocation.getLatitude(), myLocation.getLongitude(), UQ.latitude, UQ.longitude);
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                                .position(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))
                                .flat(true)
                                .rotation((float) angle - 90));
                        mMap.addMarker(new MarkerOptions().position(UQ).title("Keys").snippet("Location of keys"));
//        // Instantiates a new CircleOptions object and defines the center and radius
                        CircleOptions circleOptions = new CircleOptions()
                                .center(UQ)
                                .radius(800)
                                .strokeColor(Color.argb(180, 80, 130, 168))
                                .fillColor(Color.argb(180, 80, 130, 168)); // In meters
                        Circle circle1 = mMap.addCircle(circleOptions);
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

    @Override
    public void onBackPressed() {
        ImageView hero = (ImageView) findViewById(R.id.photo);
        ObjectAnimator color = ObjectAnimator.ofArgb(hero.getDrawable(), "tint",
                0, getResources().getColor(R.color.photo_tint));
        color.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                finishAfterTransition();
            }
        });
        color.start();

        findViewById(R.id.info).animate().alpha(0.0f);
        findViewById(R.id.star).animate().alpha(0.0f);
    }

    private void setupText() {
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(getIntent().getStringExtra("title"));

    }

    private void setupMap() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        float zoom = getIntent().getFloatExtra("zoom", 15.0f);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UQ, zoom));
        mMap.addMarker(new MarkerOptions().position(UQ));
    }

    private void setOutlines(int star, int info) {
        final int size = getResources().getDimensionPixelSize(R.dimen.floating_button_size);

        final ViewOutlineProvider vop = new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, size, size);
            }
        };

        findViewById(star).setOutlineProvider(vop);
        findViewById(info).setOutlineProvider(vop);
    }

    private void applySystemWindowsBottomInset(int container) {
        View containerView = findViewById(container);
        containerView.setFitsSystemWindows(true);
        containerView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                if (metrics.widthPixels < metrics.heightPixels) {
                    view.setPadding(0, 0, 0, 0);
                } else {
                    view.setPadding(0, 0, 0, 0);
                }
                return windowInsets.consumeSystemWindowInsets();
            }
        });
    }

    private void colorize(Bitmap photo) {
        Palette palette = Palette.generate(photo);
        applyPalette(palette);
    }

    private void applyPalette(Palette palette) {
        getWindow().setBackgroundDrawable(new ColorDrawable(palette.getDarkMutedColor().getRgb()));

        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setTextColor(palette.getVibrantColor().getRgb());


        colorRipple(R.id.info, palette.getDarkMutedColor().getRgb(),
                palette.getDarkVibrantColor().getRgb());
        colorRipple(R.id.star, palette.getMutedColor().getRgb(),
                palette.getVibrantColor().getRgb());

        View infoView = findViewById(R.id.information_container);
        infoView.setBackgroundColor(palette.getLightMutedColor().getRgb());

        AnimatedPathView star = (AnimatedPathView) findViewById(R.id.star_container);
        star.setFillColor(palette.getVibrantColor().getRgb());
        star.setStrokeColor(palette.getLightVibrantColor().getRgb());
    }

    private void colorRipple(int id, int bgColor, int tintColor) {
        View buttonView = findViewById(id);

        RippleDrawable ripple = (RippleDrawable) buttonView.getBackground();
        GradientDrawable rippleBackground = (GradientDrawable) ripple.getDrawable(0);
        rippleBackground.setColor(bgColor);

        ripple.setColor(ColorStateList.valueOf(tintColor));
    }

    private Bitmap setupPhoto(int resource) {
        Bitmap bitmap = MainActivity.sPhotoCache.get(resource);
        ((ImageView) findViewById(R.id.photo)).setImageBitmap(bitmap);
        return bitmap;
    }

    public void showStar(View view) {
        toggleStarView();
    }

    private void toggleStarView() {
        final AnimatedPathView starContainer = (AnimatedPathView) findViewById(R.id.star_container);

        if (starContainer.getVisibility() == View.INVISIBLE) {
            findViewById(R.id.photo).animate().alpha(0.2f);
            starContainer.setAlpha(1.0f);
            starContainer.setVisibility(View.VISIBLE);
            starContainer.reveal();
        } else {
            findViewById(R.id.photo).animate().alpha(1.0f);
            starContainer.animate().alpha(0.0f).withEndAction(new Runnable() {
                @Override
                public void run() {
                    starContainer.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    public void showInformation(View view) {
        toggleInformationView(view);
    }

    private void toggleInformationView(View view) {
        final View infoContainer = findViewById(R.id.information_container);

        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;
        float radius = Math.max(infoContainer.getWidth(), infoContainer.getHeight()) * 2.0f;

        Animator reveal;
        if (infoContainer.getVisibility() == View.INVISIBLE) {
            infoContainer.setVisibility(View.VISIBLE);
            reveal = ViewAnimationUtils.createCircularReveal(
                    infoContainer, cx, cy, 0, radius);
            reveal.setInterpolator(new AccelerateInterpolator(2.0f));
        } else {
            reveal = ViewAnimationUtils.createCircularReveal(
                    infoContainer, cx, cy, radius, 0);
            reveal.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    infoContainer.setVisibility(View.INVISIBLE);
                }
            });
            reveal.setInterpolator(new DecelerateInterpolator(2.0f));
        }
        reveal.setDuration(600);
        reveal.start();
    }

    private double angleFromCoordinate(double lat1, double long1, double lat2,
                                       double long2) {

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;
        brng = 360 - brng;

        return brng;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    private void TCPClientStart() {
        client = new TCPClient();
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        scanNodes();
    }

    private void scanNodes() {
        wifiManager.startScan();
    }

    public void requestKeyPos(View view)
    {
        Location loc = client.getKeyLocationEstimate();
        String report = "\nRead:" + loc.getProvider() + "\n\tlat: " + loc.getLatitude()
                + "\n\tlon: " + loc.getLongitude() + "\n\tAcc: " + loc.getAccuracy() + "m\n\ttime: " + loc.getTime();
        Log.d("TCP", report);
    }

    public void requestMyPos(View view) {
        if (client == null)
            client = new TCPClient();
        Location loc = client.getMyLocationEstimate(wifiManager.getScanResults());
        if (loc != null) {
            String report = "\nRead:" + loc.getProvider() + "\n\tlat: " + loc.getLatitude()
                    + "\n\tlon: " + loc.getLongitude() + "\n\tAcc: " + loc.getAccuracy() + "m;";
            Log.d("TCP", report);

        }
    }

}
