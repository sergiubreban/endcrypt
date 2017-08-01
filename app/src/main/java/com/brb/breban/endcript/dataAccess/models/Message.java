package com.brb.breban.endcript.dataAccess.models;


/**
 * Created by breban on 29.04.2017.
 */

public class Message {
    private int id;
    private Conversation conversation;
    private String text;
    private Boolean isMine;

    public Message(int id, Conversation conversation, String text, Boolean isMine) {
        this.id = id;
        this.conversation = conversation;
        this.text = text;
        this.isMine = isMine;
    }

    public Message(Conversation conversation, String text, Boolean isMine) {
        this.conversation = conversation;
        this.text = text;
        this.isMine = isMine;
    }

    public Message(int id, String text, Boolean isMine) {
        this.id = id;
        this.text = text;
        this.isMine = isMine;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getIsMine() {
        return isMine;
    }

    public void setIsMine(Boolean isMine) {
        isMine = isMine;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversation=" + conversation +
                ", text='" + text + '\'' +
                ", isMine=" + isMine +
                '}';
    }
}
