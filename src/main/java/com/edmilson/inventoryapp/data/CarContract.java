package com.edmilson.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class CarContract {

    // To prevent instantiation
    private CarContract (){}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.edmilson.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.edmilson.inventoryapp/cars/ is a valid path for
     * looking at car data. content://com.edmilson.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_CARS = "cars";

    /**
     * Inner class that defines constant values for the cars database table.
     * Each entry in the table represents a single car.
     */
    public static abstract class CarEntry implements BaseColumns {

        /** The content URI to access the car data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_CARS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of cars.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CARS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single car.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CARS;

        /** Name of database table for cars */
        public static final String  TABLE_NAME = "cars";

        /**
         * Unique ID number for the car (only for use in the database table).
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;

        /**
         * Name of the car.
         * Type: TEXT
         */
        public final static String COLUMN_CAR_NAME ="name";

        /**
         * Price of the car.
         * Type: INTEGER
         */
        public final static String COLUMN_CAR_PRICE = "price";

        /**
         * Quantity of the car.
         * Type: INTEGER
         */
        public final static String COLUMN_CAR_QUANTITY = "quantity";

        /**
         * Supplier of the car.
         * Type: TEXT
         */
        public final static String COLUMN_CAR_SUPPLIER = "supplier";

        /**
         * Email of the car.
         * Type: TEXT
         */
        public final static String COLUMN_CAR_EMAIL = "email";

        /**
         * Image of the car.
         * Type: TEXT
         */
        public final static String COLUMN_CAR_IMAGE = "image";
    }
}
