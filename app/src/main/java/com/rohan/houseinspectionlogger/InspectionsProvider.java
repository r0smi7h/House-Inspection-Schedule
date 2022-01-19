package com.rohan.houseinspectionlogger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class InspectionsProvider extends ContentProvider {
    // constants for the content provider URI
    static final String PROVIDER_NAME = "com.rohan.houseinspectionlogger.inspectionsprovider";
    static final Uri CONTENT_URI = Uri.parse("content://"+ PROVIDER_NAME + "/inspections");

    // constants for the contentproviders' database schema (column names)
    static final String _ID = "_id";
    static final String CLIENT_NAME = "client_name";
    static final String CLIENT_ADDRESS = "client_address";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String INSPECTOR_NOTES = "inspector_notes";

    // matching integers to URI paths
    static final int INSPECTIONS = 1; // match 1 to the table URI content://com.rohan.houseinspectionlogger.inspectionsprovider/inspections"
    static final int INSPECTION_ID = 2; // match 2 to the Row URI content://com.rohan.houseinspectionlogger.inspectionsprovider/inspections/#"
    private static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "inspections", INSPECTIONS);
        uriMatcher.addURI(PROVIDER_NAME, "inspections/#", INSPECTION_ID);
    }

    // global DatabaseHelper object
    DatabaseHelper dbHelper;

    // constants for the SQLite database schema
    static final String DATABASE_NAME = "Inspections";
    static final String DATABASE_TABLE = "clients";
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE +
                    " (_id INTEGER primary key autoincrement, "
                    + "client_name text not null, client_address text not null, latitude double not null, longitude double not null, inspector_notes not null);";

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }
        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(DATABASE_CREATE);
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion) {
            Log.w("Provider database",
                    "Upgrading database from version " +
                            oldVersion + " to " + newVersion +
                            ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS clients");
            onCreate(db);
        }

    }

    @Override
    public int delete(Uri arg0, String arg1, String[] arg2) {

        // get a local database instance (it will close it goes out of scope)
        SQLiteDatabase inspectionsDB = dbHelper.getWritableDatabase();

        int count=0;
        switch (uriMatcher.match(arg0)){
            case INSPECTIONS:
                // delete whole table
                count = inspectionsDB.delete(
                        DATABASE_TABLE,
                        arg1,
                        arg2);
                break;

            case INSPECTION_ID:
                // delete the specified row by ID
                String id = arg0.getPathSegments().get(1);
                count = inspectionsDB.delete(
                        DATABASE_TABLE,
                        _ID + " = " + id +
                                (!TextUtils.isEmpty(arg1) ? " AND (" +
                                        arg1 + ')' : ""),
                        arg2);
                break;
            default: throw new IllegalArgumentException("Unknown URI " + arg0);
        }
        getContext().getContentResolver().notifyChange(arg0, null);
        return count;
    }
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            //---get all books---
            case INSPECTIONS:
                return "vnd.android.cursor.dir/vnd.learn2develop.books ";

            //---get a particular book---
            case INSPECTION_ID:
                return "vnd.android.cursor.item/vnd.learn2develop.books ";

            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // get a local database instance (it will close it goes out of scope)
        SQLiteDatabase inspectionsDB = dbHelper.getWritableDatabase();

        //---add a new book---
        long rowID = inspectionsDB.insert(
                DATABASE_TABLE,
                "",
                values);
        //---if added successfully ID != -1 ---
        if (rowID>0)
        {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();

        // instantiate the global DatabaseHelper
        dbHelper = new DatabaseHelper(context);

        // best NOT to keep an open global database object - do this step in each method that needs a database
        //inspectionsDB = dbHelper.getWritableDatabase();
        return (dbHelper == null)? false:true;
    }
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // get a local database instance (it will close it goes out of scope)
        SQLiteDatabase inspectionsDB = dbHelper.getWritableDatabase();

        SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
        sqlBuilder.setTables(DATABASE_TABLE);
        if (uriMatcher.match(uri) == INSPECTION_ID)
            //---if getting a particular inspection, so we add a where clause---
            sqlBuilder.appendWhere(_ID + " = " + uri.getPathSegments().get(1));

        if (sortOrder==null || sortOrder=="")
            // if no sort order is given, set a default sort order
            sortOrder = CLIENT_NAME +"ASC";

        Cursor c = sqlBuilder.query(
                inspectionsDB,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        //---register to watch a content URI for changes---
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // get a local database instance (it will close it goes out of scope)
        SQLiteDatabase inspectionsDB = dbHelper.getWritableDatabase();

        // although the URI specified in the whole table, we update the records matching selection clause
        int count = 0;
        switch (uriMatcher.match(uri)){
            case INSPECTIONS:
                count = inspectionsDB.update(
                        DATABASE_TABLE,
                        values,
                        selection,
                        selectionArgs);
                break;

            case INSPECTION_ID:
                // update the specified record and the matching selection clause in arg1 and arg2
                count = inspectionsDB.update(
                        DATABASE_TABLE,
                        values,
                        _ID + " = " + uri.getPathSegments().get(1) +
                                (!TextUtils.isEmpty(selection) ? " AND (" +
                                        selection + ')' : ""),
                        selectionArgs);
                break;
            default: throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}