package com.brb.breban.endcript.dataAccess.models;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brb.breban.endcript.R;
import com.brb.breban.endcript.dataAccess.dao.DAO;
import com.brb.breban.endcript.dataAccess.dao.MessageDao;
import com.brb.breban.endcript.dataAccess.interfaces.IConversation;
import com.brb.breban.endcript.encryption.OpenSSL;
import com.brb.breban.endcript.util.DataCallback;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by breban on 29.04.2017.
 */

public class Conversation implements IConversation{
    private long id;
    private Contact contact;
    private Resources res;
    private Context context;
    private RequestQueue queue;

    public Conversation(long id, Contact contact) {
        this.id = id;
        this.contact = contact;

    }
    public Conversation(Context context, Contact contact) {
        this.contact = contact;
        this.context = context;
        this.res = context.getResources();
        this.queue = Volley.newRequestQueue(this.context);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    @Override
    public void sendMessage(DAO dao, final Message message) {
        MessageDao messageDao = (MessageDao) dao;
        messageDao.addMessage(message);

        PublicKey publicKey = null;
        try {
            publicKey = OpenSSL.getInstance().loadPublicKeyFromString(contact.getPublic_key());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        String stringMsg = OpenSSL.getInstance().encrypt(publicKey, message.getText());

        sendNotification(stringMsg, false);
    }

    private void sendNotification(final String message, final Boolean refetch){
        String url = res.getString(R.string.fcm_url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObj = new JSONObject(response);

                            if((int)jsonObj.get("success") == 0 && refetch == false){
                                contact.fetch(context, new DataCallback() {
                                    @Override
                                    public void onSuccess(JSONObject result) {
                                        sendNotification( message, true);
                                    }
                                });

                            }
                        }catch(JSONException e){
                            Log.d("JSON Parse", e.toString());
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        if(refetch == false) {
                            sendNotification(message, true);
                        }
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject body = new JSONObject();
                    JSONObject notification = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {


                        notification.put("body", "new message");
                        notification.put("title", "Endcrypt notification");
                        data.put("type", "message");
                        data.put("message", message);
                        data.put("from_token", FirebaseInstanceId.getInstance().getToken());
                        body.put("data", data);
                        body.put("to", contact.getSender_id());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    final String mRequestBody = body.toString();
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes");
                    return null;
                }
            }
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Authorization", "key=" + res.getString(R.string.web_apy_key));
                return params;
            }
        };
        this.queue.add(stringRequest);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
        //set resource
        this.setRes(this.context.getResources());
        //set queue request
        this.setQueueByContext();
    }


    public void setRes(Resources res) {
        this.res = res;
    }

    public void setQueueByContext() {
        this.queue = Volley.newRequestQueue(this.context);
    }
}
