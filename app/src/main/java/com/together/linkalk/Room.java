package com.together.linkalk;

/**
 * Created by kimhj on 2017-08-01.
 */

public class Room {
    String roomName;
    int numMember;
    String lastCommunication;
    String lastCommunicationTime;

    Room(String rn, int nm, String lc, String lct){
        roomName = rn;
        numMember = nm;
        lastCommunication = lc;
        lastCommunicationTime = lct;
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
}
