package com.kodexlabs.eventiles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DB_Filter {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_FILTER_TYPE = "filter_type";
    public static final String KEY_FILTER_ITEM = "filter_item";
    private static final String TAG = "DB_Filter";

    private static final String DATABASE_NAME = "DB_Filter";
    private static final String DATABASE_TABLE = "filter";
    private static final int DATABASE_vVERSION = 1;

    private static final String DATABASE_CREATE = "create table filter (_id integer primary key autoincrement, " + "filter_type text not null, filter_item text not null unique);";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DB_Filter(Context ctx){
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_vVERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try{
                db.execSQL(DATABASE_CREATE);
            }catch (SQLException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version "+oldVersion+" to "+newVersion+" ,which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS filter");
            onCreate(db);
        }
    }

    //opens the database
    public DB_Filter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //closes the database
    public void close(){
        DBHelper.close();
    }

    //inserts a contact into the database
    public long insertFilter(String filter_type, String filter_item){
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_FILTER_TYPE, filter_type);
        initialValues.put(KEY_FILTER_ITEM,filter_item);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //deletes a particular contact
    public boolean deleteFilter(long rowId){
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //retrieve all the contacts
    public Cursor getAllFilter(){
        return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILTER_TYPE, KEY_FILTER_ITEM}, null, null, null, null, null);
    }

    //retrieve a particular contact
    public Cursor getFilter(long rowId) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILTER_TYPE, KEY_FILTER_ITEM}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null){
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //update a contact
    public boolean updateFilter(long rowId, String filter_type, String filter_item){
        ContentValues args = new ContentValues();
        args.put(KEY_FILTER_TYPE, filter_type);
        args.put(KEY_FILTER_ITEM, filter_item);
        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor searchFilter(String filter_item) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_FILTER_TYPE, KEY_FILTER_ITEM}, KEY_FILTER_ITEM + "=" + filter_item, null, null, null, null, null);
        if (mCursor != null){
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
