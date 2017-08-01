package com.brb.breban.endcript.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.brb.breban.endcript.R;
import com.brb.breban.endcript.dataAccess.DatabaseHelper;
import com.brb.breban.endcript.dataAccess.dao.ContactDao;
import com.brb.breban.endcript.dataAccess.models.Contact;
import com.brb.breban.endcript.encryption.AsyncKeyGenerator;
import com.brb.breban.endcript.home.HomeActivity;
import com.brb.breban.endcript.util.SharedPreferencesUtils;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class LogInActivity extends Activity {
    private EditText emailField;
    private RequestQueue queue;
    private Resources res;
    private ContactDao db;
    private AsyncKeyGenerator asyncKeyGenerator;
    public static final String MyPREFERENCES = "MyPofile" ;
    private SharedPreferences sharedpreferences;
    //get token
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferencesUtils.getInstance().setContext(this);
        SharedPreferencesUtils.getInstance().setSharedpreferences(sharedpreferences);
        final LogInActivity _this = this;

        asyncKeyGenerator = new AsyncKeyGenerator(new HashMap<String, String>(), new AsyncResponse() {
            @Override
            public void processFinish() {
                canLogin();
            }
        });
        asyncKeyGenerator.execute();
        if (!sharedpreferences.getString("email", "").equals("")) {
            Intent in = new Intent(LogInActivity.this, HomeActivity.class);
            startActivity(in);
        }
        this.queue = Volley.newRequestQueue(this);
        this.db = new ContactDao(this);
        this.res = getResources();
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//        TelephonyManager tMgr =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
//        String mPhoneNumber = tMgr.getLine1Number();

        emailField = (EditText) findViewById(R.id.editText);
    }

    public void canLogin(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //hide elements
                ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
                progressBar.setVisibility(View.INVISIBLE);
                TextView waitText = (TextView) findViewById(R.id.waitText);
                waitText.setVisibility(View.INVISIBLE);

                Button logInButton = (Button)findViewById(R.id.logInButton);
                logInButton.setClickable(true);
            }
        });
    }
    public void sendVerificationCode(View view){
        String url = "http://" +  res.getString(R.string.service_host) + ":" + res.getString(R.string.service_port) + "/api/v1/userprofile/vcode";

        final String email = emailField.getText().toString().trim();
        if(isValidEmail(email)){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObj = new JSONObject(response);
                            if(jsonObj.get("ok").toString().equals("true")){
                                Intent in = new Intent(LogInActivity.this, VerificationActivity.class);
                                in.putExtra("email", email);
                                startActivity(in);
                            }else{
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
                        Log.d("Error.Response", error.toString());
                    }
                }
        ) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    JSONObject body = new JSONObject();
                    try {

                        body.put("email", email);
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
        }else{
            emailField.setError("Email is not valid!");
        }

    }

    public final boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

/*
    public static String getContactName(Context context, String phoneNumber) {
        String contactName = null;
        final ArrayList<String> listNumbers = new ArrayList<String>();
        final Cursor cursor = context.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null );
        while ( cursor.moveToNext() ) {
            final int phone_id = cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER );
            String phone = cursor.getString( phone_id );
            phone = phone.replaceAll(" ","");

            if(phone.startsWith("*")){
            }else{
                if(!phone.startsWith("+4")){
                    phone = "+4" + phone;
                }
                if(phone.equals(phoneNumber)){
                    final int name_id = cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    return cursor.getString(name_id);
                }
            }
        }

        return null;
    }
    private ArrayList<String> getAllMobileNumbers(final Context context ) {
        final ArrayList<String> listNumbers = new ArrayList<String>();
        final Cursor cursor = context.getContentResolver().query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null );
        while ( cursor.moveToNext() ) {
            final int phone_id = cursor.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER );
            String phone = cursor.getString( phone_id );
            phone = phone.replaceAll(" ","");

            if(phone.startsWith("*")){
            }else{
                if(!phone.startsWith("+4")){
                    phone = "+4" + phone;
                }
                listNumbers.add( phone );
            }
        }

        return listNumbers;
    }*/
}
