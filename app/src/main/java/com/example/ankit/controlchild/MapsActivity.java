package com.example.ankit.controlchild;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ankit.controlchild.Model.Tracking;
import com.example.ankit.controlchild.Model.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // toolbar instance
    private android.support.v7.widget.Toolbar mToolbar;
    RelativeLayout rootLayout;
    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;
    private NavigationView mNavigationView;

    //Request code
    private static final int PERMISSION_REQUEST_CODE = 1998;
    private static final int PLAY_SERVICES_REQUEST_CODE = 1999;
    protected static final int REQUEST_CHECK_SETTINGS = 555;

    //Google APi client
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    Marker mCurrentMarker, mConnectedMarker;
    private GoogleMap mMap;
    double lattitude, longitude, connectedLat, connectedLng;
    private String connectedEmail , currentUserEmail;
    private String sendLocUserId;

    //Map intervals
    private static int UPDATE_INTERVAL = 5000;
    private static int FASTEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    //Firebase
    DatabaseReference mUserDatabase, locationDatabaseRef, mRootRef;
    private FirebaseAuth mAuth;

    //Model Classes
    Tracking trackLoc;
    boolean hasAnimated = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Custom Toolbar
        mToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Control Child");

        mDrawerlayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerlayout, R.string.open, R.string.close);
        mDrawerlayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Firebase initialization
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        locationDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Locations");

        mNavigationView = (NavigationView) findViewById(R.id.navigation_home);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == R.id.navigationAccount) {

                    Intent profileIntent = new Intent(MapsActivity.this, ProfileActivity.class);
                    startActivity(profileIntent);
                    mDrawerlayout.closeDrawers();
                }
                else if (menuItem.getItemId() == R.id.navigationConnected) {

                    Intent connectedIntent = new Intent(MapsActivity.this, ConnectedActivity.class);
                    connectedIntent.putExtra("connectedUserId" , sendLocUserId);
                    startActivity(connectedIntent);
                    mDrawerlayout.closeDrawers();
                }
                else if (menuItem.getItemId() == R.id.navigationSettings) {

                    Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    mDrawerlayout.closeDrawers();
                }
                else if (menuItem.getItemId() == R.id.navigationContact) {

                }

                return false;
            }
        });

        mAuth = FirebaseAuth.getInstance();

        //set contentView
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        setUpLocation();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (checkPlayServices()) {

                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();

                    }
                }
                break;
        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermission();
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }
    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            lattitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();

            //Updating to firebase

            Map locationMap = new HashMap();

            locationMap.put("email", mAuth.getCurrentUser().getEmail());
            locationMap.put("uid", mAuth.getCurrentUser().getUid());
            locationMap.put("lat", mLastLocation.getLatitude());
            locationMap.put("lng", mLastLocation.getLongitude());

            locationDatabaseRef.child(mAuth.getCurrentUser().getUid()).updateChildren(locationMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Log.d("LocationUpdateError", databaseError.getMessage().toString());
                    }
                }
            });

            /*locationDatabaseRef.child(mAuth.getCurrentUser().getUid())
                    .setValue(new Tracking(mAuth.getCurrentUser().getEmail(),
                            mAuth.getCurrentUser().getUid(),
                            String.valueOf(mLastLocation.getLatitude()),
                            String.valueOf(mLastLocation.getLongitude()))); */

            mMap.clear();
            mCurrentMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(lattitude, longitude))
                    .title("You"));

            if (hasAnimated) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lattitude, longitude), 12.02f));
                hasAnimated = false;
            } else {

            }
        }

    }

    private void createLocationRequest() {

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        // Prompt to set location enabled in settings is described here

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        builder.setAlwaysShow(true); //this is the key ingredient **Very important**

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates state = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(MapsActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        createLocationRequest();//keep asking if imp or do whatever
                        break;
                }
                break;
        }
    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {

                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_REQUEST_CODE).show();

            } else {

                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();

            }
            return false;
        }
        return true;

    }

    private void requestRuntimePermission() {

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, PERMISSION_REQUEST_CODE);

    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // mMap.getUiSettings().setMapToolbarEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        displayLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        //Connect is here
        if (item.getItemId() == R.id.connect_item) {

            makeConnectionToGetLocation();

        }

        // Logout is here

        if (item.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent MainIntent = new Intent(MapsActivity.this, StartActivity.class);
            startActivity(MainIntent);
            finish();
        }

        if (mToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return true;

    }

    private void makeConnectionToGetLocation() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Make Connection");
        dialog.setMessage("Please use ID shown in profile");

        LayoutInflater inflater = LayoutInflater.from(this);
        final View connect_layout = inflater.inflate(R.layout.connect_layout, null);

        final MaterialEditText connectID = connect_layout.findViewById(R.id.connect_id);

        dialog.setView(connect_layout);

        dialog.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

        //validation
        if (TextUtils.isEmpty(connectID.getText().toString())) {
            Snackbar.make(rootLayout, "Please enter ID", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        mUserDatabase.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUserEmail = dataSnapshot.child("email").getValue().toString();

                Log.d("UserEmail", currentUserEmail);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUserDatabase.orderByChild("uniqueID").equalTo(connectID.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()) {

                            sendLocUserId = dataSnapshot.getChildren().iterator().next().getKey();

                            mRootRef.child("Locations")
                                    .child(sendLocUserId)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            trackLoc = dataSnapshot.getValue(Tracking.class);

                                            connectedLng = trackLoc.getLng();
                                            connectedLat = trackLoc.getLat();
                                            connectedEmail = trackLoc.getEmail();

                                            Map connectLocMap = new HashMap();
                                            connectLocMap.put("lat", connectedLng);
                                            connectLocMap.put("lng", connectedLat);
                                            connectLocMap.put("email" , connectedEmail);

                                            Map currentLocMap = new HashMap();
                                            currentLocMap.put("lat", lattitude);
                                            currentLocMap.put("lng", longitude);
                                            currentLocMap.put("email" , currentUserEmail);

                                            Map newConnectMap = new HashMap();
                                            newConnectMap.put("Connected/" + mAuth.getCurrentUser().getUid() + "/" + sendLocUserId, connectLocMap);
                                            newConnectMap.put("Connected/" + sendLocUserId + "/" + mAuth.getCurrentUser().getUid(), currentLocMap);


                                            mRootRef.updateChildren(newConnectMap, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                    if (databaseError != null) {

                                                        Log.d("New Connection log", databaseError.getMessage().toString());
                                                    }else {
                                                        mConnectedMarker = mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(connectedLat, connectedLng))
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                                                .title("New Connection"));
                                                        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(connectedLat, connectedLng), 12.02f));

                                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                                                        builder.include(mCurrentMarker.getPosition());
                                                        builder.include(mConnectedMarker.getPosition());

                                                        LatLngBounds bounds = builder.build();

                                                        int padding = 50 ;

                                                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);

                                                        mMap.animateCamera(cu);

                                                        updateConnectedLatLng();
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                            //This is giving correct userId
                            // Log.d("UserID", dataSnapshot.getChildren().iterator().next().getKey());

                            //Log.d("userLoc", dataSnapshot.getValue().);
                            //Log.d("LocationIser", locationDatabaseRef.child(dataSnapshot.getValue().toString()) + "/" +  );
                            Toast.makeText(MapsActivity.this, "Yo ! Connected Successfully", Toast.LENGTH_SHORT).show();

                        } else {

                            Toast.makeText(MapsActivity.this, "User Not Exist", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });


        // this is cancel btn for dialog
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void updateConnectedLatLng() {
        connectedLat = trackLoc.getLat();
        connectedLng = trackLoc.getLng();
    }
}

  /*   This is a function to generate unique ID's wihtin an array

    static int [] createUniqueRandomNumbers(int from , int to) {
        int n = to - from + 1;  // number of int need to regenerate

        // Array to store all umber in [from , to]

        int a[] = new int[n];
        for (int i = 0; i < n; i++)
            a[i] = i;

        //Array to store result
        int[] result = new int[n];

        int x = n;
        SecureRandom rd = new SecureRandom();
        for (int i = 0; i < n; i++) {

            // k is random index in [0 , x]

            int k = rd.nextInt(x);
            result[i] = a[k];

            // we get value from a[k] , we replace its value by the value from last index
            //so that we don't get that value any more
            a[k] = a[x - 1];

            // then we decrease value x by 1 to get random index from 0 to x only

            x--;
        }
        return result;
    }*/
