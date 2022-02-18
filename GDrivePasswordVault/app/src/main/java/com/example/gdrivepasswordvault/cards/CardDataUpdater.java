package com.example.gdrivepasswordvault.cards;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.example.gdrivepasswordvault.Global;
import com.example.gdrivepasswordvault.cards.CardData;
import com.example.gdrivepasswordvault.cards.RecyclerViewAdapter;

import java.util.Date;

/**/
public class CardDataUpdater implements TextWatcher {

    private CardData carddata;
    private int subindex;
    private RecyclerViewAdapter.CardViewHolder holder;

    public CardDataUpdater(RecyclerViewAdapter.CardViewHolder holder, CardData carddata, int subindex){
        this.holder = holder;
        this.carddata = carddata;
        this.subindex = subindex;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        Log.d("+++++++++++++++",carddata.mData[subindex]+" <>"+ editable.toString());
        carddata.mData[subindex] = editable.toString();
        carddata.mData[5] = Global.mDateFormat.format(new Date());
        holder.updatePassiveFields(carddata.mData[0],carddata.mData[5]);
    }

}


