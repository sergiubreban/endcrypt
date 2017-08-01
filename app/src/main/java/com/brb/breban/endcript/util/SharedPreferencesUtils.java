package com.brb.breban.endcript.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by breban on 26.05.2017.
 */

public class SharedPreferencesUtils {

    private static SharedPreferencesUtils sharedPreferencesUtilsInstance = null;
    private SharedPreferences sharedpreferences;
    public static String PRIVATE_KEY_FILE_PATH = "Private.asc";
    private Context context;

    public static SharedPreferencesUtils getInstance(){

        if(sharedPreferencesUtilsInstance == null){
            sharedPreferencesUtilsInstance = new SharedPreferencesUtils();
        }
        return sharedPreferencesUtilsInstance;
    }

    public Boolean areEncryptionKeysGenerated(){
            File file = new File(context.getFilesDir(), PRIVATE_KEY_FILE_PATH);

            //Read text from file
            StringBuilder text = new StringBuilder();

            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;

                if ((line = br.readLine()) != null) {
                   br.close();
                   return true;
                }else{
                    br.close();
                    return false;
                }
            }
            catch (IOException e) {
                Log.d("SharedPreferenceUtil", "error read file:" + e.toString());
            }

            return false;
    }

    public void setContext(Context context){
        this.context = context;
    }

    public Context getContext(){
        return this.context;
    }
    public SharedPreferences getSharedpreferences() {
        return sharedpreferences;
    }

    public void setSharedpreferences(SharedPreferences sharedpreferences) {
        this.sharedpreferences = sharedpreferences;
    }
}
