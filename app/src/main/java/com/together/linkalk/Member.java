package com.together.linkalk;

/**
 * Created by kimhj on 2017-07-18.
 */

public class Member {
    String nickname;
    String location;
    String language;
    String lastTime;
    String introduce;
    String hobby1;
    String hobby2;
    String hobby3;
    String hobby4;
    String hobby5;
    String imgpath;

    boolean checked;

    Member(String nick, String loc, String lan, String time, String intro, String ho1, String ho2, String ho3, String ho4, String ho5, String pa){
        nickname = nick;
        location = loc;
        language = lan;
        lastTime = time;
        introduce = intro;
        hobby1 = ho1;
        hobby2 = ho2;
        hobby3 = ho3;
        hobby4 = ho4;
        hobby5 = ho5;
        imgpath = pa;
    }

    public String getNickname(){
        return nickname;
    }

    public String getLocation(){
        return location;
    }

    public String getLanguage(){
        return language;
    }

    public String getLastTime(){
        return lastTime;
    }

    public String getIntroduce(){
        return introduce;
    }

    public String getHobby1(){
        return hobby1;
    }

    public String getHobby2(){
        return hobby2;
    }

    public String getHobby3(){
        return hobby3;
    }

    public String getHobby4(){
        return hobby4;
    }

    public String getHobby5(){
        return hobby5;
    }

    public String getImgpath(){
        return imgpath;
    }

    public boolean getChecked() {
        return checked;
    }

    public void setChecked(boolean c){
        checked = c;
    }

}
