package com.together.linkalk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kimhj on 2017-08-01.
 */

public class ChatList_Adapter extends BaseAdapter {

    Context claContext;

    ArrayList<Room> claItem;

    ViewHolderRoom viewHolder;

    ChatList_Adapter(Context context){
        super();
        claContext = context;
        claItem = new ArrayList<Room>();
    }

    @Override
    public int getCount() {
        return claItem.size();
    }

    @Override
    public Object getItem(int position) {
        return claItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = ((LayoutInflater)claContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chatlist_frame, null);
            viewHolder = new ViewHolderRoom();

            viewHolder.roomName = (TextView)view.findViewById(R.id.roomName);
            viewHolder.numMember = (TextView)view.findViewById(R.id.numMember);
            viewHolder.lastCommu = (TextView)view.findViewById(R.id.lastCommu);
            viewHolder.lastCommuTime = (TextView)view.findViewById(R.id.lastCommuTime);
            viewHolder.numUnread = (TextView)view.findViewById(R.id.numUnread);
            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolderRoom) view.getTag();
        }

        TimeZone timeZone = TimeZone.getTimeZone("Asia/Seoul");
        Date date = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy");
        dateFormat.setTimeZone(timeZone);
        String year = dateFormat.format(date);
        dateFormat = new SimpleDateFormat("MM");
        String month = dateFormat.format(date);
        dateFormat = new SimpleDateFormat("dd");
        String day = dateFormat.format(date);

        String last_time = null;
        String time = claItem.get(position).getLastCommunicationTime();
        String[] time_array = time.split("/");
        if(String.valueOf(year).equals(time_array[0]) && String.valueOf(month).equals(time_array[1]) && String.valueOf(day).equals(time_array[2])){
            last_time = time_array[3]+":"+time_array[4];
        } else {
            last_time = time_array[1]+"월 "+time_array[2]+"일";
        }

        viewHolder.roomName.setText(claItem.get(position).getRoomName());
        if(claItem.get(position).getNumMember()==1){
            viewHolder.numMember.setVisibility(View.GONE);
        } else {
            viewHolder.numMember.setText(String.valueOf(claItem.get(position).getNumMember()));
        }
        viewHolder.lastCommu.setText(claItem.get(position).getLastCommunication());
        viewHolder.lastCommuTime.setText(last_time);
        if(claItem.get(position).getNumUnread()==0){
            viewHolder.numUnread.setVisibility(View.GONE);
        } else {
            viewHolder.numUnread.setVisibility(View.VISIBLE);
            viewHolder.numUnread.setText(String.valueOf(claItem.get(position).getNumUnread()));
        }

        return view;
    }

    public void addItem(Room room){
        claItem.add(room);
    }
}
