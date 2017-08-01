package com.brb.breban.endcript.encryption;

/**
 * Created by breban on 26.05.2017.
 */


import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.brb.breban.endcript.login.AsyncResponse;
import com.brb.breban.endcript.util.SharedPreferencesUtils;

import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class AsyncKeyGenerator extends AsyncTask<Void, Void, Void> {

    private HashMap<String, String> params;
    private AsyncResponse delegate;

    public AsyncKeyGenerator(HashMap<String, String> params, AsyncResponse delegate) {
        this.delegate = delegate;
        this.params = params;
    }

    public HashMap<String, String> getParams() {
        return this.params;
    }

    @Override
    protected Void doInBackground(Void... params) {

        String publicKey = "";
        String privateKey = "";
        try {
            if (!SharedPreferencesUtils.getInstance()
                    .areEncryptionKeysGenerated()) {
                OpenSSL.getInstance().createKeyPair(4096);
            }
            KeyPair keyPair = OpenSSL.getInstance().getKeyPair();
            publicKey = OpenSSL.getInstance().exportPublicKeyToString(keyPair.getPublic());
            privateKey = OpenSSL.getInstance().exportPrivateKeyToString(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferences sharedpreferences = SharedPreferencesUtils.getInstance().getSharedpreferences();

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("public_key", publicKey);
        editor.putString("private_key", privateKey);
        editor.commit();

        this.params.put("PUBLIC_KEY", publicKey);
        delegate.processFinish();

        return null;
    }


}