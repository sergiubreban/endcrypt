package com.brb.breban.endcript.dataAccess.dao;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.brb.breban.endcript.dataAccess.DatabaseHelper;
import com.brb.breban.endcript.dataAccess.models.Contact;
import com.brb.breban.endcript.dataAccess.models.Invitation;

import java.util.ArrayList;
import java.util.List;

import static com.brb.breban.endcript.dataAccess.DatabaseHelper.TABLE_INVITATIONS;

/**
 * Created by brebanescu on 6/24/2017.
 */

public class InvitationDao {
    public static final String KEY_ID = "id";
    public static final String KEY_EMAIL= "email";
    public static final String KEY_PUBLIC_KEY= "public_key";
    public static final String KEY_SENDER_ID = "sender_id";
    public DatabaseHelper db;
    private Context context;
    private Resources res;
    private RequestQueue queue;

    public InvitationDao(Context context) {
        this.context = context;
        this.queue = Volley.newRequestQueue(this.context);
        this.res = context.getResources();
        this.db = new DatabaseHelper(this.context);
    }

    public Boolean checkInvitationByEmail(String email){
        SQLiteDatabase db = this.db.getReadableDatabase();

        Cursor cursor = db.query(TABLE_INVITATIONS, new String[]{KEY_ID}, KEY_EMAIL+ "=?",
                new String[]{email}, null, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            return true;
        }else{
            return false;
        }
    }

    public Long add(Invitation invitation){
        SQLiteDatabase db = this.db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_EMAIL, invitation.getEmail());
        values.put(KEY_SENDER_ID, invitation.getSender_id());
        values.put(KEY_PUBLIC_KEY, invitation.getPublic_key());

        Long id = db.insert(TABLE_INVITATIONS, null, values);
        db.close();
        return id;
    }

    public List<Invitation> all(){
        List<Invitation> invitationList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_INVITATIONS;

        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                Invitation invitation = new Invitation(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(3), cursor.getString(2));
                invitationList.add(invitation);
            }while(cursor.moveToNext());
        }
        return invitationList;
    }


    public void delete(Invitation invitation){
        SQLiteDatabase db = this.db.getWritableDatabase();
        db.delete(TABLE_INVITATIONS, KEY_ID + "=?",
                new String[]{String.valueOf(invitation.getId())});
        db.close();
    }
}
