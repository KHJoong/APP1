package com.together.linkalk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.zip.CRC32;

/**
 * Created by kimhj on 2017-07-06.
 */

public class MainChatFragment extends Fragment {

    ListView lvChat;
    ChatList_Adapter clAdapter;

    public MainChatFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = (RelativeLayout)inflater.inflate(R.layout.main_chat_activity, container, false);
        lvChat = (ListView)layout.findViewById(R.id.lvChat);
        clAdapter = new ChatList_Adapter(getActivity().getApplicationContext());
        lvChat.setAdapter(clAdapter);

        lvChat.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room room = (Room)parent.getItemAtPosition(position);
                String roomName = room.getRoomName();

                Intent intent = new Intent(getActivity().getApplicationContext(), InChattingActivity.class);
                intent.putExtra("Receiver", roomName);
                startActivity(intent);
            }
        });

        ShowRoom showRoom = new ShowRoom(getActivity().getApplicationContext(), lvChat, clAdapter);
        showRoom.start();

        return layout;
    }

    // 저장된 채팅방 처음에 불러오는 쓰레드
    static class ShowRoom extends Thread{
        Context mContext;
        ListView listView;
        ChatList_Adapter cla;

        Handler handler = new Handler();

        public ShowRoom(Context context, ListView lv, ChatList_Adapter ad){
            mContext = context;
            listView = lv;
            cla = ad;
        }

        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MsgDBHelper dbHelper = new MsgDBHelper(mContext);
                    SQLiteDatabase db = dbHelper.getReadableDatabase();

                    String selectQuery = "SELECT * FROM chat_room ORDER BY ordered ASC;";
                    Cursor c1 = db.rawQuery(selectQuery, null);

                    while (c1.moveToNext()){
                        String roomName = "";
                        int numMember = 0;
                        String lastCom = "";
                        String lastComTime = "";

                        int roomNo = c1.getInt(c1.getColumnIndex("roomNo"));
                        String relation = c1.getString(c1.getColumnIndex("relation"));

                        SharedPreferences sp = mContext.getSharedPreferences("maintain", Context.MODE_PRIVATE);
                        String my_nickname = sp.getString("nickname", "");

                        String[] rel = relation.split("/");
                        numMember = rel.length - 1;
                        for(int i=0; i<rel.length; i++){
                            if(!rel[i].equals(my_nickname)){
                                if(roomName.equals("")){
                                    roomName = roomName + rel[i];
                                } else {
                                    roomName = ", " + roomName + rel[i];
                                }
                            }
                        }

                        String selectQuery2 = "SELECT * FROM chat_msg WHERE roomNo='"+ roomNo +"';";
                        Cursor c2 = db.rawQuery(selectQuery2, null);
                        int lastMsgNum = c2.getCount();
                        while(c2.moveToNext()){
                            if(c2.getInt(c2.getColumnIndex("msgNo")) == (lastMsgNum-1)){
                                lastCom = c2.getString(c2.getColumnIndex("message"));
                                lastComTime = c2.getString(c2.getColumnIndex("time"));
                            }
                        }

                        Room room = new Room(roomNo, roomName, numMember, lastCom, lastComTime);
                        cla.addItem(room);

                        c2.close();
                    }
                    listView.setAdapter(cla);
                    c1.close();
                    db.close();
                    dbHelper.close();
                }
            });
        }
    } // 채팅방 불러오는 Thread End
}
