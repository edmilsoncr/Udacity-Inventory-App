package com.edmilson.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.edmilson.inventoryapp.data.CarContract.CarEntry;

/**
 * {@link CarCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of car data as its data source. This adapter knows
 * how to create list items for each row of car data in the {@link Cursor}.
 */
public class CarCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link CarCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public CarCursorAdapter(InventoryActivity context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the car data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current car can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name_text_view);
        TextView quantityTextView = view.findViewById(R.id.quantity_text_view);
        TextView priceTextView = view.findViewById(R.id.price_text_view);
        ImageButton buyImageButton = view.findViewById(R.id.buy_image_button);
        ImageView carImageView = view.findViewById(R.id.car_image_view_in_list_view);

        // Find the columns of car attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_PRICE);
        int idColumnIndex = cursor.getColumnIndex(CarEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(CarEntry.COLUMN_CAR_IMAGE);

        // Read the car attributes from the Cursor for the current car
        String carName = cursor.getString(nameColumnIndex);
        final int carQuantity = cursor.getInt(quantityColumnIndex);
        int carPrice = cursor.getInt(priceColumnIndex);
        final long carId = cursor.getLong(idColumnIndex);
        String carImageUriString = cursor.getString(imageColumnIndex);
        Uri carImageUri = Uri.parse(carImageUriString);

        // Update the TextViews and the ImageView with the attributes for the current car
        nameTextView.setText(carName);
        quantityTextView.setText(Integer.toString(carQuantity));
        priceTextView.setText(Integer.toString(carPrice));
        carImageView.setImageURI(carImageUri);

        // Handle the sell button click on the main screen
        buyImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentUri = ContentUris.withAppendedId(CarEntry.CONTENT_URI, carId);
                if (carQuantity > 0) {
                    String newQuantity = String.valueOf(carQuantity - 1);
                    ContentValues values = new ContentValues();
                    values.put(CarEntry.COLUMN_CAR_QUANTITY, newQuantity);
                    context.getContentResolver().update(currentUri, values, null, null);
                } else {
                    Toast.makeText(context, context.getString(R.string.error_message_out_of_stock),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
