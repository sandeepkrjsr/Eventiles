package com.kodexlabs.eventiles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DB_Bookmark {

    public static final String KEY_ROWID = "_id";
    public static final String KEY_EVENT_ID = "event_id";
    private static final String TAG = "DB_Bookmark";

    private static final String DATABASE_NAME = "DB_Bookmark";
    private static final String DATABASE_TABLE = "bookmark";
    private static final int DATABASE_vVERSION = 1;

    private static final String DATABASE_CREATE = "create table bookmark (_id integer primary key autoincrement, " + "event_id text not null);";

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DB_Bookmark(Context ctx){
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
            db.execSQL("DROP TABLE IF EXISTS bookmark");
            onCreate(db);
        }
    }

    //opens the database
    public DB_Bookmark open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //closes the database
    public void close(){
        DBHelper.close();
    }

    //inserts a contact into the database
    public long insertBookmark(String event_id){
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_EVENT_ID, event_id);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //deletes a particular contact
    public boolean deleteBookmark(long rowId){
        return db.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    //retrieve all the contacts
    public Cursor getAllBookmark(){
        return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_EVENT_ID}, null, null, null, null, null);
    }

    //retrieve a particular contact
    public Cursor getBookmark(long rowId) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_EVENT_ID}, KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null){
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //update a contact
    public boolean updateBookmark(long rowId, String event_id){
        ContentValues args = new ContentValues();
        args.put(KEY_EVENT_ID, event_id);
        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    public Cursor searchBookmark(String filter_item) throws SQLException {
        Cursor mCursor = db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_EVENT_ID}, KEY_EVENT_ID + "=" + filter_item, null, null, null, null, null);
        if (mCursor != null){
            mCursor.moveToFirst();
        }
        return mCursor;
    }
}
