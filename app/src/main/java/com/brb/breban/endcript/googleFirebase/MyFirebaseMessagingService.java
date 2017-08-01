package com.brb.breban.endcript.googleFirebase;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.brb.breban.endcript.R;
import com.brb.breban.endcript.chat.ChatWindowActivity;
import com.brb.breban.endcript.dataAccess.dao.ContactDao;
import com.brb.breban.endcript.dataAccess.dao.ConversationDao;
import com.brb.breban.endcript.dataAccess.dao.InvitationDao;
import com.brb.breban.endcript.dataAccess.dao.MessageDao;
import com.brb.breban.endcript.dataAccess.models.Contact;
import com.brb.breban.endcript.dataAccess.models.Conversation;
import com.brb.breban.endcript.dataAccess.models.Invitation;
import com.brb.breban.endcript.dataAccess.models.Message;
import com.brb.breban.endcript.encryption.OpenSSL;
import com.brb.breban.endcript.login.LogInActivity;
import com.brb.breban.endcript.util.SharedPreferencesUtils;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.spongycastle.crypto.InvalidCipherTextException;

import java.io.IOException;

/**
 * Created by breban on 09.04.2017.
 */


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String INTENT_FILTER = "INTENT_FILTER";
    public static final String INTENT_REQUEST_FILTER = "FRIEND_REQUEST";
    private static final String TAG = "MyFirebaseMsgService";
    private ContactDao contactDao;
    private InvitationDao invitationDao;
    private MessageDao messageDao;
    private ConversationDao conversationDao;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //save message and send notification if chatAcivity is not in foreground
        String type = remoteMessage.getData().get("type");
        if(type.equals("message")){
            saveMessage(remoteMessage);
        } else if (type.equals("invitation")) {
            handleInvitation(remoteMessage);
        }
    }

    private void handleInvitation(RemoteMessage remoteMessage){
        String invitationType = remoteMessage.getData().get("invitation_type");
        if(contactDao == null){
            contactDao = new ContactDao(this);
        }

        if(invitationType.equals("response")){
            Contact contact = contactDao.getPartialContactByEmail(remoteMessage.getData().get("email"));
            if(contact == null){
                contact = new Contact(remoteMessage.getData().get("email"),remoteMessage.getData().get("email"));
            }
            Boolean accept = remoteMessage.getData().get("accept").equals("true")? true: false;
            if(accept){
                contact.setPublic_key(remoteMessage.getData().get("public_key"));
                contact.setSender_id(remoteMessage.getData().get("from_token"));
                contactDao.updateOrCreateContact(contact);
            }else{
                contactDao.deleteContact(contact);
            }
        }else if(invitationType.equals("request")){
            if(invitationDao == null){
                invitationDao = new InvitationDao(this);
            }
            if(!invitationDao.checkInvitationByEmail(remoteMessage.getData().get("email"))){
                Long id = invitationDao.add(new Invitation(remoteMessage.getData().get("email"), remoteMessage.getData().get("sender_id"), remoteMessage.getData().get("public_key")));
            }

            Intent intent = new Intent(INTENT_REQUEST_FILTER);
            sendBroadcast(intent);
        }

    }
    private void saveMessage(RemoteMessage remoteMessage){
        if(contactDao == null){
            contactDao = new ContactDao(this);
        }
        if(conversationDao == null){
            conversationDao = new ConversationDao(this);
        }
        if(messageDao == null){
            messageDao = new MessageDao(this);
        }
        Contact contact = contactDao.getContactByToken(remoteMessage.getData().get("from_token"));
        Message message = null;
        if(contact != null){
            Conversation conversation = conversationDao.getConversationByContactId(contact.getId());
            if(conversation != null){
                if(SharedPreferencesUtils.getInstance().getSharedpreferences() == null){
                    SharedPreferencesUtils.getInstance().setSharedpreferences(getSharedPreferences("MyPofile", Context.MODE_PRIVATE));
                    SharedPreferencesUtils.getInstance().setContext(getApplicationContext());
                }
                String decodedMsg = null;
                try {
                    decodedMsg = OpenSSL.getInstance().decrypt(remoteMessage.getData().get("message"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InvalidCipherTextException e) {
                    e.printStackTrace();
                }
                message = new Message(conversation, decodedMsg, false);
                int id = (int)messageDao.addMessage(message);
                message.setId(id);

            }

            if(ChatWindowActivity.isActivityVisible() &&
                    (Integer.parseInt(ChatWindowActivity.getContactId()) == conversation.getContact().getId()) ){
                Intent intent = new Intent(INTENT_FILTER + conversation.getContact().getId());
                intent.putExtra("message_id", ""+message.getId());
                sendBroadcast(intent);
            }else{
                sendNotification(remoteMessage);
            }
        }
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Endcrypt")
                .setContentText("New message"/*remoteMessage.getNotification().getBody()*/)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification note = notificationBuilder.build();
        note.defaults |= Notification.DEFAULT_VIBRATE;
        note.defaults |= Notification.DEFAULT_SOUND;
        notificationManager.notify(0, note);
    }
}