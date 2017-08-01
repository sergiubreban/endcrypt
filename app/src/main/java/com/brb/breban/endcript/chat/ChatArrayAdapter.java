package com.brb.breban.endcript.chat;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.brb.breban.endcript.R;
import com.brb.breban.endcript.dataAccess.models.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by breban on 30.04.2017.
 */

public class ChatArrayAdapter extends ArrayAdapter {
    private TextView textView;
    private List<Message> messageList;
    private Activity context;

    public ChatArrayAdapter(Activity context, int resource, List<Message> messageList) {
        super(context, resource);
        this.messageList = new ArrayList<>();
        for(Message message: messageList){
            this.add(message);
        }
        this.context = context;
    }
    public void add(Message message){
        this.messageList.add(message);
        super.add(message);
    }

    public View getView(int position, View convertView, ViewGroup parent){
        Message chatMessageObj = this.messageList.get(position);
        LayoutInflater inflater=context.getLayoutInflater();
        View row=inflater.inflate(R.layout.contact_list, null,true);

        if(chatMessageObj.getIsMine()){
            row = inflater.inflate(R.layout.sent_message, parent, false);
        }else{
            row = inflater.inflate(R.layout.received_message, parent, false);
        }
        textView = (TextView)row.findViewById(R.id.message);
        textView.setText(chatMessageObj.getText());
        return row;
    }
}
