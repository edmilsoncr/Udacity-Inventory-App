package com.edmilson.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.edmilson.inventoryapp.data.CarContract.CarEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    /**
     * Constant value for identification of the request action
     */
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    /**
     * Identifier for the pet data loader
     */
    private static final int EXISTING_CAR_LOADER = 0;
    /**
     * TextInputEditText field and layout to enter the car's name
     */
    private TextInputEditText mNameEditText;
    private TextInputLayout mNameInputLayout;
    private String mNameString;
    /**
     * TextInputEditText field and layout to enter the car's price
     */
    private TextInputEditText mPriceEditText;
    private TextInputLayout mPriceInputLayout;
    private String mPriceString;
    /**
     * TextInputEditText field and layout to enter the available car quantity
     */
    private TextInputEditText mQuantityEditText;
    private TextInputLayout mQuantityInputLayout;
    private String mQuantityString;
    /**
     * TextInputEditText field and layout to enter the car's supplier
     */
    private TextInputEditText mSupplierEditText;
    private TextInputLayout mSupplierInputLayout;
    private String mSupplierString;
    /**
     * TextInputEditText field and layout to enter the car's supplier email
     */
    private TextInputEditText mEmailEditText;
    private TextInputLayout mEmailInputLayout;
    private String mEmailString;
    /**
     * Declaration of mCarImageView to show a car picture
     */
    private ImageView mCarImageView;
    /**
     * RelativeLayout to handle the car image
     */
    private RelativeLayout addCarImageLayout;
    private ImageView addPictureImageView;
    /**
     * Image Uri and ImageView of the car
     */
    private Uri mImageUri;
    /**
     * Content URI for the existing car (null if it's a new car)
     */
    private Uri mCurrentCarUri;
    /**
     * Boolean flag that keeps track of whether the car has been edited (true) or not (false)
     */
    private boolean mCarHasChanged = false;
    /**
     * Boolean flag to prevent the onLoadFinished method to reload data from the database and populate
     * the fields, when restarting the EditorActivity after the user changes the car image
     */
    private boolean mChangingCarImage = false;
    private String pictureFilePath;


    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mCarHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCarHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch the activity
        // in order to figure out if we're creating a new car or editing an existing one
        Intent intent = getIntent();
        mCurrentCarUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.name_edit_text);
        mPriceEditText = findViewById(R.id.price_edit_text);
        mQuantityEditText = findViewById(R.id.quantity_edit_text);
        mSupplierEditText = findViewById(R.id.supplier_edit_text);
        mEmailEditText = findViewById(R.id.email_edit_text);

        // Initializing the buttons and the imageView
        Button plusButton = findViewById(R.id.plus_button);
        Button minusButton = findViewById(R.id.minus_button);
        Button makeOrderButton = findViewById(R.id.make_order_button);
        mCarImageView = findViewById(R.id.car_image_view);
        addCarImageLayout = findViewById(R.id.addImage_layout);
        addPictureImageView = findViewById(R.id.add_picture_image_view);

        // Initializing the InputLayout to setError when no data was inputted on the EditTexts
        mNameInputLayout = findViewById(R.id.car_name_input_layout);
        mPriceInputLayout = findViewById(R.id.car_price_input_layout);
        mQuantityInputLayout = findViewById(R.id.car_quantity_input_layout);
        mSupplierInputLayout = findViewById(R.id.car_supplier_input_layout);
        mEmailInputLayout = findViewById(R.id.car_email_input_layout);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);
        plusButton.setOnTouchListener(mTouchListener);
        minusButton.setOnTouchListener(mTouchListener);
        addCarImageLayout.setOnTouchListener(mTouchListener);

        // Setting the onClickListener to this activity
        plusButton.setOnClickListener(this);
        minusButton.setOnClickListener(this);
        makeOrderButton.setOnClickListener(this);
        addCarImageLayout.setOnClickListener(this);

        // If the intent does not contain a car content Uri, them we know that we are
        // creating a new car
        if (mCurrentCarUri == null) {
            // This is a new car, so change the app bar to say "Add a Car"
            setTitle(getString(R.string.editor_activity_title_add_car));
            makeOrderButton.setVisibility(View.GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a car that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing car, so change app bar to say "Edit a Car"
            setTitle(getString(R.string.editor_activity_title_edit_car));
            makeOrderButton.setVisibility(View.VISIBLE);
            addPictureImageView.setImageResource(R.drawable.ic_pencil_grey600_48dp);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) addPictureImageView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            addPictureImageView.setLayoutParams(params); //causes layout update

            // Initialize a loader to read the car data from the database
            // and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_CAR_LOADER, null, this);
        }
    }

    /**
     * This method is called when any of the activity's view components is clicked.
     */
    @Override
    public void onClick(View v) {
        mQuantityString = mQuantityEditText.getText().toString().trim();
        // If the quantity EditText has no value set it to "0", otherwise the app will crash
        if (mQuantityString.equals("")) {
            mQuantityString = "0";
        }
        int quantity;
        switch (v.getId()) {
            case R.id.plus_button:
                quantity = Integer.parseInt(mQuantityString);
                quantity++;
                mQuantityEditText.setText(Integer.toString(quantity));
                break;
            case R.id.minus_button:
                quantity = Integer.parseInt(mQuantityString);
                if (quantity > 0) {
                    quantity--;
                }
                mQuantityEditText.setText(Integer.toString(quantity));
                break;
            case R.id.make_order_button:
                dispatchSendEmailIntent();
                break;
            case R.id.addImage_layout:
                dispatchTakePictureIntent();
                break;
        }
    }

    /**
     * Starts an Intent to send an Email
     */
    private void dispatchSendEmailIntent() {
        // Intent to open an email app
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + mEmailEditText.getText().toString().trim()));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_intent_subject));
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_intent_car_name)
                + mNameEditText.getText().toString().trim() + "\n" + getString(R.string.car_quantity));
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        }
    }

    /**
     * Using FileProvider, get file URI, add it to intent as extra and then start activity
     * to get an image from a camera app.
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this,
                        getString(R.string.error_message_cannot_create_photo_file),
                        Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.edmilson.inventoryapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Creates a file path to store an image
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        pictureFilePath = image.getAbsolutePath();
        return image;
    }

    /**
     * Receive the image requested with intent ACTION_OPEN_DOCUMENT and save it's Uri
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            File imgFile = new File(pictureFilePath);
            if (imgFile.exists()) {
                mImageUri = Uri.fromFile(imgFile);
                mCarImageView.setImageURI(mImageUri);
                // Change the click image source and change it's position
                addPictureImageView.setImageResource(R.drawable.ic_pencil_grey600_48dp);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) addPictureImageView.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                addPictureImageView.setLayoutParams(params); //causes layout update
                // Show that the user just changed the image of the car, so no reloading data from the
                // database is needed
                mChangingCarImage = true;
            }
        }
    }

    /**
     * Get user input from editor and save new car into database
     */
    private void saveCar() {
        // Read from input fields and use trim to eliminate leading or trailing white space
        mNameString = mNameEditText.getText().toString().trim();
        mPriceString = mPriceEditText.getText().toString().trim();
        mQuantityString = mQuantityEditText.getText().toString().trim();
        mSupplierString = mSupplierEditText.getText().toString().trim();
        mEmailString = mEmailEditText.getText().toString().trim();

        // Check if any of the input fields is empty, and exit this method if so
        if (!validateInputs()) {
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(CarEntry.COLUMN_CAR_NAME, mNameString);
        values.put(CarEntry.COLUMN_CAR_PRICE, mPriceString);
        values.put(CarEntry.COLUMN_CAR_QUANTITY, mQuantityString);
        values.put(CarEntry.COLUMN_CAR_SUPPLIER, mSupplierString);
        values.put(CarEntry.COLUMN_CAR_EMAIL, mEmailString);
        values.put(CarEntry.COLUMN_CAR_IMAGE, mImageUri.toString());

        // Determine if this is a new or existing car by checking if mCurrentCarUri is null or not
        if (mCurrentCarUri == null) {
            // This is a NEW car, so insert a new car into the provider,
            // returning the content URI for the new car.
            Uri newUri = getContentResolver().insert(CarEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_car_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_car_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING car, so update the car with content URI: mCurrentCarUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentCarUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentCarUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_car_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_car_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Exit activity
        finish();
    }

    /**
     * This method is called to check if any of the input values is empty and set an error message
     * to the TextInputLayout
     */
    private boolean validateInputs() {
        boolean valid = true;
        // Check if the inputted Name is empty and set error to the TextInputLayout
        if (mNameString.isEmpty()) {
            mNameInputLayout.setError(getString(R.string.error_message_name_field));
            valid = false;
        } else {
            mNameInputLayout.setError(null);
        }

        // Check if the inputted Price is empty and set error to the TextInputLayout
        if (mPriceString.isEmpty()) {
            mPriceInputLayout.setError(getString(R.string.error_message_price_field));
            valid = false;
        } else {
            mPriceInputLayout.setError(null);
        }

        // Check if the inputted Quantity is empty and set error to the TextInputLayout
        if (mQuantityString.isEmpty()) {
            mQuantityInputLayout.setError(getString(R.string.error_message_quantity_field));
            valid = false;
        } else {
            mQuantityInputLayout.setError(null);
        }

        // Check if the inputted Supplier's name is empty and set error to the TextInputLayout
        if (mSupplierString.isEmpty()) {
            mSupplierInputLayout.setError(getString(R.string.error_message_supplier_field));
            valid = false;
        } else {
            mSupplierInputLayout.setError(null);
        }

        // Check if the inputted Email is empty and set error to the TextInputLayout
        if (mEmailString.isEmpty()) {
            mEmailInputLayout.setError(getString(R.string.error_message_email_field));
            valid = false;
        } else {
            mEmailInputLayout.setError(null);
        }

        // Check if the mImageUri is empty or null
        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.error_message_image_field), Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new car, hide the "Delete" menu item.
        if (mCurrentCarUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save car to the database
                saveCar();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the car hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mCarHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the car hasn't changed, continue with handling back button press
        if (!mCarHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        // Since the editor shows all car attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                CarEntry._ID,
                CarEntry.COLUMN_CAR_NAME,
                CarEntry.COLUMN_CAR_PRICE,
                CarEntry.COLUMN_CAR_QUANTITY,
                CarEntry.COLUMN_CAR_SUPPLIER,
                CarEntry.COLUMN_CAR_EMAIL,
                CarEntry.COLUMN_CAR_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentCarUri,         // Query the content URI for the current car
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null, there is less than 1 row in the cursor
        // or the user is changing an image of the car (Otherwise all the data inputted
        // on the EditTexts will be replaced with the ones saved in th database.)
        if (cursor == null || cursor.getCount() < 1 || mChangingCarImage) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of car attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_NAME);
            int priceColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_SUPPLIER);
            int emailColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String carImageUriString = cursor.getString(imageColumnIndex);
            mImageUri = Uri.parse(carImageUriString);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);
            mEmailEditText.setText(email);
            mCarImageView.setImageURI(mImageUri);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mEmailEditText.setText("");
        mCarImageView.setImageURI(null);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog,
                // continue editing the car and set the mCarHasChanged to false.
                if (dialog != null) {
                    mCarHasChanged = false;
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Show a dialog to the user to confirm that they want to delete the current car data.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_one_car_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the car.
                deleteCar();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the car.
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
     * Perform the deletion of the car in the database.
     */
    private void deleteCar() {
        // Only perform the delete if this is an existing car.
        if (mCurrentCarUri != null) {
            // Call the ContentResolver to delete the car at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentCarUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentCarUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_car_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_car_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}
