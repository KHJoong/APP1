package com.together.linkalk;

/**
 * Created by kimhj on 2017-08-01.
 */

public class Room {
    int roomNum;
    String roomName;
    int numMember;
    String lastCommunication;
    String lastCommunicationTime;
    int numUnread;

    Room(int rnum, String rn, int nm, String lc, String lct, int nu){
        roomNum = rnum;
        roomName = rn;
        numMember = nm;
        lastCommunication = lc;
        lastCommunicationTime = lct;
        numUnread = nu;
    }

    public int getRoomNum() {
         return roomNum;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getNumMember() {
        return numMember;
    }

    public String getLastCommunication() {
        return lastCommunication;
    }

    public String getLastCommunicationTime() {
        return lastCommunicationTime;
    }

    public int getNumUnread(){
        return numUnread;
    }
}
