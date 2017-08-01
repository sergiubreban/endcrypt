package com.brb.breban.endcript.dataAccess;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by breban on 23.04.2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper{

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "contactsManager";

    public static final String TABLE_CONTACTS = "contacts";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME= "name";
    public static final String KEY_EMAIL= "email";
    public static final String KEY_PUBLIC_KEY= "public_key";
    public static final String KEY_SENDER_ID = "sender_id";

    public static final String TABLE_CONVERSATIONS = "conversations";
    public static final String KEY_CONTACT = "contact_id";

    public static final String TABLE_MESSAGES = "messages";
    public static final String KEY_CONVERSATION= "conversation_id";
    public static final String KEY_TEXT= "text";
    public static final String KEY_IS_MINE= "`is_mine`";

    public static final String TABLE_INVITATIONS = "invitations";


    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DATABSE>>>>>>", "create");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVITATIONS);
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_NAME + " TEXT,"
                + KEY_EMAIL+ " TEXT,"
                + KEY_PUBLIC_KEY + " TEXT,"
                + KEY_SENDER_ID + " TEXT" + ")";

        String CREATE_CONVERSATIONS_TABLE =  "CREATE TABLE " + TABLE_CONVERSATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CONTACT + " INTEGER DEFAULT NULL"
                + ")";

        String CREATE_MESSAGE_TABLE = "CREATE TABLE " + TABLE_MESSAGES + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_CONVERSATION + " INTEGER DEFAULT NULL,"
                + KEY_IS_MINE + "TEXT,"
                + KEY_TEXT + " TEXT"
                + ")";

        String CREATE_INVITATIONS_TABLE = "CREATE TABLE " + TABLE_INVITATIONS+ "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_EMAIL+ " TEXT,"
                + KEY_PUBLIC_KEY + " TEXT,"
                + KEY_SENDER_ID + " TEXT" + ")";

        db.execSQL((CREATE_CONTACTS_TABLE));
        db.execSQL((CREATE_CONVERSATIONS_TABLE));
        db.execSQL((CREATE_MESSAGE_TABLE));
        db.execSQL((CREATE_INVITATIONS_TABLE));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.d("DATABSE", "upgrade");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public void reset(){
        SQLiteDatabase writableDatabase = this.getWritableDatabase();

        writableDatabase.delete(TABLE_CONTACTS, null, null);
        writableDatabase.delete(TABLE_CONVERSATIONS, null, null);
        writableDatabase.delete(TABLE_MESSAGES, null, null);

        writableDatabase.close();
    }

}
