package com.example.gdrivepasswordvault.cards;

import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CustomLayoutManager extends LinearLayoutManager {
    private int offScreenPageLimit = 20;
    public CustomLayoutManager(Context context) {
        super(context);
    }
    public CustomLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public void setOffscreenPageLimit(int extraPages) {
        this.offScreenPageLimit = extraPages;
    }
    @Override
    protected int getExtraLayoutSpace(RecyclerView.State state) {
        return super.getExtraLayoutSpace(state) * offScreenPageLimit;
    }

}
