package com.wisaterhunep.stock;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class StockViewHolder extends RecyclerView.ViewHolder {

    public TextView symbol;
    TextView name;
    TextView price;
    TextView priceChange;
    //TextView percentDiff;

    public StockViewHolder(View view) {
        super(view);
        symbol = (TextView) view.findViewById(R.id.stockSymbol);
        name = (TextView) view.findViewById(R.id.stockName);
        price = (TextView) view.findViewById(R.id.stockPrice);
        priceChange = (TextView) view.findViewById(R.id.priceDiff);
       // percentDiff = (TextView) view.findViewById(R.id.priceDiff);
    }
}
