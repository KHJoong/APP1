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
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kimhj on 2017-08-27.
 */

public class DialogInChatting_Adapter extends BaseAdapter {

    Context dcContext;

    ArrayList<Member> dcItem;

    ViewHolder viewHolder;

    DialogInChatting_Adapter(Context context){
        super();
        dcContext = context;
        dcItem = new ArrayList<Member>();
    }

    @Override
    public int getCount() {
        return dcItem.size();
    }

    @Override
    public Object getItem(int position) {
        return dcItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = ((LayoutInflater)dcContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.in_chat_room_info_frame, null);
            viewHolder = new ViewHolder();

            viewHolder.nickname = (TextView)view.findViewById(R.id.tvInChatMemNick);
            viewHolder.language = (TextView)view.findViewById(R.id.tvInChatMemLan);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.nickname.setText(dcItem.get(position).getNickname());
        viewHolder.language.setText(dcItem.get(position).getLanguage());

        return view;
    }

    public void addItem(Member member){
        dcItem.add(member);
    }

}
