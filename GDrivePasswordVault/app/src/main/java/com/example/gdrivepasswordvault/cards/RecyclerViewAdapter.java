package com.example.gdrivepasswordvault.cards;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gdrivepasswordvault.Encryption;
import com.example.gdrivepasswordvault.Logger;
import com.example.gdrivepasswordvault.MainActivity;
import com.example.gdrivepasswordvault.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CardViewHolder>{

    private MainActivity act;
    private ArrayList<CardData> mCards;
    private ArrayList<FilteredElement> mFilteredOut;
    private String mFilter;

    public RecyclerViewAdapter(MainActivity act){
        this.act = act;
        mCards = new ArrayList<>();
        mFilteredOut = new ArrayList<>();
        mFilter ="";
        initializeData();
    }

    private void initializeData() {
       // http://blowfish.online-domain-tools.com/
    }

    private void selectionSort() {
        for (int i = mCards.size()-1; i >= 0; i--) {
            CardData min = mCards.get(i);
            int minId = i;
            for (int j = i - 1; j >= 0; j--) {
                if (mCards.get(j).compareTo(min) > 0) {
                    min = mCards.get(j);
                    minId = j;
                }
            }
            // swapping
            CardData temp = mCards.get(i);
            mCards.set(i, min);
            mCards.set(minId, temp);
        }
    }


    public boolean addNewCard(){
        boolean status = false;
        try {
            mCards.add(0, new CardData());
            notifyItemInserted(0);
            act.scrollToPosition(0);
            act.showNumberOfItems(getItemCount() +"");
            status = true;
        }catch(Exception e){
            Logger.log("Error: couldn't add card",false);
        }
        return status;
    }

    // TODO replace with selection sort
    public void rearrangeItems(int selected){
        CardData selectedCard=null;
        ArrayList<CardData> beforeSorting = new ArrayList<>();
        for(int idx=0;idx<mCards.size();idx++){
            if(idx == selected){
                selectedCard = mCards.get(idx);
            }
            beforeSorting.add(mCards.get(idx));
        }
        selectionSort();
        //Collections.sort(mCards);

        for (int idx = mCards.size() - 1; idx >= 0; idx--) {
            CardData cd = mCards.get(idx);
            int idxold = beforeSorting.indexOf(cd);
            //Log.d("<<<<",">"+ mCards.get(idx).mData[0]+" * " +idxold+" -> "+idx);
            notifyItemMoved(idxold, idx);
            beforeSorting.remove(idxold);
        }
        if(selectedCard !=null)
            act.scrollToPosition(mCards.indexOf(selectedCard));
    }

    public boolean removeCard(int index){
        boolean status = false;
        try {
            new AlertDialog.Builder(new ContextThemeWrapper(act, R.style.AlertDialogTheme))
                    .setTitle("Confirmation")
                    .setMessage("Do you really want to remove the item?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("Yes", (dialog, whichButton) -> {
                        notifyItemRemoved(index);
                        mCards.remove(index);
                        act.showNumberOfItems(getItemCount() +"");

                    })
                    .setNegativeButton("No", null).show();/**/

            status = true;
        }catch(Exception e){
            Logger.log("Error: couldn't remove card",false);
        }
        return status;
    }

    public byte[] getDataToUpload(){
        StringBuilder s = new StringBuilder();
        byte[] uploadData;
        try {

            ArrayList<CardData> uploadList = new ArrayList<>();
            for(int idx=0;idx<mCards.size();idx++){
                uploadList.add(mCards.get(idx));
            }

            for(int idx=(mFilteredOut.size())-1;idx>=0;idx--) {
                FilteredElement fe = mFilteredOut.get(idx);
                if(fe.idx < uploadList.size()) {
                    uploadList.add(fe.idx, fe.carddata);
                } else {
                    uploadList.add(fe.carddata);
                }
            }

            for (int i = 0; i < uploadList.size(); i++) {
                CardData data = uploadList.get(i);
                s.append(String.format(":%s\00%s\00%s\00%s\00%s\00%s\n", data.mData[0], data.mData[1], data.mData[2], data.mData[3], data.mData[4], data.mData[5]));
            }

            uploadData  = (s.toString()+"#"+ Encryption.md5(s.toString().getBytes())).getBytes();

        }catch(Exception e) {
            uploadData = null;
            e.printStackTrace();
            Logger.log("Exception getDataUpload!",false);
        }

        return uploadData;
    }

    public boolean parseDownloadedData(byte[] array){
        boolean status = false;
        int hashIndex = 0;
        String hash = "";
        for(int i=array.length-1;i>=0;i--){
            if(array[i]=='#'){
                hashIndex = i;
                break;
            }
            hash =(char)array[i] + hash;
        }

        /**/
        byte[] arrayWithouthash = Arrays.copyOf(array, hashIndex);
        String recalcuateHash = Encryption.md5(arrayWithouthash);

        if(!hash.equals(recalcuateHash)){
            Logger.log("Error: MD5 :"+recalcuateHash +" :"+hash,false);
            return false;
        }

        String[] datasplit = new String(arrayWithouthash).trim().split("\\r?\\n");

        try {
            mCards.clear();
            mFilteredOut.clear();
            for (int idx = 0; idx < datasplit.length; idx++) {
                int dataoffset = datasplit[idx].indexOf(":");

                String[] subdata = datasplit[idx].substring(dataoffset+1).split("\00");

                if(subdata.length==6)
                    mCards.add(new CardData(subdata[0],subdata[1],subdata[2],subdata[3],subdata[4],subdata[5]));
            }

            notifyDataSetChanged();
            act.showNumberOfItems(getItemCount() +"");
            status = true;

        }catch(Exception e){
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            Logger.log("Parsing Error "+sw.toString(),false);
        }

        return status;
    }

    public  boolean setFilter(String filter){
        String fltr = filter.toLowerCase();

        int preFilterSize = mCards.size();

        if(fltr.length()>mFilter.length()){

            // remove elements that don't match
            for (int idx = mCards.size() - 1; idx >= 0; idx--) {
                if (!mCards.get(idx).mData[0].contains(fltr)) {
                    mFilteredOut.add(new FilteredElement(idx, mCards.remove(idx)));
                }
            }

        } else {
            for (int idx = (mFilteredOut.size()) - 1; idx >= 0; idx--) {
                FilteredElement fe = mFilteredOut.get(idx);
                if (fe.carddata.mData[0].contains(fltr)) {
                    mFilteredOut.remove(idx);
                    if(fe.idx < mCards.size()) {
                        mCards.add(fe.idx, fe.carddata);
                    } else {
                        mCards.add(fe.carddata);
                    }

                }
            }
        }

        mFilter = filter;
        return preFilterSize!=mCards.size();

    }

    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        CardViewHolder holder = new CardViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_recycle, viewGroup, false));
        return holder;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {

            //Log.d("---------->",position+" "+mCards.get( holder.getAdapterPosition()).mData[0]);
            holder.carddata = mCards;

            holder.twebsiteTitle.setText(mCards.get( holder.getAdapterPosition()).mData[0]);
            holder.ewebsite.setText(mCards.get( holder.getAdapterPosition()).mData[0]);
            holder.eemail.setText(mCards.get( holder.getAdapterPosition()).mData[1]);
            holder.eaccount.setText(mCards.get( holder.getAdapterPosition()).mData[2]);
            holder.epassword.setText(mCards.get( holder.getAdapterPosition()).mData[3]);
            holder.ecomment.setText(mCards.get( holder.getAdapterPosition()).mData[4]);
            holder.tdate.setText(mCards.get( holder.getAdapterPosition()).mData[5]);

            holder.ewebsite.addTextChangedListener( new CardDataUpdater(holder,mCards.get(position),  0));
            holder.eemail.addTextChangedListener(   new CardDataUpdater(holder, mCards.get(position), 1));
            holder.eaccount.addTextChangedListener( new CardDataUpdater( holder,mCards.get(position), 2));
            holder.epassword.addTextChangedListener(new CardDataUpdater( holder,mCards.get(position), 3));
            holder.ecomment.addTextChangedListener( new CardDataUpdater(holder,mCards.get(position),  4));

            holder.cv.setOnLongClickListener(view -> {
                removeCard(holder.getAdapterPosition());
                return true;
            });
            holder.cv.setOnClickListener(view -> {

                if(holder.closed){
                    holder.note.setVisibility(LinearLayout.VISIBLE);
                    holder.closed = false;

                }else{
                    act.hideKeyboard();
                    holder.note.setVisibility(LinearLayout.GONE);
                    holder.closed = true;
                    rearrangeItems(-1);
                }
            });

            holder.copyButton.setOnClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) this.act.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Password",   holder.epassword.getText());
                clipboard.setPrimaryClip(clip);

                Logger.log("Copied Password to Clipboard",true);
            });
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        ArrayList<CardData> carddata;
        TextView twebsiteTitle;
        EditText ewebsite;
        EditText eemail;
        EditText eaccount;
        EditText epassword;
        EditText ecomment;
        TextView tdate;
        View note;
        Button copyButton;
        boolean closed;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = itemView.findViewById(R.id.cv);
            copyButton = itemView.findViewById(R.id.copyButton);
            note = itemView.findViewById(R.id.note);
            twebsiteTitle =  itemView.findViewById(R.id.websiteTitle);
            ewebsite =   itemView.findViewById(R.id.editTextWebsite);
            eemail =     itemView.findViewById(R.id.editTextEmail);
            epassword =  itemView.findViewById(R.id.editTextPassword);
            eaccount = itemView.findViewById(R.id.editTextAccount);
            ecomment = itemView.findViewById(R.id.editTextComment);
            tdate =    itemView.findViewById(R.id.date);

            note.setVisibility(LinearLayout.GONE);

            closed = true;
        }

        public void updatePassiveFields(String website, String date){
            twebsiteTitle.setText(website);
            tdate.setText(date);
        }
    }

    public class FilteredElement{
        int idx;
        CardData carddata;
        FilteredElement(int idx, CardData carddata ){
            this.idx = idx;
            this.carddata = carddata;
        }
    }
}
