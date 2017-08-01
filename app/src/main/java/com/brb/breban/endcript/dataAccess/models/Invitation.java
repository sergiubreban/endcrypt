package com.brb.breban.endcript.dataAccess.models;

/**
 * Created by brebanescu on 6/24/2017.
 */

public class Invitation {
    private int id;
    private String email;
    private String sender_id;
    private String public_key;

    public Invitation(int id, String email, String sender_id, String public_key) {
        this.id = id;
        this.email = email;
        this.sender_id = sender_id;
        this.public_key = public_key;
    }
    public Invitation(String email, String sender_id, String public_key) {
        this.email = email;
        this.sender_id = sender_id;
        this.public_key = public_key;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
