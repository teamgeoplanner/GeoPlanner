package com.example.geoplanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.Calendar;

public class callServiceReceiver extends BroadcastReceiver {

    private static boolean ring=false;
    private static boolean callReceived=false;
    private String callerPhoneNumber;
    private Context saveContext;
    String number;

    @Override
    public void onReceive(Context mContext, Intent intent)
    {
        saveContext=mContext;
        // Get the current Phone State


        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if(state==null){
            return;
        }


        if(!intent.getExtras().containsKey(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
            return;
        }
        Bundle bundle = intent.getExtras();
        number = bundle.getString("incoming_number");


//        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        // If phone state "Rininging"
        if(state.equals(TelephonyManager.EXTRA_STATE_RINGING))
        {
            ring =true;
            // Get the Caller's Phone Number
            Toast.makeText(mContext, "ringing call : "+number, Toast.LENGTH_LONG).show();
        }



        // If incoming call is received
        if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
        {
            callReceived=true;
        }


        // If phone is Idle
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE))
        {
            // If phone was ringing(ring=true) and not received(callReceived=false) , then it is a missed call
            if(ring==true&&callReceived==false)
            {
//                if(callerPhoneNumber != null) {
                    Toast.makeText(mContext, "missed call : "+number, Toast.LENGTH_LONG).show();
//                }

                //workingWithFunctions();
                ring=false;
            }
            callReceived=false;
        }}
}
