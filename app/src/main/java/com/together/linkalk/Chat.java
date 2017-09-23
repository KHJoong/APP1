package com.together.linkalk;

/**
 * Created by kimhj on 2017-08-01.
 */

public class Chat {
    String sender;
    String Receiver;
    int type;
    String msg;
    String transmsg;
    String time;
    int sync;
    String path;

    boolean isTrans;

    Chat(String S, String R, int TY, String M, String TM, String T, int s){
        sender = S;
        Receiver = R;
        type = TY;
        msg = M;
        transmsg = TM;
        time = T;
        sync = s;
        isTrans = true;
    }

    Chat(String S, String R, int TY, String M, String TM, String T, int s, String P){
        sender = S;
        Receiver = R;
        type = TY;
        msg = M;
        transmsg = TM;
        time = T;
        sync = s;
        isTrans = true;
        path = P;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return Receiver;
    }

    public int getType() { return type; }

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

    public boolean getIsTrans() { return isTrans; }

    public String getPath() { return path; }

    public void setIsTrans() {
        if(isTrans){
            isTrans = false;
        } else {
            isTrans = true;
        }
    }
}
