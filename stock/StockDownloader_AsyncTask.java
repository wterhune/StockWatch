package com.wisaterhunep.stock;


/*
When you have the desired stock symbol (and company name), you use the stock
symbol to download financial data
for that stock. For this you will need an API key.
 */
import android.net.Uri;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class StockDownloader_AsyncTask implements Runnable{
    private static final String TAG = "StockDownloader_AsyncTask";

    private MainActivity mainActivity;
    private String stockName; 
    private String stockSymbol;
    private String stockInfoFromURL;
    private double stockLatestPrice = 0.0;
    private double priceChange = 0.0;
    private double percentageChange = 0.0;
    static ArrayList<Double> stockDetails = new ArrayList<>(3);
    
    private static final String URL = "https://cloud.iexapis.com/stable/stock/";
    //https://cloud.iexapis.com/stable/stock/STOCK_SYMBOL/quote?token=API_KEY
    private static final String API_KEY = "pk_b7c3a30c5dcb4e85b48607f7f6528a54";

    StockDownloader_AsyncTask(MainActivity mainActivity, String stockSymbol, String stockName) { //constructor for this class
        this.mainActivity = mainActivity;
        this.stockSymbol = stockSymbol;
        this.stockName = stockName;
    }

    @Override
    public void run() {

        stockInfoFromURL = getStockInfoWithURL(); //call the complete url to get the JSON objects
        //extract JSON fields "latestPrice", "change", and "changePercent" and to into stock details array
        if(stockInfoFromURL != null) {
            stockDetails = parseJsonInformation(stockInfoFromURL);
        }

        //Passing the stock details information to the main activity; can have null fields
        Stock stock = new Stock(this.stockSymbol, this.stockName, stockDetails.get(0).toString(), stockDetails.get(1).toString(), stockDetails.get(2).toString());
        Log.d( TAG, "Passing stock details to main from StockDownloader_AsyncTask : " + stock.toString());
        //mainActivity.updateData(stock);
    }

    protected String getStockInfoWithURL() {

        String request = String.format("%s/quote?token=%s", stockSymbol, API_KEY); //completing the rest of the request link
        Uri.Builder URLBuilder = Uri.parse(URL.concat(request)).buildUpon();
        String useThisURL = URLBuilder.build().toString();

        Log.d( TAG, "request URL to get a stock information: " + useThisURL);

        StringBuilder stringBuilder = new StringBuilder();

        try {
            URL url = new URL(useThisURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); //The request is of type GET
            InputStream input = connection.getInputStream(); //Assuming GET request gives HTTP status of 200
            BufferedReader reader = new BufferedReader((new InputStreamReader(input)));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n'); //Reading in the response fields from the GET request
            }

            Log.d( TAG, "Individual stock information received from URL: "
                    + stringBuilder.toString() );

        } catch (Exception error) {
            //Adding additional logs for debugging purposes
            Log.e( TAG, "Unable to retrieve individual stock information from URL: ", error);
            error.printStackTrace();
            error.getCause();
            error.getMessage();
            return null;
        }
        return stringBuilder.toString();
    }


    //This method will take the incoming stringBuilder of JSON fields to retrieve the stock information
    //The fields we have to retrieve are symbol, companyName, latestPrice, change, and changePercent.
    private ArrayList<Double> parseJsonInformation(String stockInfo) {
        Log.d( TAG, "parseJsonInformation: retrieving stock financial information");

        try {
            JSONObject jsonStockInfoObject = new JSONObject(stockInfo);

            String latestPrice = jsonStockInfoObject.getString( "latestPrice");

            if (!latestPrice.trim().isEmpty() && latestPrice != null) {
                stockLatestPrice = Double.parseDouble(latestPrice);
            }
            String stockPriceChange = jsonStockInfoObject.getString( "change");

            if (!stockPriceChange.trim().isEmpty() && stockPriceChange != null) {
                priceChange = Double.parseDouble(stockPriceChange);
            }
            String stockPercentChange = jsonStockInfoObject.getString( "changePercent");

            if (!latestPrice.trim().isEmpty() && stockPercentChange != null) {
                percentageChange = Double.parseDouble(stockPercentChange);
            }
            stockDetails.add(stockLatestPrice);
            stockDetails.add(priceChange);
            stockDetails.add(percentageChange);
            return stockDetails;

        } catch (JSONException error) {
            //Adding additional logs for debugging purposes
            Log.d( TAG, "parseJsonInformation: unable to parse JSON information retrieved " +
                    "from stock financial information.");
            error.printStackTrace();
            error.getCause();
            error.getMessage();
        }
        return null;
    }
}



