package com.together.linkalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by kimhj on 2017-08-19.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent socketIntent = new Intent(context, SocketService.class);
            context.startService(socketIntent);
        }
    }
}
