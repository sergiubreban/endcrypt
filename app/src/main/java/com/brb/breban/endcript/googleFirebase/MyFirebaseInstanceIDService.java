package com.brb.breban.endcript.googleFirebase;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.brb.breban.endcript.R;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Request;

import static com.brb.breban.endcript.login.LogInActivity.MyPREFERENCES;

/**
 * Created by breban on 09.04.2017.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {

        //Getting registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        //Displaying token on logcat
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        //You can implement this method to store the token on your server
        //Not required for current project
      /*  SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        if (!sharedpreferences.getString("phoneNumber", "").equals("")) {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("phoneNumber", sharedpreferences.getString("phoneNumber", ""))
                    .add("senderId", token)
                    .add("publicKey", sharedpreferences.getString("publicKey", ""))
                    .build();

            Request request = new Request.Builder()
                    .url("http://" + getResources().getString(R.string.service_host) + ":" + getResources().getString(R.string.service_port) + "/api/v1/userprofile")
                    .build();

            try {
                client.newCall(request).execute();
            }catch (IOException e){
                e.printStackTrace();
            }


        }*/
    }
}