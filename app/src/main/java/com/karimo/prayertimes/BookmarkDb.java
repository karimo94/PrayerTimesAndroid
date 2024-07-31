package com.karimo.prayertimes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.widget.Toast;

import java.util.ArrayList;

public class BookmarkDb extends SQLiteOpenHelper {
    //https://developer.android.com/training/data-storage/sqlite

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "BookmarkVerses";
    //create insert/delete queries
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BookmarkEntry.TABLE_NAME + " (" +
                    BookmarkEntry._ID + " INTEGER PRIMARY KEY," +
                    BookmarkEntry.COLUMN_SURAH_ID + " TEXT," +
                    BookmarkEntry.COLUMN_TRANSLITERATION + " TEXT," +
                    BookmarkEntry.COLUMN_SURAH_AR_NAME + " TEXT," +
                    BookmarkEntry.COLUMN_VERSE_ID + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + BookmarkEntry.TABLE_NAME;
    //columns after SELECT keyword
    public static String[] projection = { BaseColumns._ID,
            BookmarkEntry.COLUMN_SURAH_ID,
            BookmarkEntry.COLUMN_TRANSLITERATION,
            BookmarkEntry.COLUMN_SURAH_AR_NAME,
            BookmarkEntry.COLUMN_VERSE_ID};
    //filter results after WHERE keyword
    public static String selection = "";
    public static String[] selectionArgs = {};
    //sorting order
    public static String sortOrder = BookmarkEntry.COLUMN_SURAH_ID + " DESC";

    public BookmarkDb(Context context){ super(context, DATABASE_NAME, null, DATABASE_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public long insertBookmark(int surahId, int verseId, String surahArName, String surahNameTranslit) {
        ContentValues values = new ContentValues();
        values.put(BookmarkDb.BookmarkEntry.COLUMN_SURAH_ID, surahId);
        values.put(BookmarkDb.BookmarkEntry.COLUMN_TRANSLITERATION, surahNameTranslit);
        values.put(BookmarkDb.BookmarkEntry.COLUMN_SURAH_AR_NAME, surahArName);
        values.put(BookmarkDb.BookmarkEntry.COLUMN_VERSE_ID, verseId);
        // Insert the new row, returning the primary key value of the new row
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        long newRowId = sqLiteDatabase.insert(BookmarkDb.BookmarkEntry.TABLE_NAME, null, values);
        return newRowId;
    }
    public void deleteBookmark(int surahId, int verseId) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int result = sqLiteDatabase.delete(BookmarkEntry.TABLE_NAME,
                BookmarkEntry.COLUMN_SURAH_ID + "=?" + " AND " + BookmarkEntry.COLUMN_VERSE_ID + "=?",
                new String[]{Integer.toString(surahId),Integer.toString(verseId)});
        sqLiteDatabase.close();
    }
    public ArrayList<BookmarkBean> loadAllBookmarks() {
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        ArrayList<BookmarkBean> bookmarkBeans = new ArrayList<>();
        Cursor cursor = sqLiteDatabase.query(
                BookmarkDb.DATABASE_NAME,
                BookmarkDb.projection,
                BookmarkDb.selection,
                null,
                null,
                null,
                BookmarkDb.sortOrder
        );
        if(cursor.moveToFirst()) {
            do {
                //this is based on your projection
                int bookmarkId = Integer.parseInt(cursor.getString(0)); //bookmark id
                int surahId = Integer.parseInt(cursor.getString(1)); //surah id
                String surahTranslitName = cursor.getString(2);
                String surahArName = cursor.getString(3);
                int verseId  = Integer.parseInt(cursor.getString(4));//verse id
                bookmarkBeans.add(new BookmarkBean(surahId, surahTranslitName, surahArName, verseId));
            }
            while(cursor.moveToNext());
            cursor.close();
        }
        else {
            cursor.close();
            return new ArrayList<>();
        }
        return bookmarkBeans;
    }
    class BookmarkEntry implements BaseColumns {
        public static final String TABLE_NAME = BookmarkDb.DATABASE_NAME;
        //surah Id
        public static final String COLUMN_SURAH_ID = "surah_id";
        //surah name
        public static final String COLUMN_SURAH_AR_NAME = "surah_name";
        //surah name transliteration
        public static final String COLUMN_TRANSLITERATION = "surah_name_translit";
        public static final String COLUMN_VERSE_ID = "verse_id";
    }
}
