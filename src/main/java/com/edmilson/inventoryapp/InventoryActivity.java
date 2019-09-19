package com.edmilson.inventoryapp;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.edmilson.inventoryapp.data.CarContract.CarEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CAR_LOADER = 0;
    CarCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the inventory data
        ListView carListView = findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        carListView.setEmptyView(emptyView);

        // Setup an adapter to create a list item for each row of car data in the cursor
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor
        mCursorAdapter = new CarCursorAdapter(this, null);
        carListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        carListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(InventoryActivity.this, EditorActivity.class);

                Uri currentCarUri = ContentUris.withAppendedId(CarEntry.CONTENT_URI, id);

                // Set the Uri on the data field of the intent
                intent.setData(currentCarUri);

                // Launch the {@link EditorActivity} to display the data for the current car
                startActivity(intent);
            }
        });

        // Kick off the loader
        //no inspection deprecation
        getSupportLoaderManager().initLoader(CAR_LOADER, null, InventoryActivity.this);
    }

    /**
     * Show a dialog to the user to confirm that they want to delete all cars from the database.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_cars_dialog_msg);
        builder.setPositiveButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete all the cars from the database.
                deleteAllCars();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Helper method to delete all cars in the database.
     */
    private void deleteAllCars() {
        int rowsDeleted = getContentResolver().delete(CarEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from car database");
    }

    /**
     * Helper method to insert hardcoded car data into the database. For debugging purposes only.
     */
    private void insertCar() {
        // Create a ContentValues object where column names are the keys,
        // and Ford Mustang's car attributes are the values.
        ContentValues values = new ContentValues();
        values.put(CarEntry.COLUMN_CAR_NAME, "Ford Mustang");
        values.put(CarEntry.COLUMN_CAR_PRICE, 25845);
        values.put(CarEntry.COLUMN_CAR_QUANTITY, 50200);
        values.put(CarEntry.COLUMN_CAR_SUPPLIER, "Fast Car Store");
        values.put(CarEntry.COLUMN_CAR_EMAIL, "order@fastcarstore.com");

        // Get Uri for example photo from drawable resource
        Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.car_default)
                + '/' + getResources().getResourceTypeName(R.drawable.car_default)
                + '/' + getResources().getResourceEntryName(R.drawable.car_default));
        values.put(CarEntry.COLUMN_CAR_IMAGE, String.valueOf(imageUri));

        // Insert a new row for Ford Mustang into the provider using the ContentResolver.
        // Use the {@link CarEntry#CONTENT_URI} to indicate that we want to insert
        // into the cars database table.
        // Receive the new content URI that will allow us to access Ford Mustang's data in the future.
        Uri newUri = getContentResolver().insert(CarEntry.CONTENT_URI, values);
        Log.v("InventoryActivity", "newUri" + newUri);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertCar();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about
        String[] projection = {
                CarEntry._ID,
                CarEntry.COLUMN_CAR_NAME,
                CarEntry.COLUMN_CAR_QUANTITY,
                CarEntry.COLUMN_CAR_PRICE,
                CarEntry.COLUMN_CAR_IMAGE};

        // This Loader will execute the ContentProvider's query method on a background thread
        return  new CursorLoader(this,  // Parent activity context
                CarEntry.CONTENT_URI,           // Provider content URI to query
                projection,                     // Columns to include in the resulting Cursor
                null,                   // No Selection clause
                null,               // No Selection arguments
                null);                  //Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Update {@link CarCursorAdapter} with this new cursor containing updated car data
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
