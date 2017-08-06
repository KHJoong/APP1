package com.together.linkalk;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by kimhj on 2017-07-18.
 */

public class NewMemberList_Adapter extends BaseAdapter {

    Context nmContext;

    ArrayList<Member> nmItem;

    ViewHolder viewHolder;

    NewMemberList_Adapter(Context context){
        super();
        nmContext = context;
        nmItem = new ArrayList<Member>();
    }

    @Override
    public int getCount() {
        return nmItem.size();
    }

    @Override
    public Object getItem(int position) {
        return nmItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = ((LayoutInflater)nmContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.newfriend_frame, null);
            viewHolder = new ViewHolder();

            viewHolder.nickname = (TextView)view.findViewById(R.id.nf_nick);
            viewHolder.language = (TextView)view.findViewById(R.id.nf_lang);
            viewHolder.lastTime = (TextView)view.findViewById(R.id.nf_time);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.nickname.setText(nmItem.get(position).getNickname());
        viewHolder.language.setText(nmItem.get(position).getLanguage());
        viewHolder.lastTime.setText(nmItem.get(position).getLastTime());

        return view;
    }

    public void addItem(Member member){
        nmItem.add(member);
    }
}
