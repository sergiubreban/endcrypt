package com.brb.breban.endcript.dataAccess.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.brb.breban.endcript.R;
import com.brb.breban.endcript.dataAccess.DatabaseHelper;
import com.brb.breban.endcript.dataAccess.models.Contact;
import com.brb.breban.endcript.util.DataCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.brb.breban.endcript.dataAccess.DatabaseHelper.TABLE_CONTACTS;

/**
 * Created by breban on 29.04.2017.
 */

public class ContactDao implements DAO{
    public static final String KEY_ID = "id";
    public static final String KEY_NAME= "name";
    public static final String KEY_EMAIL= "email";
    public static final String KEY_PUBLIC_KEY= "public_key";
    public static final String KEY_SENDER_ID = "sender_id";
    public DatabaseHelper db;
    private Context context;
    private Resources res;
    private RequestQueue queue;

    public ContactDao(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(this.context);
        this.res = context.getResources();
        this.db = new DatabaseHelper(context);
    }
    public ContactDao(DatabaseHelper db) {
        this.db = db;
    }

    public Long addContact(Contact contact){
        SQLiteDatabase db = this.db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName());
        values.put(KEY_EMAIL, contact.getEmail());
        values.put(KEY_SENDER_ID, contact.getSender_id());
        values.put(KEY_PUBLIC_KEY, contact.getPublic_key());

        Long id = db.insert(TABLE_CONTACTS, null, values);
        db.close();
        return id;
    }

    public Contact getContact(int id){
        SQLiteDatabase db = this.db.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID, KEY_NAME, KEY_EMAIL, KEY_SENDER_ID, KEY_PUBLIC_KEY}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));

        return contact;
    }

    public Contact getContactByEmail(String email){
        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID, KEY_NAME, KEY_EMAIL, KEY_SENDER_ID, KEY_PUBLIC_KEY}, KEY_EMAIL + "=?",
                new String[]{email}, null, null, null, null);
        Contact contact = null;

        if(cursor != null && cursor.moveToFirst()){
            contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        }
        return contact;
    }

    public Contact getPartialContactByEmail(String email){
        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID, KEY_NAME, KEY_EMAIL,}, KEY_EMAIL + "=?",
                new String[]{email}, null, null, null, null);
        Contact contact = null;

        if(cursor != null && cursor.moveToFirst()){
            contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2));
        }
        return contact;
    }

    public Contact getContactByToken(String token){
        SQLiteDatabase db = this.db.getReadableDatabase();
        Log.d("token", token);
        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID, KEY_NAME, KEY_EMAIL, KEY_SENDER_ID, KEY_PUBLIC_KEY}, KEY_SENDER_ID + "=?",
                new String[]{token}, null, null, null, null);
        Contact contact = null;

        if(cursor != null && cursor.moveToFirst()){
            contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        }
        return contact;
    }

    public Contact fetch(final Contact contact, final DataCallback callback){
        String url = "http://" +  res.getString(R.string.service_host) + ":" + res.getString(R.string.service_port) + "/api/v1/userprofile/search?email=" + contact.getEmail();

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,  new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    contact.setEmail(response.getString("email"));
                    contact.setPublic_key(response.getString("public_key"));
                    contact.setSender_id(response.getString("sender_id"));
                    updateOrCreateContact(contact);

                }catch (Exception e){
                }
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onSuccess(new JSONObject());

            }
        });

        this.queue.add(request);

        return contact;
    }

    public List<Contact> getAllContacts(){
        List<Contact> contactList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;

        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                Contact contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(4), cursor.getString(3));
                contactList.add(contact);
            }while(cursor.moveToNext());
        }
        return contactList;
    }

    public void updateOrCreateContact(Contact contact){
        if(contact.getId() > 0){
            SQLiteDatabase db = this.db.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(KEY_NAME, contact.getName());
            values.put(KEY_EMAIL, contact.getEmail());
            values.put(KEY_PUBLIC_KEY, contact.getPublic_key());
            values.put(KEY_SENDER_ID, contact.getSender_id());
            db.update(TABLE_CONTACTS, values, KEY_ID + "=?",
                    new String[]{String.valueOf(contact.getId())});
        }else{
            this.addContact(contact);
        }
    }

    public void deleteContact(Contact contact){
        SQLiteDatabase db = this.db.getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + "=?",
                new String[]{String.valueOf(contact.getId())});
        db.close();
    }
}
