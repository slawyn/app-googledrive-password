package com.example.gdrivepasswordvault.cards;

public class CardData implements Comparable<CardData> {
    String[] mData;


    public CardData(String website, String email, String account, String password, String comment, String date) {
        mData = new String[6];
        mData[0] = website;
        mData[1]  = email;
        mData[2]  = account;
        mData[3]  = password;
        mData[4]  = comment;
        mData[5]  = date;
    }

    public CardData(){
        mData = new String[6];
        mData[0] = "";
        mData[1]  = "";
        mData[2]  = "";
        mData[3]  = "";
        mData[4]  = "";
        mData[5]  = "";

    }

    @Override
    public int compareTo(CardData dataelement) {
        return  mData[0].compareTo(dataelement. mData[0]);
    }
}
