package com.together.linkalk;

/**
 * Created by kimhj on 2017-08-01.
 */

public class Room {
    int roomNum;
    String roomName;
    String roomRelation;
    int numMember;
    String lastCommunication;
    String lastCommunicationTime;
    int numUnread;

    Room(int rnum, String rn, String rr, int nm, String lc, String lct, int nu){
        roomNum = rnum;
        roomName = rn;
        roomRelation = rr;
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

    public String getRoomRelation() {
        return roomRelation;
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
