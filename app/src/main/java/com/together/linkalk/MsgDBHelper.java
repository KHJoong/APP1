package com.together.linkalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.Adapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kimhj on 2017-08-01.
 */

public class MsgDBHelper extends SQLiteOpenHelper{

    public static final String DB_NAME = "Chat.db";
    public static final int DB_VERSION = 1;
    Context mContext;

    Handler handler;

    public MsgDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_room (roomNo INTEGER, relation TEXT, ordered INTEGER);");
        db.execSQL("CREATE TABLE IF NOT EXISTS chat_msg (roomNo INTEGER, msgNo INTEGER, sender TEXT, message TEXT, transmsg TEXT, time TEXT, readed INTEGER, sync INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertRoom(int no, String rel){
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM chat_room";
        Cursor cursor = db.rawQuery(query, null);
        int ct = cursor.getCount();

        String query2 = "SELECT * FROM chat_room where roomNo='"+no+"'";
        Cursor cursor2 = db.rawQuery(query2, null);
        if(cursor2.getCount() == 0){
            db.execSQL("INSERT INTO chat_room VALUES('"+no+"', '"+rel+"','"+ct+"');");
            db.close();
        }
    }

    public void insertMsg(String sender, String receiver, String receiver2, String msg, String transmsg, String time, int readed, int sync){
        SQLiteDatabase db = getWritableDatabase();

        // 어떤 방인지, 방 번호 찾는 쿼리
        String query = "SELECT roomNo, ordered FROM chat_room WHERE relation='"+receiver+"' OR relation='"+receiver2+"'";
        Cursor cursor = db.rawQuery(query, null);
        int rn = 0;
        int or = 0;
        if(cursor.moveToFirst()){
            rn =  cursor.getInt(cursor.getColumnIndex("roomNo"));
            or = cursor.getInt(cursor.getColumnIndex("ordered"));
        }

        // 메시지 수 세서 마지막 번호 찾는 쿼리
        String query2 = "SELECT msgNo FROM chat_msg WHERE roomNo='"+rn+"'";
        cursor = db.rawQuery(query2, null);
        int mn = cursor.getCount();

        // ' 저장 에러나는 부분 ''로 바꿔서 저장 가능한 String으로 변환
        String[] msgArray = msg.split("'");
        String saveMsg = "";
        for(int i=0; i<msgArray.length; i++){
            if(i==0){
                saveMsg = msgArray[i];
            } else if(i>0){
                saveMsg = saveMsg + "''" + msgArray[i];
            }
        }
        String[] transMsgArray = transmsg.split("'");
        String saveTransMsg = "";
        for(int i=0; i<transMsgArray.length; i++){
            if(i==0){
                saveTransMsg = transMsgArray[i];
            } else if(i>0){
                saveTransMsg = saveTransMsg + "''" + transMsgArray[i];
            }
        }

        // 메시지 가장 마지막 번호에 받은 메세지 추가하기
        db.execSQL("INSERT INTO chat_msg VALUES('"+rn+"', '"+mn+"', '"+sender+"', '"+saveMsg+"', '"+saveTransMsg+"', '"+time+"', '"+readed+"', '"+sync+"');");

        // 새로 메시지가 도착한 채팅방의 순위를 0으로 땡겨주는 부분
        // 먼저 채팅방의 순서를 읽어오고
        cursor = db.rawQuery("SELECT * FROM chat_room ORDER BY ordered ASC;", null);
        while(cursor.moveToNext()){
            // 새로 등록되는 메시지를 갖는 채팅방보다 위에 있는 채팅방의 순위를 1씩 늘려준다.
            if((cursor.getInt(cursor.getColumnIndex("roomNo")) != rn) && cursor.getInt(cursor.getColumnIndex("ordered"))<=or){
                int this_rn = cursor.getInt(cursor.getColumnIndex("roomNo"));
                int save_or = cursor.getInt(cursor.getColumnIndex("ordered")) + 1;
                db.execSQL("UPDATE chat_room SET ordered = '"+ save_or +"' WHERE roomNo = '"+ this_rn +"'");
            }
            // 새로 등록되는 메시지를 갖는 채팅방의 순위를 0으로 오린다.
            if(cursor.getInt(cursor.getColumnIndex("roomNo")) == rn){
                db.execSQL("UPDATE chat_room SET ordered = '0' WHERE roomNo='"+rn+"'");
                break;
            }
        }

        db.close();
    }

