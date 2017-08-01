package com.brb.breban.endcript.dataAccess.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.brb.breban.endcript.dataAccess.DatabaseHelper;
import com.brb.breban.endcript.dataAccess.models.Conversation;

import java.util.ArrayList;
import java.util.List;

import static com.brb.breban.endcript.dataAccess.DatabaseHelper.TABLE_CONVERSATIONS;

/**
 * Created by breban on 30.04.2017.
 */

public class ConversationDao implements DAO{
    private DatabaseHelper db;
    private ContactDao contactDao;

    public static final String KEY_CONTACT = "contact_id";
    public static final String KEY_ID = "id";

    public ConversationDao(Context context) {
        this.db = new DatabaseHelper(context);
        this.contactDao = new ContactDao(this.db);
    }

    public long addConversation(Conversation conversation){
        SQLiteDatabase db = this.db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CONTACT, conversation.getContact().getId());

        long id = db.insert(TABLE_CONVERSATIONS, null, values);
        db.close();
        return id;
    }

    public Conversation getConversation(int id){
        SQLiteDatabase db = this.db.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONVERSATIONS, new String[]{KEY_ID, KEY_CONTACT}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
        }

        Conversation conversation = new Conversation(Integer.parseInt(cursor.getString(0)), contactDao.getContact(Integer.parseInt(cursor.getString(1))));

        return conversation;
    }

    public Conversation getConversationByContactId(int id){
        SQLiteDatabase db = this.db.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONVERSATIONS, new String[]{KEY_ID, KEY_CONTACT}, KEY_CONTACT + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if(cursor != null && cursor.moveToFirst()){
            cursor.moveToFirst();
            return new Conversation(Integer.parseInt(cursor.getString(0)), contactDao.getContact(Integer.parseInt(cursor.getString(1))));
        }
        return null;
    }

    public List<Conversation> getAllConversations(){
        List<Conversation> conversationList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_CONVERSATIONS;

        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()){
            do{
                Conversation conversation= new Conversation(Integer.parseInt(cursor.getString(0)), contactDao.getContact(Integer.parseInt(cursor.getString(1))));
                conversationList.add(conversation);
            }while(cursor.moveToNext());
        }
        return conversationList;
    }

    public void deleteConversation(Conversation conversation){
        SQLiteDatabase db = this.db.getWritableDatabase();
        db.delete(TABLE_CONVERSATIONS, KEY_ID + "=?",
                new String[]{String.valueOf(conversation.getId())});
        db.close();
    }
}
