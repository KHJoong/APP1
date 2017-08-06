package com.together.linkalk;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by kimhj on 2017-08-01.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private final String TAG = "MyFirebaseIDService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.i(TAG, token);
    }
}