    // 대화방 처음 들어갈 때 메시지 로딩
    public void selectMsg(String my, String dis, ListView lv, ChatCommunication_Adapter ad){
        String my_nick = my;
        String dist = dis;

        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT roomNo FROM chat_room WHERE relation='"+dist+"'";
        Cursor cursor = db.rawQuery(query, null);
        int rn = 0;
        if(cursor.moveToFirst()){
            rn =  cursor.getInt(0);
        }

        query = "SELECT * FROM chat_msg WHERE roomNo='"+rn+"' ORDER BY msgNo DESC LIMIT 15";
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
            do{
                int mn = c.getInt(1);
                String sender = c.getString(2);
                String msg = c.getString(3);
                String transmsg = c.getString(4);
                String time = c.getString(5);

                String nick="";
                String imgpath="";
                SharedPreferences mainShared = mContext.getSharedPreferences("maintain", Context.MODE_PRIVATE);
                String my_nickname = mainShared.getString("nickname", "");
                if(!sender.equals(my_nickname)){
                    for(int i=0; ; i++) {
                        SharedPreferences myFriendShared = mContext.getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
                        if (myFriendShared.contains(String.valueOf(i))) {
                            String myFriend_tmp = myFriendShared.getString(String.valueOf(i), "");
                            try {
                                JSONObject friendObject = new JSONObject(myFriend_tmp);
                                nick = friendObject.getString("nickname");
                                if(sender.equals(nick)){
                                    imgpath = friendObject.getString("imgpath");
                                    break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                }

                if(TextUtils.isEmpty(imgpath)){
                    SharedPreferences sp = mContext.getSharedPreferences("tmpFriendPicPath", Context.MODE_PRIVATE);
                    imgpath = sp.getString(sender, "");
                }

                Chat chat = new Chat(sender, dist, msg, transmsg, time, 1, imgpath);
                ad.add(chat);
                db.execSQL("UPDATE chat_msg SET readed='2' WHERE roomNo='"+rn+"' and msgNo='"+mn+"'");
            }while(c.moveToNext());
        }
        cursor.close();
        c.close();
        db.close();
    }

    // 대화방에 들어와 있을 때, 새로 도착한 메시지 로딩
    public void continueSelectMsg(Handler handler, String my, String rel, final ListView listView, final ChatCommunication_Adapter ad){
        this.handler = handler;

        String dist = rel;

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT roomNo FROM chat_room WHERE relation='"+dist+"'";
        Cursor cursor = db.rawQuery(query, null);
        int rn = 0;
        if(cursor.moveToFirst()){
            rn =  cursor.getInt(0);
        }

        query = "SELECT * FROM chat_msg WHERE roomNo='"+rn+"' and readed='1' ORDER BY msgNo ASC";
        Cursor c = db.rawQuery(query, null);
        if(c.moveToFirst()){
            do{
                int mn = c.getInt(1);
                String sender = c.getString(2);
                String msg = c.getString(3);
                String transmsg = c.getString(4);
                String time = c.getString(5);

                String nick="";
                String imgpath="";
                SharedPreferences mainShared = mContext.getSharedPreferences("maintain", Context.MODE_PRIVATE);
                String my_nickname = mainShared.getString("nickname", "");
                if(!sender.equals(my_nickname)){
                    for(int i=0; ; i++) {
                        SharedPreferences myFriendShared = mContext.getSharedPreferences("MyFriend", Context.MODE_PRIVATE);
                        if (myFriendShared.contains(String.valueOf(i))) {
                            String myFriend_tmp = myFriendShared.getString(String.valueOf(i), "");
                            try {
                                JSONObject friendObject = new JSONObject(myFriend_tmp);
                                nick = friendObject.getString("nickname");
                                if(sender.equals(nick)){
                                    imgpath = friendObject.getString("imgpath");
                                    break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            break;
                        }
                    }
                }

                if(TextUtils.isEmpty(imgpath)){
                    SharedPreferences sp = mContext.getSharedPreferences("tmpFriendPicPath", Context.MODE_PRIVATE);
                    imgpath = sp.getString(sender, "");
                }

                Chat chat = new Chat(sender, dist, msg, transmsg, time, 1, imgpath);
                ad.addItem(chat);
                db.execSQL("UPDATE chat_msg SET readed='2' WHERE roomNo='"+rn+"' and msgNo='"+mn+"'");
            }while(c.moveToNext());
            this.handler.post(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(ad);
                    listView.setSelection(ad.getCount()-1);
                }
            });
        }

        cursor.close();
        c.close();
        db.close();
    }

}