package com.brb.breban.endcript.login;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Activity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brb.breban.endcript.R;
import com.brb.breban.endcript.dataAccess.DatabaseHelper;
import com.brb.breban.endcript.home.HomeActivity;
import com.brb.breban.endcript.util.SharedPreferencesUtils;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.RequestQueue;

public class VerificationActivity extends Activity {
    private DatabaseHelper databaseHelper;
    private Resources res;
    private String token;
    private RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.databaseHelper = new DatabaseHelper(this);
        this.res = getResources();
        this.token = FirebaseInstanceId.getInstance().getToken();
        this.queue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_verification);
    }

    public void logIn(View view){
        String url = "http://" +  res.getString(R.string.service_host) + ":" + res.getString(R.string.service_port) + "/api/v1/userprofile";
        System.out.println(url);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObj = new JSONObject(response);

                            if(jsonObj.get("ok").toString().equals("true")){
                                showNextPopUp(true, jsonObj.get("status").toString());
                            }else{
                                showNextPopUp(false, "");
                            }
                        }catch(JSONException e){
                            Log.d("JSON Parse", e.toString());
                            showNextPopUp(false, "");
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                        showNextPopUp(false, "");
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject body = new JSONObject();
                    token = FirebaseInstanceId.getInstance().getToken();
                    String publicKeyString = SharedPreferencesUtils.getInstance().getSharedpreferences().getString("public_key", "");
                    Intent intent = getIntent();

                    String email = intent.getExtras().getString("email", "");
                    EditText editField = (EditText) findViewById(R.id.editField);
                    try {
                        body.put("verification_code", editField.getText().toString());
                        body.put("publicKey", publicKeyString);
                        body.put("email", email);
                        body.put("senderId", token);
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
        };
// Add the request to the RequestQueue.
        this.queue.add(stringRequest);

    }


    private void showNextPopUp(Boolean next, String status){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Endcrypt status");

        this.databaseHelper.reset();

        if(next){
            builder.setMessage("Your account has been " + status);

            builder.setPositiveButton("next",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = getIntent();
                            String email = intent.getExtras().getString("email", "");

                            SharedPreferences.Editor editor = SharedPreferencesUtils.getInstance().getSharedpreferences().edit();
                            editor.putString("email", email);
                            editor.putString("sender_id", token);
                            editor.commit();

                            Intent in = new Intent(VerificationActivity.this, HomeActivity.class);
                            startActivity(in);

                        }
                    });
        }else{
            builder.setMessage("An error has occurred, try again later.");
        }

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
