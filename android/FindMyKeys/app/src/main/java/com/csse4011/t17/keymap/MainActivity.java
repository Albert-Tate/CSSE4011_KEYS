package com.csse4011.t17.keymap;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, OnMapReadyCallback , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private MapFragment mMapFragment;
    private GoogleMap mMap;
    private LatLng UQ = new LatLng(-27.494908, 153.012023);
    private Location mLastLocation;
    private Location myLocation;
    private GoogleApiClient mGoogleApiClient;
    TCPClient client = null;
    WifiManager wifi;
    Location lockey;
    Location locPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        buildGoogleApiClient();

//        client = new TCPClient();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        scanNodes();

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                myLocation = location;

                if (mMap != null) {
                       mMap.clear();
                    if(myLocation != null){
                        mMap.addMarker(new MarkerOptions().position(new LatLng(myLocation.getLatitude(),myLocation.getLongitude())).title("phone").snippet("Phone GPS"));
                        // Instantiates a new CircleOptions object and defines the center and radius
                        CircleOptions circleOptions1 = new CircleOptions()
                                .center(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()))
                                .radius(50)
                                .strokeColor(Color.argb(180, 84, 160, 168))
                                .fillColor(Color.argb(180, 84, 160, 168)); // In meters


                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()), 12));



// Get back the mutable Circle
                        Circle circle1 = mMap.addCircle(circleOptions1);
                    }

//                    locPhone = client.getMyLocationEstimate(wifi.getScanResults());
                    if(locPhone != null){
                        mMap.addMarker(new MarkerOptions().position(new LatLng(locPhone.getLatitude(),locPhone.getLongitude())).title("phone").snippet("Phone Wifi"));
                        // Instantiates a new CircleOptions object and defines the center and radius
                        CircleOptions circleOptions2 = new CircleOptions()
                                .center(new LatLng(locPhone.getLatitude(),locPhone.getLongitude()))
                                .radius(100)
                                .strokeColor(Color.argb(180, 84, 160, 168))
                                .fillColor(Color.argb(180, 84, 160, 168)); // In meters


                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locPhone.getLatitude(),locPhone.getLongitude()), 12));

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
                    if(lockey != null){
                        Log.v("keyPos", "found key");
                        mMap.addMarker(new MarkerOptions().position(new LatLng(lockey.getLatitude(),lockey.getLongitude())).title("phone").snippet("Keys"));
                        // Instantiates a new CircleOptions object and defines the center and radius
                        CircleOptions circleOptions3 = new CircleOptions()
                                .center(new LatLng(lockey.getLatitude(),lockey.getLongitude()))
                                .radius(500)
                                .strokeColor(Color.argb(180, 84, 160, 168))
                                .fillColor(Color.argb(180, 84, 160, 168)); // In meters

                        double angle = angleFromCoordinate(myLocation.getLatitude(), myLocation.getLongitude(),lockey.getLatitude(), lockey.getLongitude() );
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                                .position(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()))
                                .flat(true)
                                .rotation((float)angle - 90));


// Get back the mutable Circle
                        Circle circle1 = mMap.addCircle(circleOptions3);
                    } else {
                        double angle = angleFromCoordinate(myLocation.getLatitude(), myLocation.getLongitude(),UQ.latitude, UQ.longitude );
                        mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                                .position(new LatLng(myLocation.getLatitude(),myLocation.getLongitude()))
                                .flat(true)
                                .rotation((float)angle - 90));
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

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

// Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

    }

    public void requestKeyPos(View view)
    {
        if (client == null)
            return;
            client = new TCPClient();
         lockey = client.getKeyLocationEstimate();
        String report = "\nRead:" + lockey.getProvider() + "\n\tlat: " + lockey.getLatitude()
                + "\n\tlon: " + lockey.getLongitude() + "\n\tAcc: " + lockey.getAccuracy() + "m\n\ttime: " + lockey.getTime();

        Log.v("keyPos", report);
    }

    public void requestMyPos(View view)
    {
        if (client == null)
            return;
            client = new TCPClient();
         locPhone = client.getMyLocationEstimate(wifi.getScanResults());
        if (locPhone != null) {
            String report = "\nRead:" + locPhone.getProvider() + "\n\tlat: " + locPhone.getLatitude()
                    + "\n\tlon: " + locPhone.getLongitude() + "\n\tAcc: " + locPhone.getAccuracy() + "m;";
        }
    }

    public void scanNodesBtn(View view)
    {
        scanNodes();
    }

    private void scanNodes()
    {
        if (wifi == null)
            wifi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        wifi.startScan();
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void onMapReady(GoogleMap mMap) {
        this.mMap = mMap;
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(UQ, 15));
        myLocation = mMap.getMyLocation();

//        mMap.addMarker(new MarkerOptions().position(UQ).title("Keys").snippet("Location of keys"));
//        // Instantiates a new CircleOptions object and defines the center and radius
//        CircleOptions circleOptions = new CircleOptions()
//                .center(UQ)
//                .radius(500)
//                .strokeColor(Color.argb(180, 126, 130, 168))
//                .fillColor(Color.argb(180, 126, 130, 168)); // In meters
//
//// Get back the mutable Circle
//        Circle circle = mMap.addCircle(circleOptions);

//        mMap.addMarker(new MarkerOptions()
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
//                .position(UQ)
//                .flat(true)
//                .rotation(245));




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
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment frag = null;
        switch (position + 1) {
            case 1:
                mTitle = getString(R.string.title_section1);
                frag = PlaceholderFragment.newInstance(position);
                // update the main content by replacing fragments
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, frag)
                        .commit();
                break;
            case 2:

                mMapFragment = MapFragment.newInstance();
                FragmentTransaction fragmentTransaction =
                        getFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.container, mMapFragment);
                fragmentTransaction.commit();
                mMapFragment.getMapAsync(this);
                break;
        }

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 2:
                mTitle = getString(R.string.title_section1);
                mMap = mMapFragment.getMap();
                break;
            case 1:
                mTitle = getString(R.string.title_section2);

                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
