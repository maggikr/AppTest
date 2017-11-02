package com.example.maggs.fishapp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
=======
>>>>>>> parent of b72637d... added bottomsheet
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private GoogleMap gMap;
    // Write a message to the database
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("fishLocations");


    //Variabler for å sjekke permission
    private String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
    private static final int LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Legger til egen toolbar øverst i app
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

<<<<<<< HEAD
        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(400);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        startActivity(new Intent(this, RegisterActivity.class)
                .putExtra("LatLng", latLng));
=======
>>>>>>> parent of b72637d... added bottomsheet
    }

    // Laster inn meny/søkeknapp i toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    //Legger til "onClick" funksjonalitet på menyelementer.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.item_login:
                startActivity(new Intent(this, LoginActivity.class));
                return true;

            case R.id.item_regEvent:
                startActivity(new Intent(this, RegisterActivity.class));
                return true;

            case R.id.item_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                return true;

            case R.id.item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            case R.id.item_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



    // Kjøres når kartet er lastet inn
    // Legger til "marker" i halden og zoomer inn på lokasjonen
    // Kjører også metoder for å sjekke at det er gitt permission til brukers lokasjon
    // og aktiverer UI komponenter i kartet som "My location" knapp og +/- zoom
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gMap = googleMap;

        LatLng halden = new LatLng(59.12478, 11.38754);
        gMap.addMarker(new MarkerOptions().position(halden)
                .title("Marker in Halden"));
        gMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(halden, 15, 0, 0)));

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                for (DataSnapshot fishLocs: dataSnapshot.getChildren()) {
                    FishLoc fishLoc = fishLocs.getValue(FishLoc.class);
                    Log.d("maggiDB", "Value is: " + dataSnapshot.getChildrenCount());
                    gMap.addMarker(new MarkerOptions().position(new LatLng(fishLoc.getLat(),fishLoc.getLng()))
                            .title(fishLoc.getFishType())
                            .snippet("ID: " + fishLoc.getId()));
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("maggiDB", "Failed to read value.", error.toException());
            }

        });
<<<<<<< HEAD

        gMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                updateBottomSheetContent(marker);
                return true;
            }
        });
        //Hides bottom sheet on map clicks
        /*gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });*/

        setLocationEnabled();
        setDefaultUiSettings();

    }

    //Updates bottom sheet text views with info from marker and shows the bottom sheet
    private void updateBottomSheetContent(Marker marker) {
        TextView title = (TextView) bottomSheet.findViewById(R.id.marker_title);
        TextView snippet = (TextView) bottomSheet.findViewById(R.id.marker_snippet);
        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }






        //gMap.setInfoWindowAdapter(new MyInfoWindow(this));
=======
        gMap.setInfoWindowAdapter(new MyInfoWindow(this));
>>>>>>> parent of b72637d... added bottomsheet
        /*gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                      @Override
                                      public View getInfoWindow(Marker marker) {
                                          View view = rentClusterManager.getMarkerManager().getInfoWindow(marker);
                                          if (view == null)
                                              view = saleClusterManager.getMarkerManager().getInfoWindow(marker);
                                          return view;
                                      }

                                      @Override
                                      public View getInfoContents(Marker marker) {
                                          return null;
                                      }
                                  });*/


        setLocationEnabled();
        setDefaultUiSettings();
        gMap.setOnMapLongClickListener(this);
    }

    private void setDefaultUiSettings() {
        UiSettings uiSettings = gMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setMyLocationButtonEnabled(true);

    }

    //Sjekker om bruker har gitt lokasjons permission, ber om permission om det ikke er gitt.
    @AfterPermissionGranted(LOCATION_PERMISSION)
    private void setLocationEnabled() {

        if (EasyPermissions.hasPermissions(this, locationPermission)) {

            gMap.setMyLocationEnabled(true);

        } else {

            EasyPermissions.requestPermissions(this, getString(R.string.no_location_permission),
                    LOCATION_PERMISSION, locationPermission);
        }
    }



    //Obsolete
    /*
    protected void onResume() {
        super.onResume();

        ArrayList<FishLoc> locList = FishLoc.getFishLocList();

        for (int i = 0; i < locList.size(); i++){
            Log.v("Maggi main", "verdi :"+ locList.get(i));
            //gMap.addMarker(new MarkerOptions().position(locList.get(i).getLoc())
            //        .title("Hei"));
        }
    }*/


}
