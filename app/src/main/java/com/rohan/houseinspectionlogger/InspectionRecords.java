package com.rohan.houseinspectionlogger;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;

import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class InspectionRecords extends AppCompatActivity implements LoaderCallbacks {


    // globals for ListView and SimpleCursorAdapter
    ListView InspectionsListView;
    SimpleCursorAdapter InspectionsAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_records);

        //CODE FOR CONTENT PROVIDER

        // get a reference to the ListView
        InspectionsListView = findViewById(R.id.inspectionsListview);

        // setup the SimpleCursorAdapter with the needed columns and XML item layout for ListView, but WITHOUT a cursor

        // Array of columns from the the Inspections Content Provider
        String[] columns = new String[] { InspectionsProvider.CLIENT_NAME , InspectionsProvider.CLIENT_ADDRESS,
                InspectionsProvider.LATITUDE, InspectionsProvider.LONGITUDE, InspectionsProvider.INSPECTOR_NOTES };

        // Array of textview ID's from the ListView
        int[] views = new int[] {R.id.clientName, R.id.clientAddress, R.id.latitude, R.id.longitude, R.id.inspectorNotes};

        // create an instance of the SimpleCursorAdapter with above "settings" and a NULL cursor
        InspectionsAdapter = new SimpleCursorAdapter(this, R.layout.layout_rows, null, columns, views, 0);

        // attach the Adapter to the ListView
        InspectionsListView.setAdapter(InspectionsAdapter);

        // setup the LoaderManager
        LoaderManager.getInstance(this).initLoader(0, null, this);
        // after the LoaderManager inits the CursorLoader - it load with data automatically ...

    }


    @NonNull
    @Override
    public Loader onCreateLoader(int id, @Nullable Bundle args) {

        // create and return a CursorLoader
        Uri allInspections = Uri.parse("content://com.rohan.houseinspectionlogger.inspectionsprovider/inspections");

        CursorLoader cursorLoader = new CursorLoader(
                this,
                allInspections,
                null,
                null,
                null,
                "client_name DESC"); // sort by client name column in descending order

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader loader, Object data) {

        // after the LoaderManager inits the CursorLoader - it load with data automatically ...
        // onLoadFinished will run when the CursorLoader has loaded a cursor with data
        // and that data is passed in here into the param Object data

        Cursor c = (Cursor) data; // cast the input data into a Cursor

        // attach this new / reloaded cursor to the SimpleCursorAdapter
        // notice that the Cursor is managed by the LoaderManager / CursorLoader automatically
        // and InspectionsAdapter will automatically update to display data as it changes
        InspectionsAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) { }


    // CODE FOR MENU IMPLEMENTATION - ACTIVITY 2

    // first inflate the menu --> will add items tot the action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.enter_record, menu);
        return true;
    }

    // overflow menu will display 2 options: Enter Record and Quit
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // option 1 --> send user back to first activity
        // option 2 --> quit/close application
        switch (item.getItemId()) {
            case R.id.menuitem_enter_record:
                startActivity(new Intent(this, MapsActivity.class));
                return true;

            case R.id.menuitem_quit:
                System.exit(0); // exit the application

            default:
                return super.onOptionsItemSelected(item);
        }

    }

}
