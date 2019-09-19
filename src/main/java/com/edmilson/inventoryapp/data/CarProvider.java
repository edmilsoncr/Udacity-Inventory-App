package com.edmilson.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.edmilson.inventoryapp.data.CarContract.CarEntry;

/**
 * {@link ContentProvider} for Cars app.
 */
public class CarProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = CarProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the cars table */
    private static final int CARS = 100;

    /** URI matcher code for the content URI for a single car in the cars table */
    private static final int CAR_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.edmilson.inventoryapp/cars" will maps to the integer code
        // {@link #CARS}. This URI is used to provide access to multiple rows of the cars table.
        sUriMatcher.addURI(CarContract.CONTENT_AUTHORITY, CarContract.PATH_CARS, CARS);

        // The content URI of the form "content://com.edmilson.inventoryapp/cars/#" will maps to the integer code
        // {@link #CAR_ID}. This URI is used to provide access to one single row of the cars table.

        /**
         * In this case, the "#" wildcard is used where "#" can be substituted for an Integer.
         * For example "content://com.edmilson.inventoryapp/cars/3" matches, but
         * "content://com.edmilson.inventoryapp/cars" doesn't match.
         */
        sUriMatcher.addURI(CarContract.CONTENT_AUTHORITY, CarContract.PATH_CARS + "/#", CAR_ID);
    }

    /** DataBase helper object */
    private CarDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new CarDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable DataBase
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This Cursor will hold the result of the query
        Cursor cursor;

        // Figure it out if the URI Matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case CARS:
                // For the CARS code, query the cars table directly with the given
                // projection, selection, selection arguments and sort order. The cursor
                // could contain multiple rows of the cars table.
                cursor = database.query(CarEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case CAR_ID:
                // For the CAR_ID code, extract out the ID from the URI.
                // From an example URI such as "content://com.edmilson.inventoryapp/cars/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID  of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments String array.
                selection = CarEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query off the cars table where the _id = 3 to return a
                // cursor containing that row  of the table
                cursor = database.query(CarEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI: " + uri);
        }
        // Set notification uri on the cursor,
        // so we know what the content URI the cursor was created for.
        // If the data of this uri changes, then we know we need to update the cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CARS:
                return CarEntry.CONTENT_LIST_TYPE;
            case CAR_ID:
                return CarEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CARS:
                return insertCar(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a car into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    public Uri insertCar (Uri uri, ContentValues values){
        // Check if the given name is null
        String name = values.getAsString(CarEntry.COLUMN_CAR_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Car requires a name");
        }

        // Check if the given price is bigger than 0
        Integer price = values.getAsInteger(CarEntry.COLUMN_CAR_PRICE);
        if (price == null && price < 0) {
            throw new IllegalArgumentException("Car requires a valid price");
        }

        // Check if the given quantity is bigger than 0
        Integer quantity = values.getAsInteger(CarEntry.COLUMN_CAR_QUANTITY);
        if (quantity == null && quantity < 0) {
            throw new IllegalArgumentException("Car requires a valid quantity");
        }

        // Check if the given supplier is null
        String supplier = values.getAsString(CarEntry.COLUMN_CAR_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Car requires a supplier");
        }

        // Check if the given email is null
        String email = values.getAsString(CarEntry.COLUMN_CAR_EMAIL);
        if (email == null) {
            throw new IllegalArgumentException("Car requires an email");
        }

        // Check if the given image is null
        String image = values.getAsString(CarEntry.COLUMN_CAR_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Car requires an image");
        }

        // Get writable DataBase
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        long id = database.insert(CarEntry.TABLE_NAME, null, values);

        // Show a log message when the insertion was failed
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the car content uri
        // uri: content://com.edmilson.inventoryapp/cars
        getContext().getContentResolver().notifyChange(uri, null);

        // return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CARS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(CarEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CAR_ID:
                // Delete a single row given by the ID in the URI
                selection = CarEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(CarEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CARS:
                return updateCar(uri, values, selection, selectionArgs);
            case CAR_ID:
                // For the CAR_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = CarEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateCar(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update cars in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more cars).
     * Return the number of rows that were successfully updated.
     */
    private int updateCar(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link CarEntry#COLUMN_CAR_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(CarEntry.COLUMN_CAR_NAME)) {
            String name = values.getAsString(CarEntry.COLUMN_CAR_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Car requires a name");
            }
        }

        // If the {@link CarEntry#COLUMN_CAR_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(CarEntry.COLUMN_CAR_PRICE)) {
            // Check if the price is bigger or equals to 0
            Integer price = values.getAsInteger(CarEntry.COLUMN_CAR_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Car requires valid price");
            }
        }

        // If the {@link CarEntry#COLUMN_CAR_QUANTITY} key is present,
        // check that the QUANTITY value is valid.
        if (values.containsKey(CarEntry.COLUMN_CAR_QUANTITY)) {
            // Check if the quantity is bigger or equals to 0
            Integer quantity = values.getAsInteger(CarEntry.COLUMN_CAR_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Car requires valid quantity");
            }
        }

        // If the {@link CarEntry#COLUMN_CAR_SUPPLIER} key is present,
        // check that the supplier value is not null.
        if (values.containsKey(CarEntry.COLUMN_CAR_SUPPLIER)) {
            String supplier = values.getAsString(CarEntry.COLUMN_CAR_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Car requires valid supplier");
            }
        }

        // If the {@link CarEntry#COLUMN_CAR_EMAIL} key is present,
        // check that the email value is not null.
        if (values.containsKey(CarEntry.COLUMN_CAR_EMAIL)) {
            String email = values.getAsString(CarEntry.COLUMN_CAR_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException("Car requires valid email");
            }
        }

        // If the {@link CarEntry#COLUMN_CAR_IMAGE} key is present,
        // check that the image value is not null.
        if (values.containsKey(CarEntry.COLUMN_CAR_IMAGE)) {
            String image = values.getAsString(CarEntry.COLUMN_CAR_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Car requires valid image");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(CarEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}