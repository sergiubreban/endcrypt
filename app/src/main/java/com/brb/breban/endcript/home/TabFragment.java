package com.brb.breban.endcript.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brb.breban.endcript.R;
import com.brb.breban.endcript.chat.ChatWindowActivity;
import com.brb.breban.endcript.dataAccess.dao.ContactDao;
import com.brb.breban.endcript.dataAccess.dao.InvitationDao;
import com.brb.breban.endcript.dataAccess.models.Contact;
import com.brb.breban.endcript.dataAccess.models.Invitation;
import com.brb.breban.endcript.googleFirebase.MyFirebaseMessagingService;
import com.brb.breban.endcript.login.VerificationActivity;
import com.brb.breban.endcript.util.DataCallback;
import com.brb.breban.endcript.util.SharedPreferencesUtils;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by breban on 29.04.2017.
 */

public class TabFragment extends Fragment{
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private ContactDao contactDao;
    private InvitationDao invitationDao;
    private List<Contact> contacts;
    private List<Invitation> invitations;
    private Activity context;
    private Resources res;
    private RequestQueue queue;

    public TabFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static TabFragment newInstance(int sectionNumber, Activity context) {
        TabFragment fragment = new TabFragment();
        fragment.setContext(context);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.contactDao = new ContactDao(this.context);
        this.contacts = contactDao.getAllContacts();
        this.invitationDao = new InvitationDao(this.context);
        this.invitations = invitationDao.all();
        this.queue = Volley.newRequestQueue(this.context);
        this.res = context.getResources();

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                showContactList(rootView, false);
                break;
            case 2:
                showInvitationList(rootView);
                break;
            case 3:
                showContactList(rootView, true);
            break;
        }


        return rootView;
    }

    private void reload(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }

    public void showInvitationList(View rootView){
        ArrayList<String> invitationslist = new ArrayList<>();
        for(Invitation invitation: invitations){
            invitationslist.add(invitation.getEmail());
        }

        EndCriptListAdapter adapter = new EndCriptListAdapter(this.context, invitationslist);
        ListView lv= (ListView)rootView.findViewById(R.id.contacts_list);
        lv.setAdapter(adapter);

        int numberOfItems = adapter.getCount();
        // Get total height of all items.
        int totalItemsHeight = 0;
        for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
            View item = adapter.getView(itemPos, null, lv);
            item.measure(0, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }
        // Get total height of all item dividers.
        int totalDividersHeight = lv.getDividerHeight() *
                (numberOfItems - 1);

        // Set list height.
        ViewGroup.LayoutParams params = lv.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        lv.setLayoutParams(params);
        lv.requestLayout();

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setTitle("Endcrypt friend request.");
                String email = invitations.get(position).getEmail();
                builder.setMessage("Email: " + email + "\nSet name for this contact.");

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final EditText nameBox = new EditText(context);
                nameBox.setHint("Name");
                layout.addView(nameBox);

                builder.setView(layout);
                final Invitation invitation = invitations.get(position);

                builder.setPositiveButton("next",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String name = nameBox.getText().toString();
                                    if(name.equals("")){
                                        nameBox.setError("Required field");
                                    }else{
                                        contactDao.addContact(new Contact(name, invitation.getEmail(), invitation.getSender_id(), invitation.getPublic_key()));
                                        invitationDao.delete(invitation);
                                        sendResponseNotification(invitation, true);
                                    }
                                }
                            });

                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResponseNotification(invitation, false);

                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    public void showContactList(View rootView, final Boolean encrypted){
        final ArrayList<String> contacts_names = new ArrayList<>();
        for(Contact contact: contacts){
            if(contact.getPublic_key() != null){
                contacts_names.add(contact.getName());
            }
        }
        EndCriptListAdapter adapter = new EndCriptListAdapter(this.context, contacts_names);
        ListView lv= (ListView)rootView.findViewById(R.id.contacts_list);
        lv.setAdapter(adapter);

        int numberOfItems = adapter.getCount();
        // Get total height of all items.
        int totalItemsHeight = 0;
        for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
            View item = adapter.getView(itemPos, null, lv);
            item.measure(0, 0);
            totalItemsHeight += item.getMeasuredHeight();
        }
        // Get total height of all item dividers.
        int totalDividersHeight = lv.getDividerHeight() *
                (numberOfItems - 1);

        // Set list height.
        ViewGroup.LayoutParams params = lv.getLayoutParams();
        params.height = totalItemsHeight + totalDividersHeight;
        lv.setLayoutParams(params);
        lv.requestLayout();

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(context, ChatWindowActivity.class);
                for(Contact contact: contacts) {
                    if(contact.getName().equals(contacts_names.get(position))){
                        intent.putExtra("contact_id", "" + contact.getId());
                        break;
                    }
                }
                if(encrypted){
                    intent.putExtra("encrypted", true);
                }else{
                    intent.putExtra("encrypted", false);
                }
                startActivity(intent);
            }
        });
    }


    private void sendResponseNotification(final Invitation invitation, final Boolean accept){
        String url = res.getString(R.string.fcm_url);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                            reload();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject body = new JSONObject();
//                    JSONObject notification = new JSONObject();
                    JSONObject data = new JSONObject();
                    try {
                        data.put("type", "invitation");
                        data.put("invitation_type", "response");
                        if(accept){
                            SharedPreferences sharedPreferences = SharedPreferencesUtils.getInstance().getSharedpreferences();

                            data.put("accept", "true");
                            data.put("email", sharedPreferences.getString("email", "") /*invitation.getEmail()*/);
                            data.put("from_token", FirebaseInstanceId.getInstance().getToken());
                            data.put("public_key", sharedPreferences.getString("public_key", ""));
                            body.put("data", data);
                            body.put("to", invitation.getSender_id());
                        }else{
                            data.put("accept", "false");
                            body.put("data", data);
                            body.put("to", invitation.getSender_id());
                        }
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
    @Override
    public Context getContext() {
        return context;
    }

    public void setContext(Activity context) {
        this.context = context;
    }
}
