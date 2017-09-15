package com.together.linkalk;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kimhj on 2017-08-01.
 */

public class ChatCommunication_Adapter extends BaseAdapter {

    Context ccaContext;

    ArrayList<Chat> ccaItem;

    ViewHolderChat viewHolder;

    String savedDate;
    String[] savedDateDis;
    String savedYear;
    String savedMonth;
    String savedDay;
    String savedHour;
    String savedMin;
    String saved2Date;
    String[] saved2DateDis;
    String saved2Year;
    String saved2Month;
    String saved2Day;




    ChatCommunication_Adapter(Context context){
        super();
        ccaContext = context;
        ccaItem = new ArrayList<Chat>();
    }

    @Override
    public int getCount() {
        return ccaItem.size();
    }

    @Override
    public Object getItem(int position) {
        return ccaItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        String sender = ccaItem.get(position).getSender();
        SharedPreferences sharedPreferences = ccaContext.getSharedPreferences("maintain", ccaContext.MODE_PRIVATE);
        String nickname = sharedPreferences.getString("nickname", "");

        if(view == null){
            view = ((LayoutInflater)ccaContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.chatcommuni_me_frame, null);
            viewHolder = new ViewHolderChat();

            viewHolder.pic = (ImageView)view.findViewById(R.id.iv_my_pic);
            viewHolder.sender = (TextView)view.findViewById(R.id.tv_my_nick);
            viewHolder.msg = (TextView)view.findViewById(R.id.tv_my_communi);
            viewHolder.time = (TextView)view.findViewById(R.id.tv_my_time);
            viewHolder.date = (TextView)view.findViewById(R.id.tv_date);

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolderChat) view.getTag();
        }

        // 채팅방 날짜 변하는날 날짜 알려주는 부분
        if(position==0){
            savedDate = ccaItem.get(position).getTime();
            savedDateDis = savedDate.split("/");
            savedYear = savedDateDis[0];
            savedMonth = savedDateDis[1];
            savedDay = savedDateDis[2];
            savedHour = savedDateDis[3];
            savedMin = savedDateDis[4];

            viewHolder.date.setVisibility(View.VISIBLE);
            viewHolder.date.setText(savedYear + "년 " + savedMonth + "월 " + savedDay + "일");
        } else {
            saved2Date = ccaItem.get(position-1).getTime();
            saved2DateDis = saved2Date.split("/");
            saved2Year = saved2DateDis[0];
            saved2Month = saved2DateDis[1];
            saved2Day = saved2DateDis[2];

            savedDate = ccaItem.get(position).getTime();
            savedDateDis = savedDate.split("/");
            savedYear = savedDateDis[0];
            savedMonth = savedDateDis[1];
            savedDay = savedDateDis[2];
            savedHour = savedDateDis[3];
            savedMin = savedDateDis[4];

            if(saved2Year.equals(savedYear) && saved2Month.equals(savedMonth) && saved2Day.equals(savedDay)){
                viewHolder.date.setVisibility(View.GONE);
            } else {
                viewHolder.date.setVisibility(View.VISIBLE);
                viewHolder.date.setText(savedYear + "년 " + savedMonth + "월 " + savedDay + "일");
            }
        }

        // 채팅방 메시지 띄워주는 부분
        if(sender.equals(nickname)){
            viewHolder.pic.setVisibility(View.GONE);
            viewHolder.sender.setText(ccaItem.get(position).getSender());
            viewHolder.msg.setText(ccaItem.get(position).getTransmsg());
            viewHolder.time.setText(savedHour + " : " + savedMin);

            viewHolder.msg.setBackgroundResource(R.drawable.mine);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            viewHolder.msg.setLayoutParams(params);

            viewHolder.sender.setGravity(Gravity.RIGHT);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.LEFT_OF, viewHolder.msg.getId());
            params.rightMargin = 15;
            viewHolder.time.setLayoutParams(params);
            viewHolder.time.setGravity(Gravity.CENTER_VERTICAL);

        }  else if(!sender.equals(nickname)){
            viewHolder.pic.setVisibility(View.VISIBLE);
            String path = ccaItem.get(position).getPath();
            Uri uri = Uri.parse("http://www.o-ddang.com/linkalk/"+path);
            Glide.with(ccaContext).load(uri).apply(RequestOptions.circleCropTransform()).into(viewHolder.pic);
            viewHolder.sender.setText(ccaItem.get(position).getSender());
            if(ccaItem.get(position).getIsTrans()){
                viewHolder.msg.setText(ccaItem.get(position).getTransmsg());
            } else {
                viewHolder.msg.setText(ccaItem.get(position).getMsg());
            }
            viewHolder.time.setText(savedHour + " : " + savedMin);
            viewHolder.msg.setBackgroundResource(R.drawable.other);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.RIGHT_OF, viewHolder.msg.getId());
            params.leftMargin = 15;
            viewHolder.time.setLayoutParams(params);
            viewHolder.time.setGravity(Gravity.CENTER_VERTICAL);

            viewHolder.sender.setGravity(Gravity.LEFT);

            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            params.leftMargin = 10;
            viewHolder.pic.setLayoutParams(params);
            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.RIGHT_OF, viewHolder.pic.getId());
            params.leftMargin = 10;
            viewHolder.msg.setLayoutParams(params);

//            params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//            params.leftMargin = 40;
//            viewHolder.msg.setLayoutParams(params);

        }

        return view;
    }

    public void addItem(Chat chat){
        ccaItem.add(chat);
    }

    public void add(Chat chat) { ccaItem.add(0, chat); }
}
