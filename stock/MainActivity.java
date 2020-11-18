package com.wisaterhunep.stock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "MainActivity";
    private static final String apiToken = "pk_b7c3a30c5dcb4e85b48607f7f6528a54";
    private List<Stock> stockList = new ArrayList<>();
    private HashMap<String, String> temporaryStockHashMap = new HashMap<String, String>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipe;
    private StockAdapter stockAdapter;
    private NameDownloader nameDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler);
        stockAdapter = new StockAdapter(stockList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipe = findViewById(R.id.swiper);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });

        readJSON();

        boolean hasNetwork = isConnected();

        if (!hasNetwork) {
            noNetworkAlert("UPDATE"); 

            for(String symbol: temporaryStockHashMap.keySet()){
                Stock temporaryStock = new Stock(symbol, temporaryStockHashMap.get(symbol));
                stockList.add(temporaryStock);
            }
            Collections.sort(stockList);
            stockAdapter.notifyDataSetChanged();

        } else {

            nameDownloader = new NameDownloader();
            String doInBackground = nameDownloader.doInBackground();
            nameDownloader.onPostExecute(doInBackground);

            for (String symbol : temporaryStockHashMap.keySet()) {
                selectOption(symbol, "UPDATE");
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add, menu);
        return true;
    }
    public boolean clickAdd(MenuItem item) {

        boolean connected = isConnected();

        if (connected) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText e = new EditText(this);

            e.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            e.setGravity(Gravity.CENTER_HORIZONTAL);
            e.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            builder.setView(e);
            builder.setTitle("Stock Selection");
            builder.setMessage("Please enter a Stock Symbol:");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    processAddInput(e.getText().toString());
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            noNetworkAlert("ADD");
        }

        return true;
    }

    private void processAddInput(String input) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        final List<String> matchingList = nameDownloader.matchInput(input);

        if (matchingList == null || matchingList.isEmpty()) {
            builder.setTitle("Symbol Not Found: " + input);
            builder.setMessage("Data for stock symbol");

            AlertDialog dialog = builder.create();

            dialog.show();
        } else {
            final CharSequence[] cs = matchingList.toArray(new CharSequence[matchingList.size()]);

            //handleDuplicates("ADD", input);

            if (cs.length > 1) {

                builder.setTitle("Make a Selection");
                builder.setItems(cs, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        String selected = cs[which].toString();
                        selected = selected.split(" - ")[0];
                        selectOption(selected, "ADD");
                    }
                });

                builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        return;
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();

            } else {
                String selected = cs[0].toString();
                selected = selected.split(" - ")[0];
                //selectOption(selected, );
            }
        }
    }

    public void selectOption(String selection, String mode) {
//        StockDownloader_AsyncTask loaderTaskRunnable = new StockDownloader_AsyncTask(this, selection, fahrenheit);
//        new Thread(loaderTaskRunnable).start();
        //new StockDownloader_AsyncTask(this).execute(apiToken, selection, mode);


    }


    private boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            Toast.makeText(this, "Cannot access ConnectivityManager", Toast.LENGTH_SHORT).show();
            return false;
        }

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public void noNetworkAlert(String mode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        if (mode.equals("ADD")) {
            builder.setMessage("Stocks Cannot Be Added Without A Network Connection");
        } else if (mode.equals("UPDATE")) {
            builder.setMessage("Stocks Cannot Be Updated Without A Network Connection");
        }

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    private void doRefresh() {

        stockList.clear();

        readJSON();

        boolean connected = isConnected();

        if (!connected) {
            noNetworkAlert("UPDATE");
        } else {

            for (String symbol : temporaryStockHashMap.keySet()) {
                selectOption(symbol, "UPDATE");
            }
        }
        swipe.setRefreshing(false);
    }

    @Override
    public boolean onLongClick(View view) {

        final int dPos = recyclerView.getChildLayoutPosition(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Stock");
        builder.setMessage("Delete Stock Symbol "+ stockList.get(dPos).getSymbol() + "?" );
        //builder.setIcon(R.drawable.baseline_delete_outline_black_48);

        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {

                stockList.remove(dPos);
                writeJSON();
                stockAdapter.notifyDataSetChanged();

            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });

        AlertDialog ad = builder.create();
        ad.show();

        return true;
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onClick(View view) {

        Intent i = new Intent(Intent.ACTION_VIEW);

        final int dPos = recyclerView.getChildLayoutPosition(view);

        String symbol = stockList.get(dPos).getSymbol();

        String url = "https://www.marketwatch.com/investing/stock/" + symbol;

        i.setData(Uri.parse(url));
        startActivity(i);

    }

    private void writeJSON() {

        Log.d(TAG, "writeJSON: Writing the JSON File");
        JSONArray jsonArray = new JSONArray();

        for (Stock s : stockList) {
            try {
                JSONObject noteJSON = new JSONObject();
                noteJSON.put("stockSymbol", s.getSymbol());
                noteJSON.put("stockName", s.getCompanyName());
                jsonArray.put(noteJSON);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String jsonString = jsonArray.toString();

            Log.d(TAG, "writeJSON: " + jsonString);

            try {
                OutputStreamWriter osw = new OutputStreamWriter(openFileOutput("STOCKS.txt", Context.MODE_PRIVATE));
                osw.write(jsonString);
                osw.close();

            } catch (IOException i) {
                Log.d(TAG, "writeJSON: Writing failed!" + i.toString());
            }
        }
    }

    public void readJSON() {

        temporaryStockHashMap.clear();

        Log.d(TAG, "readJSON: Reading the JSON File");
        stockList.clear();
        try {
            InputStream inputStream = openFileInput("STOCKS.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bf = new BufferedReader(inputStreamReader);

                String comingString = "";
                StringBuilder strBuilder = new StringBuilder();

                while ((comingString = bf.readLine()) != null) {
                    strBuilder.append(comingString);
                }

                inputStream.close();

                String jsonString = strBuilder.toString();

                try {
                    JSONArray jsonArray = new JSONArray(jsonString);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jo = jsonArray.getJSONObject(i);
                        String symbol = jo.getString("stockSymbol");
                        String name = jo.getString("stockName");

                        temporaryStockHashMap.put(symbol, name);
                    }

                } catch (JSONException j) {
                    j.printStackTrace();
                }
            }
        } catch (FileNotFoundException f) {
            Log.d(TAG, "readJSON: " + f.toString());
        } catch (IOException e) {
            Log.d(TAG, "readJSON: " + e.toString());
        }
    }

    public void handleDuplicates(String mode, Stock s) {

        Boolean duplicate = false;

        if(mode.equals("ADD")){
            for (Stock t : stockList) {
                if (t.getSymbol().equals(s.getSymbol())) {
                    duplicate = true;
                }
            }
        }

        if(duplicate){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("Duplicate Stock");
            builder.setMessage("Stock Symbol " + s.getSymbol() + " is already displayed");

            AlertDialog dialog = builder.create();
            dialog.show();

        }else {
            stockList.add(s);
            java.util.Collections.sort(stockList);
            if (mode.equals("ADD")) {
                writeJSON();
            }
            stockAdapter.notifyDataSetChanged();
        }
    }

}

