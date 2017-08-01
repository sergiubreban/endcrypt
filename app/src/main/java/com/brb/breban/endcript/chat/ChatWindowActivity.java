package com.brb.breban.endcript.chat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.brb.breban.endcript.R;
import com.brb.breban.endcript.dataAccess.dao.ContactDao;
import com.brb.breban.endcript.dataAccess.dao.ConversationDao;
import com.brb.breban.endcript.dataAccess.dao.MessageDao;
import com.brb.breban.endcript.dataAccess.models.Contact;
import com.brb.breban.endcript.dataAccess.models.Conversation;
import com.brb.breban.endcript.dataAccess.models.Message;
import com.brb.breban.endcript.encryption.OpenSSL;
import com.brb.breban.endcript.googleFirebase.MyFirebaseMessagingService;
import com.brb.breban.endcript.util.SharedPreferencesUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class ChatWindowActivity extends Activity {
    private static boolean activityVisible;
    private ConversationDao conversationDao;
    private ContactDao contactDao;
    private MessageDao messageDao;
    private Conversation conversation;
    private ListView listView;
    private EditText chatText;
    private ChatArrayAdapter chatArrayAdapter;
    private ImageButton send_button;
    private static String contact_id;
    private Contact contact;
    private BroadcastReceiver myReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);
        this.conversationDao = new ConversationDao(this);
        this.contactDao = new ContactDao(this);
        this.messageDao = new MessageDao(this);
        this.listView = (ListView)findViewById(R.id.msgview);
        this.send_button = (ImageButton)findViewById(R.id.send_button);
        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        chatText = (EditText) findViewById(R.id.newmsg);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendMessage();
                }
                return false;
            }
        });

        this.send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendMessage();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        Intent intent = getIntent();

        contact_id= intent.getExtras().getString("contact_id", "");
        if(!contact_id.equals("")){
            this.contact = contactDao.getContact(Integer.parseInt(contact_id));
            this.conversation = conversationDao.getConversationByContactId(Integer.parseInt(contact_id));
            if(this.conversation == null){
                this.conversation = new Conversation(this, this.contact);
                this.conversation.setId(this.conversationDao.addConversation(this.conversation));
            }else{
                this.conversation.setContext(this);
            }

        }
        List<Message> messages = messageDao.getMessagesByConversation(this.conversation);
        Boolean encrypted = intent.getExtras().getBoolean("encrypted");
        if(encrypted){
            chatText.setVisibility(View.INVISIBLE);
            send_button.setVisibility(View.INVISIBLE);
            messages = messages.subList(Math.max(messages.size() - 10, 0), messages.size());

            String myPublicKeyString = SharedPreferencesUtils.getInstance().getSharedpreferences().getString("public_key", "");
            String contactPublicKeyString = this.contact.getPublic_key();
            PublicKey myPublicKey = null;
            PublicKey contactPublicKey = null;
            try {
                myPublicKey = OpenSSL.getInstance().createPublicKeyFromString(myPublicKeyString);
                contactPublicKey = OpenSSL.getInstance().createPublicKeyFromString(contactPublicKeyString);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
            for(Message message: messages){
                if(message.getIsMine()){
                    message.setText(OpenSSL.getInstance().encrypt(contactPublicKey, message.getText()));
                }else{
                    message.setText(OpenSSL.getInstance().encrypt(myPublicKey, message.getText()));
                }
            }
        }
        this.chatArrayAdapter = new ChatArrayAdapter(this, R.layout.sent_message, messages);

        listView.setAdapter(chatArrayAdapter);

        //receiver for new messages when this activity is in foreground
        myReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msgId = intent.getExtras().getString("message_id");
                Message msg = messageDao.getMessageById(msgId);
                if(msg != null){
                    chatArrayAdapter.add(msg);
                }
            }
        };
        //register to that filter
        registerReceiver(myReceiver, new IntentFilter(MyFirebaseMessagingService.INTENT_FILTER + this.contact.getId()));
    }
    @Override
    public void onResume() {
        super.onResume();
        activityVisible = true;
    }

    @Override
    public  void onDestroy(){
        activityVisible = false;
        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static String getContactId() {
        return contact_id;
    }
    @Override
    protected void onPause() {
        super.onPause();
        activityVisible = false;
    }

    public boolean sendMessage(){
        Message message = new Message(this.conversation, chatText.getText().toString(), true);
        this.conversation.sendMessage(this.messageDao, message);
        chatArrayAdapter.add(message);
        chatText.setText("");
        return true;
    }

}
