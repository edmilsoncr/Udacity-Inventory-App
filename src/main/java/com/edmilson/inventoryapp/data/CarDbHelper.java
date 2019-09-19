package com.edmilson.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.edmilson.inventoryapp.data.CarContract.CarEntry;

public class CarDbHelper extends SQLiteOpenHelper {

    /** Name of the database file */
    private static final String DATABASE_NAME = "carshop.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link CarDbHelper}.
     * @param context of the app
     */
    public CarDbHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the cars table
        String SQL_CREATE_CARS_TABLE = "CREATE TABLE " + CarEntry.TABLE_NAME + " ("
                + CarEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + CarEntry.COLUMN_CAR_NAME + " TEXT NOT NULL, "
                + CarEntry.COLUMN_CAR_PRICE + " INTEGER NOT NULL, "
                + CarEntry. COLUMN_CAR_QUANTITY + " INTEGER NOT NULL, "
                + CarEntry.COLUMN_CAR_SUPPLIER + " TEXT NOT NULL, "
                + CarEntry.COLUMN_CAR_EMAIL + " TEXT NOT NULL, "
                + CarEntry.COLUMN_CAR_IMAGE + " TEXT NOT NULL);";

        //Execute the SQL statement
        db.execSQL(SQL_CREATE_CARS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to be done here.
    }
}
