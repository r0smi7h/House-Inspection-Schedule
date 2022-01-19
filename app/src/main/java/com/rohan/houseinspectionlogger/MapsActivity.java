package com.rohan.houseinspectionlogger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private Marker CurrentMarker;


    // UI elements for input form
    private EditText ClientName;
    private EditText ClientAddress;
    private EditText Latitude;
    private EditText Longitude;
    private EditText InspectorNotes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Assign local variables for each UI element
        ClientName = (EditText) findViewById(R.id.clientName);
        ClientAddress = (EditText) findViewById(R.id.clientAddress);
        Latitude = (EditText) findViewById(R.id.latitude);
        Longitude = (EditText) findViewById(R.id.longitude);
        InspectorNotes = (EditText) findViewById(R.id.inspectorNotes);

    }


    //METHOD FOR INPUT FORM

    // this method will save an inspection record when the "SAVE" button is clicked.
    public void saveRecord(View view) {
        // --- add an inspection ---
        // name-value pairs of column names and data values from the edit texts
        ContentValues values = new ContentValues();

        values.put(InspectionsProvider.CLIENT_NAME, ((EditText) findViewById(R.id.clientName)).getText().toString());
        values.put(InspectionsProvider.CLIENT_ADDRESS, ((EditText) findViewById(R.id.clientAddress)).getText().toString());
        values.put(InspectionsProvider.LATITUDE, ((EditText) findViewById(R.id.latitude)).getText().toString());
        values.put(InspectionsProvider.LONGITUDE, ((EditText) findViewById(R.id.longitude)).getText().toString());
        values.put(InspectionsProvider.INSPECTOR_NOTES, ((EditText) findViewById(R.id.inspectorNotes)).getText().toString());


        // invoke the content provider via the URI through the getContentResolver().insert
        Uri uri = getContentResolver().insert(InspectionsProvider.CONTENT_URI, values);
    }


    // move the map request to onResume
    @Override
    protected void onResume() {
        super.onResume();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Regina and move the camera
        // Latlng class holds a map point with Latitude and Longitude
        LatLng SaskPolyRegina = new LatLng(50.4079808, -104.5816226);
        //LatLng SaskPolyMJ = new LatLng(50.4041519, -105.5496673);


        // Create and add a Marker to the map
        CurrentMarker = mMap.addMarker(new MarkerOptions().position(SaskPolyRegina).title("Marker in Saskpolytechnic in Regina"));


        // DISPLAY THE MARKER - by moving the camera
        mMap.animateCamera(CameraUpdateFactory.newLatLng(SaskPolyRegina));


        // We can show motion to the new point
        // start somewhere else first
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(SaskPolyMJ));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(SaskPolyRegina, 15));


        // set an onClickListener on the map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {


            @Override
            public void onMapClick(LatLng point) {
                Log.d("DEBUG", "Map clicked [" + point.latitude + " / " + point.longitude + "]");


                // create a new point and move there
                // first clear the existing marker
                CurrentMarker.remove();

                // get the map to add a marker at the new location and store it in the CurrentMarker variable
                CurrentMarker = mMap.addMarker(new MarkerOptions().position(point));

                // display the marker - move the camera
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(SaskPolyRegina));

                // move to new location
                mMap.animateCamera(CameraUpdateFactory.newLatLng(point));

                // use Geocoder to lookup nearby address(es) from the tapped point
                Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
                try {
                    // pass the point locator in to geocoder to look up the address
                    List<Address> addresses = geoCoder.getFromLocation(point.latitude, point.longitude, 1);

                    // compose the address lines into a single string for display
                    String add = "";
                    if (addresses.size() > 0) //check if point is an address
                    {
                        //loop over the # address lines and put them together in a string
                        for (int i = 0; i <= addresses.get(0).getMaxAddressLineIndex(); i++)
                            add += addresses.get(0).getAddressLine(i) + "\n";
                    }

                    // display address, latitude, and longitude in EditTexts when map is clicked
                    ClientAddress.setText(add);
                    Latitude.setText((String.valueOf(point.latitude)));
                    Longitude.setText((String.valueOf(point.longitude)));

                    //Toast.makeText(getBaseContext(), add, Toast.LENGTH_SHORT).show();

                    // create a new point and move there
                    CurrentMarker.remove();

                    // put string as title on the point
                    CurrentMarker = mMap.addMarker(new MarkerOptions().position(point).title(add));

                    // show the title on the map (may not be necessary)
                    CurrentMarker.showInfoWindow();

                    // display the point and title on the map
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15));

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });

    }



    // CODE FOR MENU IMPLEMENTATION - ACTIVITY 1

    // first inflate the menu --> will add items tot the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_all, menu);
        return true;
    }

    // overflow menu will display Show All and Quit
    // option 1 --> send user to second activity
    // option 2 --> quit/close application
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_show_all:
                startActivity(new Intent(this, InspectionRecords.class));
                return true;

            case R.id.menuitem_quit:
                System.exit(0); // exit the application
            default:
                return super.onOptionsItemSelected(item);
        }

    }

}