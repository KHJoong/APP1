package com.together.linkalk;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

/**
 * Created by kimhj on 2017-07-26.
 */

public class MyFriendList_Adapter extends BaseAdapter {

    Context mfContext;

    ArrayList<Member> mfItem;

    ViewHolder viewHolder;

    MyFriendList_Adapter(Context context){
        super();
        mfContext = context;
        mfItem = new ArrayList<Member>();
    }

    @Override
    public int getCount() {
        return mfItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mfItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if(view == null){
            view = ((LayoutInflater)mfContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.myfriend_frame, null);
            viewHolder = new ViewHolder();

            viewHolder.profilepic = (ImageView)view.findViewById(R.id.ivMyFriendPic);
            viewHolder.nickname = (TextView)view.findViewById(R.id.tvMyFriendNick);
            viewHolder.language = (TextView)view.findViewById(R.id.tvMyFriendLang);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)view.getTag();
        }

        String path = mfItem.get(position).getImgpath();
        String path2 = "http://www.o-ddang.com/linkalk/"+path;
        Uri uri = Uri.parse("http://www.o-ddang.com/linkalk/"+path);
        Glide.with(mfContext).load(uri).apply(RequestOptions.circleCropTransform()).into(viewHolder.profilepic);
        viewHolder.nickname.setText(mfItem.get(position).getNickname());
        viewHolder.language.setText(mfItem.get(position).getLanguage());

        return view;
    }

    public void addItem(Member member){
        mfItem.add(member);
    }
}
