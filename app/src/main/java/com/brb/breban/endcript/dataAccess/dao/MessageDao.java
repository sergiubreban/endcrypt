package com.brb.breban.endcript.dataAccess.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.brb.breban.endcript.dataAccess.DatabaseHelper;
import com.brb.breban.endcript.dataAccess.models.Conversation;
import com.brb.breban.endcript.dataAccess.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by breban on 30.04.2017.
 */

public class MessageDao implements DAO{
    private DatabaseHelper db;

    public static final String TABLE_MESSAGES = "messages";
    public static final String KEY_ID = "id";
    public static final String KEY_CONVERSATION= "conversation_id";
    public static final String KEY_TEXT= "text";
    public static final String KEY_IS_MINE= "`is_mine`";

    public MessageDao(Context context) {
        this.db = new DatabaseHelper(context);
    }

    public long addMessage(Message message){
        SQLiteDatabase db = this.db.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_CONVERSATION, String.valueOf(message.getConversation().getId()));
        values.put(KEY_TEXT, message.getText());
        values.put(KEY_IS_MINE, message.getIsMine() ? "true": "false");
        long id = db.insert(TABLE_MESSAGES, null, values);

        db.close();
        return id;
    }

    public Message getMessageById(String id){
        List<Message> messagesList = new ArrayList<>();

        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{KEY_TEXT, KEY_IS_MINE}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        Message message = null;

        try{
            if(cursor != null && cursor.moveToFirst()){
                message = new Message(Integer.parseInt(id), cursor.getString(0).toString(), Boolean.valueOf(cursor.getString(1)));
            }
        }catch (Exception e){
        }
        return message;
    }

    public List<Message> getMessagesByConversation(Conversation conversation){
        List<Message> messagesList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_MESSAGES;

        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{KEY_ID, KEY_TEXT, KEY_IS_MINE}, KEY_CONVERSATION + "=?",
                new String[]{String.valueOf(conversation.getId())}, null, null, null);

        if(cursor != null && cursor.moveToFirst()){
            do{
                try {
                    Message message = new Message(Integer.parseInt(cursor.getString(0)), conversation, cursor.getString(1).toString(), Boolean.valueOf(cursor.getString(2)));
                    messagesList.add(message);
                }catch (Exception e){

                }
            }while(cursor.moveToNext());
        }
        return messagesList;
    }

}
