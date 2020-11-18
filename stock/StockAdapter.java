package com.wisaterhunep.stock;

import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {

    private static final String TAG = "StockAdapter";
    private List<Stock> stockList;
    private MainActivity mainActivity;
    final String UPARROW = "&#9650";
    final String DOWNARROW = "&#9660";

    StockAdapter(List<Stock> list, MainActivity m){
        this.stockList = list;
        this.mainActivity = m;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d(TAG, "onCreateViewHolder: Creating View Holder and setting listeners");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_activity, parent, false);
        v.setOnLongClickListener(mainActivity);
        v.setOnClickListener(mainActivity);

        return new StockViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock s = stockList.get(position);
        int entryColor = Color.WHITE;
        holder.symbol.setText(s.getSymbol());
        holder.name.setText(s.getCompanyName());
        holder.price.setText(s.getRoundedPrice());

        if(Double.parseDouble(s.getChange()) < 0){
            holder.priceChange.setText(" " + s.getRoundedChange() + " (" + s.getRoundedChangePercent() + "%)" );
            entryColor = Color.RED;
        }else{
            holder.priceChange.setText(" " + s.getRoundedChange() + " (" + s.getRoundedChangePercent()+ "%)" );
            entryColor = Color.GREEN;
        }

        holder.symbol.setTextColor(entryColor);
        holder.name.setTextColor(entryColor);
        holder.price.setTextColor(entryColor);
        holder.priceChange.setTextColor(entryColor);

    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
