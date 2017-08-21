package com.together.linkalk;

/**
 * Created by kimhj on 2017-08-01.
 */

public class Chat {
    String sender;
    String Receiver;
    String msg;
    String transmsg;
    String time;
    int sync;

    Chat(String S, String R, String M, String TM, String T, int s){
        sender = S;
        Receiver = R;
        msg = M;
        transmsg = TM;
        time = T;
        sync = s;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return Receiver;
    }

    public String getMsg() {
        return msg;
    }

    public String getTransmsg(){
        return transmsg;
    }

    public String getTime() {
        return time;
    }

    public int getSync() {
        return sync;
    }
}
