package com.together.linkalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by kimhj on 2017-08-19.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "리시버 받음", Toast.LENGTH_SHORT).show();
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Toast.makeText(context, "이프 안에 옴", Toast.LENGTH_SHORT).show();
            Intent socketIntent = new Intent(context, SocketService.class);
            context.startService(socketIntent);
        }
    }
}
