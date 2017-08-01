package com.brb.breban.endcript.dataAccess.models;

import android.content.Context;

import com.brb.breban.endcript.dataAccess.dao.ContactDao;
import com.brb.breban.endcript.util.DataCallback;

/**
 * Created by breban on 23.04.2017.
 */

public class Contact {
    private int id;
    private String name;
    private String email;
    private String sender_id;
    private String public_key;
    private ContactDao contactDao;

    public Contact(){

    }

    public Contact(int id, String name, String email, String sender_id, String public_key) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.sender_id = sender_id;
        this.public_key = public_key;
    }

    public Contact(String name, String email, String sender_id, String public_key) {
        this.name = name;
        this.email = email;
        this.sender_id = sender_id;
        this.public_key = public_key;
    }

    public Contact(String name, String email){
        this.name = name;
        this.email = email;
    }
    public Contact(int id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public void fetch(Context context, DataCallback callback){
        if(this.contactDao == null){
            this.contactDao = new ContactDao(context);
        }
        contactDao.fetch(this, callback);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email= email;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getPublic_key() {
        return public_key;
    }

    public void setPublic_key(String public_key) {
        this.public_key = public_key;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", sender_id='" + sender_id + '\'' +
                ", public_key='" + public_key + '\'' +
                '}';
    }
}
